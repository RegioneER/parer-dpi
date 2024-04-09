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

package it.eng.dpi.dicom.scu;

import it.eng.dpi.bean.CStoreBean;
import it.eng.dpi.bean.FileInfoBean;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.DataWriter;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.PDVOutputStream;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.util.CloseUtils;
import org.dcm4che2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xadisk.bridge.proxies.interfaces.Session;

public class StoreSCU {

    private static final Logger log = LoggerFactory.getLogger(StoreSCU.class);
    private static final int PEEK_LEN = 1024;

    private NetworkApplicationEntity ae;

    public StoreSCU(NetworkApplicationEntity ae) {

        this.ae = ae;
    }

    public NetworkApplicationEntity createRemoteAE(String hostname, int port, String remoteAET) {
        NetworkConnection remoteConn = new NetworkConnection();
        remoteConn.setPort(port);
        remoteConn.setHostname(hostname);
        NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
        remoteAE.setAETitle(remoteAET);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
        return remoteAE;
    }

    private synchronized Association open(NetworkApplicationEntity remoteAE, TransferCapability[] tc,
            ExecutorService executor) throws IOException, ConfigurationException, InterruptedException {
        ae.setTransferCapability(tc);
        return ae.connect(remoteAE, executor);
    }

    public CStoreBean sendStudy(NetworkApplicationEntity remoteAE, File studyRootDir, ExecutorService executor)
            throws InterruptedException, IOException, ConfigurationException, XAGenericException {
        return this.sendTxStudy(remoteAE, studyRootDir, null, executor);
    }

    /**
     * Trasferisco in una associazione un'intero studio
     *
     * @param remoteAE
     *            AE remoto per il recovery dell'associazione in caso di caduta
     * @param studyRootDir
     *            directory di riferimento
     * @param session
     *            sessione applicativa
     * @param executor
     *            esecutore generico
     * 
     * @return bean {@link CStoreBean}
     * 
     * @throws IOException
     *             eccezione generica
     * @throws InterruptedException
     *             eccezione generica
     * @throws ConfigurationException
     *             eccezione generica
     * @throws XAGenericException
     *             eccezione generica
     */
    public CStoreBean sendTxStudy(NetworkApplicationEntity remoteAE, File studyRootDir, Session session,
            ExecutorService executor)
            throws IOException, InterruptedException, ConfigurationException, XAGenericException {
        final CStoreBean infoBean = new CStoreBean();

        final List<FileInfoBean> fileInfoList = new ArrayList<FileInfoBean>();
        // Mappa contenente cuid, e tc[]
        Map<String, Set<String>> tcMap = new HashMap<String, Set<String>>();

        addFiles(studyRootDir, fileInfoList, tcMap, session);
        infoBean.setNumImagesInStudy(fileInfoList.size());
        TransferCapability[] tcNetAe = new TransferCapability[tcMap.size()];
        int j = 0;
        for (Map.Entry<String, Set<String>> tcEntry : tcMap.entrySet()) {
            tcNetAe[j++] = new TransferCapability(tcEntry.getKey(),
                    tcEntry.getValue().toArray(new String[tcEntry.getValue().size()]), TransferCapability.SCU);
        }
        Association assoc = null;
        try {
            assoc = this.open(remoteAE, tcNetAe, executor);

            fileLoop: for (final FileInfoBean fileInfo : fileInfoList) {

                final InputStream fis = new BufferedInputStream((session == null) ? new FileInputStream(fileInfo.getF())
                        : XAUtil.createFileIS(session, fileInfo.getF(), false));
                final DimseRSPHandler rspHandler = new DimseRSPHandler() {
                    @Override
                    public void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
                        StoreSCU.this.onRSP(cmd, fileInfo, infoBean);
                    }
                };

                final DataWriter dataWriter = new DataWriter() {
                    @Override
                    public final void writeTo(final PDVOutputStream outStream, final String reqTransferSyntaxUID)
                            throws IOException {

                        try {
                            if (!reqTransferSyntaxUID.equals(fileInfo.getTsuid())) {
                                throw new IOException("Requested transfer syntax UID " + reqTransferSyntaxUID
                                        + " not matching stored transfer syntax UID " + fileInfo.getTsuid());
                            }
                            long bytesToSkip = fileInfo.getFmiEndPos();
                            while (bytesToSkip > 0) {
                                bytesToSkip -= fis.skip(bytesToSkip);
                            }
                            outStream.copyFrom(fis);
                        } finally {
                            if (fis != null)
                                fis.close();
                        }
                    }
                };

                TransferCapability tc = assoc.getTransferCapabilityAsSCU(fileInfo.getCuid());
                String tsuid = selectTransferSyntax(tc.getTransferSyntax(), fileInfo.getTsuid());
                // Put the cstore operation in a loop, so that we can retry
                // if the first attempt fails;
                // for example, it's always possible that the SCP might have
                // closed the association.
                for (int i = 0; i < 2; ++i) {
                    try {
                        assoc.cstore(fileInfo.getCuid(), fileInfo.getIuid(), 0, dataWriter, tsuid, rspHandler);
                        log.info("Stored " + fileInfo.getF());
                        break;
                    } catch (final NoPresentationContextException e) {
                        log.error("Presentation Context not supported for file " + fileInfo.getF() + e.getMessage());
                        continue fileLoop;
                    } catch (final IllegalStateException e) {
                        if (i == 0) {
                            log.warn("Error during C-STORE operation - association is in invalid state;"
                                    + " will attempt to reestablish association: " + e.toString());
                            // Assume that association is beyond recovery,
                            // so we can't even release it
                            assoc = this.open(remoteAE, tcNetAe, executor);
                            // Second attempt
                            continue;
                        }
                        log.warn("Error during C-STORE operation - association is in invalid state;"
                                + " retry failed - aborting: " + e.getMessage());
                        throw e;
                    } catch (final InterruptedException e) {
                        log.error("Interrupted - exiting");
                        throw e;
                    }
                }

            }

        } finally {
            if (assoc != null) {
                assoc.waitForDimseRSP();
                assoc.release(false);
            }
        }

