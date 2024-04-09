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

package it.eng.dpi.job;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;

import it.eng.dpi.bean.ProcessableFile;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.business.impl.SacerPingObjectCreator;
import it.eng.dpi.component.DPIContext;
import it.eng.dpi.component.FileQueue;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.DPIException;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType;

@Component
public class ObjectCreatorJob implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("CREA_OGGETTO");
    private static final Logger log = LoggerFactory.getLogger(ObjectCreatorJob.class);

    @Autowired
    private SacerPingObjectCreator sacerObjectCreator;

    @Autowired
    private DPIContext ctx;

    @Resource
    @Qualifier("dcmHashDicomTag")
    private List<String> dcmHashDicomTag;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private FileQueue fileQueue;

    public void doWork(String jobId) {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START + " | JOBID: " + jobId);
            processFiles(jobId);
            audit.info(DPIConstants.AUDIT_JOB_STOP + " | JOBID: " + jobId);
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " | JOBID: " + jobId + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }
    }

    private void processFiles(String jobId) throws NoSuchAlgorithmException, XAGenericException {
        List<ProcessableFile> fileList = fileQueue.getProcessableFiles(jobId);
        int studyCounter = 0;
        for (ProcessableFile pFile : fileList) {
            try {
                processStudy(pFile.getFilePath(), pFile.getDestinationSubDir(), pFile.getPacsName());
                fileQueue.remove(jobId, pFile.getFilePath(), pFile.getDestinationSubDir(), pFile.getPacsName());
                studyCounter++;
            } catch (IOException | DPIException e) {
                log.error("Si è verificato un errore processando lo studio " + pFile.getFilePath().getName()
                        + " del Pacs " + pFile.getPacsName() + " | JOBID: " + jobId);
            }

        }
        audit.info("STUDI TOTALI: " + fileList.size() + " | STUDI PROCESSATI: " + studyCounter + " | JOBID: " + jobId);
    }

    /**
     * 1- Creo DCM File 2- Calcolo DCM Hash 3- Creo Global File 4- Calcolo Global Hash 5- Creo ZIP 6- Creo XML
     * 
     * @param studyPath
     * @param destinationSubDir
     * @param pacsName
     * @param session
     * 
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws DPIException
     * @throws XAGenericException
     */
    private void processStudy(File studyPath, String destinationSubDir, String pacsName)
            throws IOException, NoSuchAlgorithmException, DPIException, XAGenericException {
        File[] series = XAUtil.listFilesNewTX(xaDiskNativeFS, studyPath);
        File[] instances = null;
        if (series.length > 0) {
            instances = XAUtil.listFilesNewTX(xaDiskNativeFS, series[0]);
        }
        if (instances != null && instances.length > 0) {
            DatiSpecificiType datiSpecificiDICOM = new DatiSpecificiType();
            datiSpecificiDICOM.setVersioneDatiSpecifici(DPIConstants.VERSIONE_DATI_SPEC_DICOM);
            datiSpecificiDICOM.setAETNodoDicom(pacsName);
            GregorianCalendar calendarDate = new GregorianCalendar();
            calendarDate.setTime(new Date(studyPath.lastModified()));
            try {
                datiSpecificiDICOM
                        .setDataPresaInCarico(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarDate));
            } catch (DatatypeConfigurationException e2) {
                log.error("Non sarebbe mai dovuto accadere", e2);
            }

            log.info("Creo l'oggetto Sacer per lo studio contenuto in " + studyPath);

            // 3
            String globalFile = sacerObjectCreator.generateGlobalFile(studyPath, datiSpecificiDICOM);
            log.debug("GLOBAL_FILE:\n" + StringUtils.abbreviate(globalFile, 10000));
            // 4
            String globalHash = sacerObjectCreator.calculateHash(globalFile);
            log.debug("GLOBAL_HASH:" + globalHash);

            // 1 - Ottengo tutte le prime immagini di ogni serie in modo da processare i tag del DCM HASH per ogni
            // serie.
            // Potrebbe capitare infatti che all'interno di uno studio alcune serie non contengano tutti i tag
            List<File> firstInstancesInSeries = new ArrayList<File>();
            for (File serie : series) {
                firstInstancesInSeries.add(XAUtil.listFilesNewTX(xaDiskNativeFS, serie)[0]);
            }
            Map<String, DicomElement> orderedMap = sacerObjectCreator.readDicomValues(dcmHashDicomTag,
                    firstInstancesInSeries);
            String dcmFile = sacerObjectCreator.generateDcmFile(orderedMap,
                    datiSpecificiDICOM.getNumberStudyRelatedImages());
            log.debug("DCM_FILE:\n" + dcmFile);
            // 2
            String dcmHash = sacerObjectCreator.calculateHash(dcmFile);
            log.debug("DCM_HASH:" + dcmHash);

            datiSpecificiDICOM.setDCMHash(dcmHash);
            datiSpecificiDICOM.setDCMHashAlgo(DPIConstants.HASH_ALGO);
            datiSpecificiDICOM.setDCMHashEncoding(DPIConstants.HASH_ENCODING);
            datiSpecificiDICOM.setDCMHashDescrizione(sacerObjectCreator.encodeFileString(dcmFile));

            datiSpecificiDICOM.setGLOBALHash(globalHash);
            datiSpecificiDICOM.setGLOBALHashAlgo(DPIConstants.HASH_ALGO);
            datiSpecificiDICOM.setGLOBALHashEncoding(DPIConstants.HASH_ENCODING);
            datiSpecificiDICOM.setGLOBALHashDescrizione(sacerObjectCreator.encodeFileString(globalFile));

            // String destDir = ctx.getWorkingPath() + DPIConstants.NEW_FOLDER + File.separator + destinationSubDir;
            String destDir = ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.NEW_FOLDER + File.separator
                    + destinationSubDir;
            // 5 //6
            if (!sacerObjectCreator.makeSacerPINGArchive(studyPath, globalHash, destDir, datiSpecificiDICOM)) {
                throw new DPIException("E' stato eseguito il rollback della transazione");
            }
        }
    }

    @Override
    public void doWork() {
        // TODO Auto-generated method stub

    }

}
