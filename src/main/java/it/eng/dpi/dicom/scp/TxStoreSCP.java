/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.dpi.dicom.scp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.AssociationAcceptEvent;
import org.dcm4che2.net.AssociationCloseEvent;
import org.dcm4che2.net.AssociationListener;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xadisk.additional.XAFileOutputStreamWrapper;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileOutputStream;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.bean.CStoreBean;
import it.eng.dpi.bean.DicomNode;
import it.eng.dpi.bean.QueryBean;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.dicom.scu.QRConstants.QueryRetrieveLevel;
import it.eng.dpi.service.DPIConstants;
import it.eng.dpi.service.QueryMoveService;

public final class TxStoreSCP {

    private static final Logger log = LoggerFactory.getLogger(TxStoreSCP.class);
    private static final Logger audit = LoggerFactory.getLogger("STORESCP");
    private static final Logger missingsopclasslog = LoggerFactory.getLogger("MISSINGSOPCLASSLOG");

    private File storageRootDir;
    private XAFileSystem xaDiskNativeFS;

    private final Map<Association, AssociationBean> associationDataMap = new HashMap<Association, AssociationBean>();
    private final Map<Association, Integer> associationCounterMap = new HashMap<Association, Integer>();
    private CustomStorageService cStoreSCP;
    private QueryMoveService qrService;
    private List<DicomNode> dicomNodes;

    // DAV
    private boolean acceptMore;
    // DAV
    private boolean contaMultiframe;

    public TxStoreSCP(NetworkApplicationEntity ae, String[] sopClasses, QueryMoveService qrService) {
        this.qrService = qrService;
        cStoreSCP = new CustomStorageService(sopClasses);
        log.info("Registering STORE SCP Service ...");
        ae.register(cStoreSCP);
        ae.addAssociationListener(cStoreSCP);
        log.info("Registering STORE SCP Service ... done");

    }

    public void refreshSopClass(NetworkApplicationEntity ae, String[] sopClasses) {
        CustomStorageService tempStoreSCP = new CustomStorageService(sopClasses);
        log.info("Registering NEW STORE SCP Service ...");
        ae.register(tempStoreSCP);
        ae.addAssociationListener(tempStoreSCP);
        log.info("Registering NEW STORE SCP Service ... done");
        log.info("Un-registering OLD STORE SCP Service ...");
        ae.unregister(cStoreSCP);
        ae.removeAssociationListener(cStoreSCP);
        log.info("Un-registering OLD STORE SCP Service ... done");
        cStoreSCP = tempStoreSCP;
    }

    private final class CustomStorageService extends StorageService implements AssociationListener {
        public CustomStorageService(final String[] sopClasses) {
            super(sopClasses);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.dcm4che2.net.service.CStoreSCP#cstore(org.dcm4che2.net.Association , int,
         * org.dcm4che2.data.DicomObject, org.dcm4che2.net.PDVInputStream, java.lang.String)
         */
        /**
         * Adapted from StorageService, with a few tweaks for better compliance with the standard: (see PS 3.7, section
         * 9.3.1.2) + standard requires UID fields in C-STORE-RSP + standard requires Data Set Type (Null) in response (
         */
        @Override
        public void cstore(final Association as, final int pcid, final DicomObject rq, final PDVInputStream dataStream,
                final String tsuid) throws DicomServiceException, IOException {
            final boolean includeUIDs = CommandUtils.isIncludeUIDinRSP();
            CommandUtils.setIncludeUIDinRSP(true);
            final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
            onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
            as.writeDimseRSP(pcid, rsp);
            onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
            CommandUtils.setIncludeUIDinRSP(includeUIDs);
        }