        return infoBean;
    }

    protected void onRSP(DicomObject cmd, FileInfoBean info, CStoreBean infoBean) {
        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        switch (status) {
        case 0:
            info.setTransferred(true);
            infoBean.incTransferedImagesInStudy();
            log.debug("Trasferite " + infoBean.getTransferedImagesInStudy() + " immagini");
            break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
            info.setTransferred(true);
            infoBean.incTransferedImagesInStudy();
            log.debug("Trasferite " + infoBean.getTransferedImagesInStudy() + " immagini");
            log.warn("WARNING: (MSG: " + msgId + ") Received RSP with Status " + StringUtils.shortToHex(status)
                    + "H for " + info.getF() + ", cuid=" + info.getCuid() + ", tsuid=" + info.getTsuid() + ", iuid="
                    + info.getIuid());
            log.warn(cmd.toString());
            break;
        default:
            log.error("ERROR: (MSG: " + msgId + ") Received RSP with Status " + StringUtils.shortToHex(status)
                    + "H for " + info.getF() + ", cuid=" + info.getCuid() + ", tsuid=" + info.getTsuid() + ", iuid="
                    + info.getIuid());
        }

    }

    // Aggiunge ad una lista "fileInfoList" le informazioni riguardo i file da trasferire al PACS
    public void addFiles(File f, List<FileInfoBean> fileInfoList, Map<String, Set<String>> tcMap, Session session)
            throws XAGenericException, IOException {
        if ((session == null) ? f.isDirectory() : XAUtil.fileExistsAndIsDirectory(session, f)) {
            File[] fs = (session == null) ? f.listFiles() : XAUtil.listFiles(session, f);
            if (fs == null || fs.length == 0)
                return;
            for (int i = 0; i < fs.length; i++)
                addFiles(fs[i], fileInfoList, tcMap, session);
            return;
        }
        FileInfoBean info = new FileInfoBean(f);
        DicomObject dcmObj = new BasicDicomObject();
        DicomInputStream dcmInStream = null;
        try {
            dcmInStream = new DicomInputStream(new BufferedInputStream(
                    (session == null) ? new FileInputStream(f) : XAUtil.createFileIS(session, f, false)));
            dcmInStream.setHandler(new StopTagInputHandler(Tag.StudyDate));
            dcmInStream.readDicomObject(dcmObj, PEEK_LEN);
            info.setTsuid(dcmInStream.getTransferSyntax().uid());
            info.setFmiEndPos(dcmInStream.getEndOfFileMetaInfoPosition());
        } catch (IOException e) {
            log.error("Miscellaneous I/O error while processing file \"" + f + "\"", e);
            throw e;
        } finally {
            CloseUtils.safeClose(dcmInStream);
        }
        info.setCuid(dcmObj.getString(Tag.MediaStorageSOPClassUID, dcmObj.getString(Tag.SOPClassUID)));
        if (info.getCuid() == null) {
            log.error("WARNING: Missing SOP Class UID in " + f + " - skipped.");
            return;
        }
        info.setIuid(dcmObj.getString(Tag.MediaStorageSOPInstanceUID, dcmObj.getString(Tag.SOPInstanceUID)));
        if (info.getIuid() == null) {
            log.error("WARNING: Missing SOP Instance UID in " + f + " - skipped.");
            return;
        }

        addTransferCapability(info.getCuid(), info.getTsuid(), tcMap);
        fileInfoList.add(info);
    }

    private void addTransferCapability(String cuid, String tsuid, Map<String, Set<String>> tcMap) {
        Set<String> ts = tcMap.get(cuid);
        if (ts == null) {
            ts = new HashSet<String>();
            ts.add(UID.ImplicitVRLittleEndian);
            tcMap.put(cuid, ts);
        }
        ts.add(tsuid);

    }

    private String selectTransferSyntax(String[] available, String tsuid) {
        if (tsuid.equals(UID.ImplicitVRLittleEndian))
            return selectTransferSyntax(available, DPIConstants.IVLE_TS);
        if (tsuid.equals(UID.ExplicitVRLittleEndian))
            return selectTransferSyntax(available, DPIConstants.EVLE_TS);
        if (tsuid.equals(UID.ExplicitVRBigEndian))
            return selectTransferSyntax(available, DPIConstants.EVBE_TS);
        for (int j = 0; j < available.length; j++)
            if (available[j].equals(tsuid))
                return tsuid;
        return null;
    }

    private String selectTransferSyntax(String[] available, String[] tsuids) {
        for (int i = 0; i < tsuids.length; i++)
            for (int j = 0; j < available.length; j++)
                if (available[j].equals(tsuids[i]))
                    return available[j];
        return null;
    }

}