        @Override
        protected void onCStoreRQ(final Association association, final int pcid, final DicomObject rq,
                final PDVInputStream dataStream, final String transferSyntaxUID, final DicomObject dcmRspObj)
                throws IOException {
            final AssociationBean assBean = associationDataMap.get(association);
            final Session session = assBean.getSession();
            final Map<String, CStoreBean> studyUIDs = assBean.getStudyUIDs();
            try {
                final String classUID = rq.getString(Tag.AffectedSOPClassUID);
                final String instanceUID = rq.getString(Tag.AffectedSOPInstanceUID);
                // log.debug("-instanceUID " + instanceUID);
                final String moveAET = rq.getString(Tag.MoveOriginatorApplicationEntityTitle);
                boolean isDPIMove = false;
                if (moveAET != null && moveAET.equals(association.getCalledAET())) {
                    isDPIMove = true;
                }
                final String pacsDir = getStorageRootDir().getPath() + File.separator + association.getCallingAET()
                        + File.separator + (isDPIMove ? DPIConstants.CMOVE_DIR : DPIConstants.CSTORE_DIR);

                if (!session.fileExistsAndIsDirectory(new File(pacsDir))) {
                    log.error("Il nodo DICOM " + association.getCallingAET() + " non è configurato nel DPI");
                }
                final BasicDicomObject fileMetaDcmObj = new BasicDicomObject();
                fileMetaDcmObj.initFileMetaInformation(classUID, instanceUID, transferSyntaxUID);
                String tempNamePart = instanceUID.replaceAll("\\.", "_");
                String fileName = tempNamePart + UUID.randomUUID() + ".dcm";
                File tempFile = new File(pacsDir, fileName);
                session.createFile(tempFile, false);
                DicomInputStream dis = null;
                DicomObject data = null;
                XAFileOutputStream xafos = session.createXAFileOutputStream(tempFile, true);
                XAFileOutputStreamWrapper wrapperOS = new XAFileOutputStreamWrapper(xafos);
                // 600000 bytes appears to be a fairly optimal cache
                // size to
                // maximize throughput for single-frame CT data
                final DicomOutputStream outStream = new DicomOutputStream(new BufferedOutputStream(wrapperOS, 600000));
                try {
                    outStream.writeFileMetaInformation(fileMetaDcmObj);
                    dataStream.copyTo(outStream);
                } finally {
                    outStream.close();
                }

                try {
                    dis = new DicomInputStream(new BufferedInputStream(XAUtil.createFileIS(session, tempFile, false)));
                    dis.setHandler(new StopTagInputHandler(Tag.PixelData));
                    data = dis.readDicomObject();
                } finally {
                    IOUtils.closeQuietly(dis);
                }

                // Leggo suid
                // DicomObject data = dataStream.readDataset();
                String suid = data.getString(Tag.StudyInstanceUID);
                String seruid = data.getString(Tag.SeriesInstanceUID);
                // log.debug("-SeriesInstanceUID " + seruid);
                File studyDir = new File(pacsDir, suid);
                boolean fileExistsAndIsDirectory = session.fileExistsAndIsDirectory(studyDir, true);
                if (!studyUIDs.containsKey(suid) && fileExistsAndIsDirectory) {
                    audit.info("ERRORE STUDIO CON UID: " + suid + " GIA' RICEVUTO");
                    log.info("Chiudo l'associazione perchè lo studio è già presente");
                } else {
                    if (!studyUIDs.containsKey(suid) && !fileExistsAndIsDirectory) {
                        log.info("Ricevuto nuovo studio SUID: " + suid
                                + ", interrogo il PACS per conoscere il numero di serie e di immagini");
                        // chiedo il numero di serie e di istanze per lo studio;
                        QueryBean query = new QueryBean(QueryRetrieveLevel.STUDY_STUDY_ROOT_FIRST);
                        query.addReturnKey(
                                new int[] { Tag.NumberOfStudyRelatedSeries, Tag.NumberOfStudyRelatedInstances });
                        query.addMatchingKey(new int[] { Tag.StudyInstanceUID }, suid);
                        DicomNode node = getDicomNode(association.getCallingAET());
                        List<DicomObject> study = qrService.doCFind(query, node.getHostname(), node.getPort(),
                                node.getAet());
                        CStoreBean sb = new CStoreBean();
                        sb.setNumImagesInStudy(study.get(0).getInt(Tag.NumberOfStudyRelatedInstances));
                        sb.setNumSeriesInStudy(study.get(0).getInt(Tag.NumberOfStudyRelatedSeries));
                        studyUIDs.put(suid, sb);
                        session.createFile(studyDir, true);
                    }
                    if (studyUIDs.containsKey(suid)) {
                        File studySeriesDir = new File(studyDir, seruid);
                        if (!session.fileExistsAndIsDirectory(studySeriesDir)) {
                            session.createFile(studySeriesDir, true);
                            studyUIDs.get(suid).incTransferedSeriesInStudy();
                        }

                        final String dicomFileBaseName = instanceUID + DPIConstants.DICOM_FILE_EXTENSION;
                        final File dicomImage = new File(studySeriesDir, dicomFileBaseName);
                        // Verifico che il file dell'immagine non esista ..
                        // questo più
                        // verificarsi se in una stessa associazione ricevo due
                        // volte lo
                        // stesso studio (molto difficile)
                        if (!session.fileExists(dicomImage)) {
                            XAUtil.moveFile(session, tempFile, dicomImage);

                            if (contaMultiframe) {
                                Integer numOfFrames = data.getInt(Tag.NumberOfFrames);
                                // log.debug("Numero di Frames complessivi: " +
                                // studyUIDs.get(suid).getNumFramesInStudy() + "\n - Frame in imm corrente: " +
                                // numOfFrames);
                                if (numOfFrames != null && numOfFrames > 0) {
                                    studyUIDs.get(suid).incNumFramesInStudy(numOfFrames);
                                } else {
                                    studyUIDs.get(suid).incNumImageNoFrame();
                                }
                                // log.debug("Frame in imm corrente: " + numOfFrames +
                                // "\nNumero di Frames complessivi: " + studyUIDs.get(suid).getNumFramesInStudy() +
                                // "\nNumero immagini NoFrame: " + studyUIDs.get(suid).getNumImageNoFrame());
                            }

                            studyUIDs.get(suid).incTransferedImagesInStudy();
                            final Integer instanceSerialNo = incrementAssociationCounter(association);
                            if (instanceSerialNo % 10 == 0) {
                                log.debug("Received instance no. " + instanceSerialNo);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                log.error(
                        "Si è verificato un errore di I/O durante il ricevimento dell'immagine, setto lo studio per il rollback e chiudo l'associazione",
                        e);
                assBean.setException(e);
                throw e;
            } catch (Exception e) {
                log.error(
                        "Si è verificato un errore durante il ricevimento dell'immagine, setto lo studio per il rollback e chiudo l'associazione",
                        e);
                assBean.setException(e);
                throw new IOException(e);
            }
        }

        public void associationAccepted(final AssociationAcceptEvent associationAcceptEvent) {
            boolean pcAccepted = false;

            Association assoc = associationAcceptEvent.getAssociation();
            for (PresentationContext pc : assoc.getAssociateAC().getPresentationContexts()) {
                if (pc.isAccepted()) {
                    pcAccepted = true;
                    break;
                }
            }
            if (!pcAccepted) {
                missingsopclasslog.info(assoc.getAssociateRQ().toString());
            }
            Session session = null;
            try {
                session = xaDiskNativeFS.createSessionForLocalTransaction();
                final Association association = associationAcceptEvent.getAssociation();
                associationDataMap.put(association, new AssociationBean(session));
                log.info("Association created: " + association.toString());
            } catch (Exception e) {
                log.error("Si è verificato un'errore durante l'apertura dell'associazione " + e);
                if (session != null) {
                    doRollback(session);
                }
                throw e;
            }
        }

        public void associationClosed(final AssociationCloseEvent associationCloseEvent) {
            final Association association = associationCloseEvent.getAssociation();
            final AssociationBean assBean = associationDataMap.get(association);
            final Session session = assBean != null ? assBean.getSession() : null;
            // l'associazione potrebbe non essere stata accettata ...
            if (session != null) {
                final Map<String, CStoreBean> studyUIDs = assBean.getStudyUIDs();
                boolean commit = true;
                StringBuilder auditLog = new StringBuilder();
                // la mappa potrebbe essere vuota se non ho trasferito nessuna
                // immagine nell'associazione
                if (studyUIDs.entrySet().isEmpty()) {
                    commit = false;
                    // auditLog.append("ROLLBACK | NESSUNA IMMAGINE TRASFERITA");
                } else {
                    for (Entry<String, CStoreBean> study : studyUIDs.entrySet()) {
                        CStoreBean studyBean = study.getValue();

                        // DAV
                        if (contaMultiframe) {
                            commit = studyBean.isStudyCompleteWithMultiframe(acceptMore);
                        } else {
                            commit = studyBean.isStudyComplete(acceptMore);
                        }
                        // vecchio controllo Marco
                        // commit = studyBean.isStudyComplete();

                        auditLog.append((commit) ? "COMMIT" : "ROLLBACK");
                        auditLog.append(" | SUID ");
                        auditLog.append(study.getKey());
                        auditLog.append(" | TRASFERITE ");

                        if (contaMultiframe) {
                            auditLog.append(studyBean.getTransferedImagesInStudy() + " (o "
                                    + (studyBean.getNumImageNoFrame() + studyBean.getNumFramesInStudy())
                                    + " nel caso di conteggio con frame) (di " + studyBean.getNumImagesInStudy()
                                    + ") IMMAGINI");
                        } else {
                            auditLog.append(studyBean.getTransferedImagesInStudy() + " (di "
                                    + studyBean.getNumImagesInStudy() + ") IMMAGINI");
                        }
                        auditLog.append(" E " + studyBean.getTransferedSeriesInStudy() + " (di "
                                + studyBean.getNumSeriesInStudy() + ") SERIE");
                        if (!commit) {
                            log.info("Lo studio " + study.getKey()
                                    + " non è stato trasferito correttamente e/o in modo completo, procedo al rollback ...");
                            break;
                        }
                    }
                    if (assBean.getException() != null) {
                        auditLog.append(" | ECCEZIONE: " + assBean.getException().getMessage());
                        commit = false;
                    }
                }

                if (commit) {
                    doCommit(session);
                } else {
                    doRollback(session);
                    log.debug("Provo a lancare un abort dell'associazione");
                    association.abort();
                    log.debug("Abort dell'associazione lanciata");
                }

                associationDataMap.remove(association);
                final Integer assocInstanceCnt = associationCounterMap.get(association);
                log.info("Association closed: " + association.toString() + " after receiving "
                        + ((assocInstanceCnt == null) ? 0 : assocInstanceCnt) + " instances. ");
                if (auditLog.length() > 0) {
                    audit.info(auditLog.toString());
                }
                removeAssociationCounter(association);

            }
        }
    }

    private void doRollback(final Session session) {
        try {
            log.debug("Procedo al rollback della transazione");
            session.rollback();
        } catch (NoTransactionAssociatedException e1) {
            log.error("Non è possibile effettuare il rollback perchè non presente una transazione aperta");
        }
    }

    private void doCommit(final Session session) {
        try {
            log.debug("Procedo alla commit della transazione");
            session.commit();
        } catch (NoTransactionAssociatedException e) {
            log.error("Non è possibile effettuare il rollback perchè non presente una transazione aperta");
        }
    }

    private Integer incrementAssociationCounter(final Association association) {
        Integer oldVal = associationCounterMap.get(association);
        // Reset if a new association or if our hardware unexpectedly has not
        // started smoking after uninterrupted
        // DICOM pushing for a year or so.
        if (oldVal == null || oldVal >= 999999999) {
            oldVal = 0;
        }
        associationCounterMap.put(association, oldVal + 1);
        return oldVal + 1;
    }

    public DicomNode getDicomNode(String callingAET) {
        for (DicomNode d : dicomNodes) {
            if (d.getAet().equalsIgnoreCase(callingAET))
                return d;
        }
        return null;
    }

    private void removeAssociationCounter(final Association association) {
        associationCounterMap.remove(association);
    }

    public final File getStorageRootDir() {
        return storageRootDir;
    }

    public final void setStorageRootDir(final File storageRootDir) {
        this.storageRootDir = storageRootDir;
    }

    public XAFileSystem getXaDiskNativeFS() {
        return xaDiskNativeFS;
    }

    public void setXaDiskNativeFS(XAFileSystem xaDiskNativeFS) {
        this.xaDiskNativeFS = xaDiskNativeFS;
    }

    private final class AssociationBean {
        private Session session;
        private Map<String, CStoreBean> studyUIDs;
        private Exception e;

        public AssociationBean(Session session) {
            super();
            this.session = session;
            this.studyUIDs = new HashMap<String, CStoreBean>();
        }

        public Session getSession() {
            return session;
        }

        public Map<String, CStoreBean> getStudyUIDs() {
            return studyUIDs;
        }

        public Exception getException() {
            return e;
        }

        public void setException(Exception e) {
            this.e = e;
        }
    }

    public void setDicomNodes(List<DicomNode> dicomNodes) {
        this.dicomNodes = dicomNodes;
    }

    // DAV
    public void setAcceptMore(boolean acceptMore) {
        this.acceptMore = acceptMore;
    }

    public void setContaMultiframe(boolean contaMultiframe) {
        this.contaMultiframe = contaMultiframe;
    }

}
