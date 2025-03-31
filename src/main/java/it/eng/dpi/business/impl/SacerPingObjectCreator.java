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

package it.eng.dpi.business.impl;

import it.eng.dpi.business.DicomReaderInterface;
import it.eng.dpi.business.StudyArchiveMakerInterface;
import it.eng.dpi.component.JAXBSingleton;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType;
import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType.ModalityInStudyList;
import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType.SOPClassList;
import it.eng.sacerasi.ws.xml.invioasync.ListaUnitaDocumentarieType;
import it.eng.sacerasi.ws.xml.invioasync.UnitaDocumentariaType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

@Service
@Scope("prototype")
public class SacerPingObjectCreator implements DicomReaderInterface, StudyArchiveMakerInterface {

    private static final Logger log = LoggerFactory.getLogger(SacerPingObjectCreator.class);
    private static final Logger audit = LoggerFactory.getLogger("INVIO_OGGETTO_SACER_PING");

    private static final int BUFFER_SIZE = 1 * 1024 * 1024;

    @Resource
    @Qualifier("replaceEmptyTag")
    private boolean replaceEmptyTag;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private JAXBSingleton jaxbSingleton;

    @Override
    public Map<String, DicomElement> readDicomValues(List<String> dcmHashDicomTag, List<File> dicomInstances)
            throws IOException {
        Map<String, DicomElement> map = new LinkedHashMap<String, DicomElement>();
        for (File dicomInstance : dicomInstances) {
            try (DicomInputStream din = new DicomInputStream(dicomInstance)) {
                din.setHandler(new StopTagInputHandler(Tag.PixelData));
                DicomObject dcmObj = din.readDicomObject();
                this.getDicomValues(dcmHashDicomTag, dcmObj, map);
            }
        }
        return map;
    }

    public void getDicomValues(List<String> dcmHashDicomTag, DicomObject dcmObj, Map<String, DicomElement> map) {
        for (String tag : dcmHashDicomTag) {
            DicomElement de = map.get(tag);
            if (de == null || de.isEmpty()) {
                int intTag = Integer.decode(tag);
                map.put(tag, dcmObj.get(intTag));
            }
        }
    }

    @Override
    public String generateDcmFile(Map<String, DicomElement> orderedMap, int numberOfImages) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, DicomElement> entry : orderedMap.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            String val = "";
            Integer entryDec = Integer.decode(entry.getKey());
            if (entryDec.equals(Tag.NumberOfStudyRelatedInstances) && entry.getValue() == null) {
                val = String.valueOf(numberOfImages);
            } else if (entry.getValue() != null) {
                val = entry.getValue().getValueAsString(new SpecificCharacterSet("UTF-8"), Integer.MAX_VALUE);
                val = val != null ? val : "";
                if (entryDec.equals(Tag.StudyTime)) {
                    // se la stringa contiene i decimali li elimino
                    if (val.contains(".")) {
                        val = val.split("\\.")[0];
                    }
                    // se la stringa non contiene i secondi aggiungo 00
                    if (val.length() == 4) {
                        val = val.concat("00");
                    }
                }
            }
            sb.append(val);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String generateGlobalFile(File studyRootDir, DatiSpecificiType datiSpecificiDICOM)
            throws NoSuchAlgorithmException, IOException, XAGenericException {
        StringBuilder sb = new StringBuilder();
        File[] studyRootDirFiles = XAUtil.listFilesNewTX(xaDiskNativeFS, studyRootDir);
        Arrays.sort(studyRootDirFiles, NameFileComparator.NAME_COMPARATOR);
        datiSpecificiDICOM.setNumberStudyRelatedSeries(studyRootDirFiles.length);
        for (File seriesDir : studyRootDirFiles) {
            log.debug("Processo la serie " + seriesDir);
            File[] seriesDirFiles = XAUtil.listFilesNewTX(xaDiskNativeFS, seriesDir);
            Arrays.sort(seriesDirFiles, NameFileComparator.NAME_COMPARATOR);
            datiSpecificiDICOM.setNumberStudyRelatedImages(
                    datiSpecificiDICOM.getNumberStudyRelatedImages() + seriesDirFiles.length);
            for (File instance : seriesDirFiles) {
                log.trace("Processo l'istanza " + instance);
                sb.append(seriesDir.getName() + "/" + instance.getName());
                sb.append(",");
                sb.append(calculateHash(instance, false));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Calcola l'hash di un file: è utilizzato per il calcolo del global hash e del dcm hash
     *
     */
    @Override
    public String calculateHash(String fileString) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(DPIConstants.HASH_ALGO);
        md.update(fileString.getBytes("UTF-8"), 0, fileString.length());
        byte[] pwdHash = md.digest();
        return new String(toHexString(pwdHash));
    }

    @Override
    public String calculateHash(File file, boolean readAllFileInMemory) throws NoSuchAlgorithmException, IOException {
        if (readAllFileInMemory) {
            MessageDigest md = MessageDigest.getInstance(DPIConstants.HASH_ALGO);
            byte[] fileArray = FileUtils.readFileToByteArray(file);
            md.update(fileArray);
            byte[] pwdHash = md.digest();
            return new String(toHexString(pwdHash));
        } else {
            try (InputStream is = FileUtils.openInputStream(file)) {
                return this.calculateHash(is);
            }
        }
    }

    /**
     * Calcola l'hash di un file: è utilizzato per il calcolo del hash del file zip provvede a chiudere lo stream
     * passato in input
     *
     */
    private String calculateHash(InputStream is) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(DPIConstants.HASH_ALGO);
        try (DigestInputStream dis = new DigestInputStream(is, md)) {
            int ch;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((ch = dis.read(buffer)) != -1) {
                log.trace("Letti " + ch + " bytes");
            }
        }
        byte[] pwdHash = md.digest();
        return new String(toHexString(pwdHash));
    }

    private static String toHexString(byte[] hash) {
        return Hex.encodeHexString(hash);
    }

    @Override
    public String encodeFileString(String fileString) throws UnsupportedEncodingException {
        byte[] fileUtf8 = fileString.getBytes("UTF-8");
        return new String(Base64.encodeBase64(fileUtf8), "UTF-8");
    }

    private void deleteStudyFolder(Session session, File root) throws XAGenericException {

        File[] fileList = XAUtil.listFiles(session, root);
        for (File file : fileList) {
            if (XAUtil.fileExistsAndIsDirectory(session, file)) {
                deleteStudyFolder(session, file);
            }
            XAUtil.deleteFile(session, file);
        }
    }

    @Override
    public boolean makeSacerPINGArchive(File studypath, String globalHash, String destination,
            DatiSpecificiType datiSpecificiDICOM) {
        final File zipStudy = new File(destination, globalHash + ".zip");
        final File xmlStudy = new File(destination, globalHash + ".xml");
        final Session session = xaDiskNativeFS.createSessionForLocalTransaction();
        try {

            if (!session.fileExists(zipStudy)) {
                createZip(session, zipStudy, studypath, datiSpecificiDICOM);
                InputStream is = XAUtil.createFileIS(session, zipStudy, false);
                datiSpecificiDICOM.setFILEHash(calculateHash(is));
                log.debug("ZIP_FILE_HASH: " + datiSpecificiDICOM.getFILEHash());
                datiSpecificiDICOM.setFILEHashAlgo(DPIConstants.HASH_ALGO);
                datiSpecificiDICOM.setFILEHashEncoding(DPIConstants.HASH_ENCODING);
                createXml(session, xmlStudy, datiSpecificiDICOM);
                deleteStudyFolder(session, studypath);
                session.deleteFile(studypath);
            } else {
                log.debug("Studio con global-hash " + globalHash + " già esistente nella folder /new");
                audit.info("PACS: " + datiSpecificiDICOM.getAETNodoDicom() + " GLOBAL-HASH: " + globalHash
                        + " Studio già presente");
            }
            session.commit();
            return true;
        } catch (Exception e) {
            log.error(
                    "Si è verificato un errore durante la creazione dell'oggetto ZIP+XML da inviare a Sacer ... procedo al rollback",
                    e);
            try {
                session.rollback();
            } catch (NoTransactionAssociatedException e1) {
                log.error("Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                        e1);
            }
        }
        return false;
    }

    private void createXml(Session session, File xmlStudy, DatiSpecificiType datiSpecificiDICOM)
            throws JAXBException, XAGenericException {
        JAXBElement datiSpecificiDICOMJAXB = new it.eng.sacerasi.ws.xml.datispecdicom.ObjectFactory()
                .createDatiSpecifici(datiSpecificiDICOM);

        UnitaDocumentariaType uniDoc = new UnitaDocumentariaType();
        uniDoc.setDatiSpecifici(datiSpecificiDICOMJAXB);
        ListaUnitaDocumentarieType xmlVersamentoPreI = new ListaUnitaDocumentarieType();
        xmlVersamentoPreI.setVersione(DPIConstants.VERSIONE_XML_PREINGEST);
        xmlVersamentoPreI.getUnitaDocumentaria().add(uniDoc);
        OutputStream os = null;
        try {
            os = XAUtil.createFileOS(session, xmlStudy, true);
            Marshaller m = jaxbSingleton.getContextListaUnitaDocumentarieType().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            JAXBElement<ListaUnitaDocumentarieType> xmlVersamentoPreIJAXB = new it.eng.sacerasi.ws.xml.invioasync.ObjectFactory()
                    .createListaUnitaDocumentarie(xmlVersamentoPreI);
            m.marshal(xmlVersamentoPreIJAXB, os);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void createZip(Session session, File zipStudy, File studypath, DatiSpecificiType datiSpecificiDICOM)
            throws XAGenericException, IOException, DatatypeConfigurationException {

        try (OutputStream os = XAUtil.createFileOS(session, zipStudy, true);
                ZipOutputStream tmpZipOutputStream = new ZipOutputStream(os);
                DicomOutputStream outStream = new DicomOutputStream(tmpZipOutputStream)) {

            outStream.setAutoFinish(false);
            MutableBoolean firstElementInSeries = new MutableBoolean(true);

            File[] studyFolder = XAUtil.listFiles(session, studypath);
            for (File seriesFile : studyFolder) {
                File[] serieFolder = XAUtil.listFiles(session, seriesFile);
                log.debug("Creazione ZIP: processo la serie " + seriesFile);
                firstElementInSeries.setValue(true);

                for (File instance : serieFolder) {
                    ZipEntry tmpEntry = new ZipEntry(seriesFile.getName() + "/" + instance.getName());
                    tmpZipOutputStream.putNextEntry(tmpEntry);

                    try (InputStream is = XAUtil.createFileIS(session, instance, false);
                            DicomInputStream dis = new DicomInputStream(new BufferedInputStream(is, BUFFER_SIZE))) {

                        dis.setHandler(new StopTagInputHandler(Tag.PixelData));
                        DicomObject dcmObj = dis.readDicomObject();
                        setDicomDatiSpec(dcmObj, datiSpecificiDICOM, firstElementInSeries);
                    }

                    try (InputStream is = XAUtil.createFileIS(session, instance, false);
                            DicomInputStream dis = new DicomInputStream(new BufferedInputStream(is, BUFFER_SIZE))) {

                        byte[] buffer = new byte[BUFFER_SIZE];
                        IOUtils.copyLarge(dis, outStream, buffer);
                    }
                }
            }
        }
    }

    private void setDicomDatiSpec(DicomObject dcmObj, DatiSpecificiType datiSpecificiDICOM,
            MutableBoolean firstElementInSeries) throws DatatypeConfigurationException {

        SpecificCharacterSet cs = calculateSpecificCharacterSet(dcmObj);

        if (firstElementInSeries.isTrue()) {
            mergeParams(dcmObj, cs, datiSpecificiDICOM);
            firstElementInSeries.setValue(false);
        }
        if (datiSpecificiDICOM.getModalityInStudyList() == null) {
            datiSpecificiDICOM.setModalityInStudyList(new ModalityInStudyList());
        }
        addAttribute(datiSpecificiDICOM.getModalityInStudyList().getModalityInStudy(),
                getDicomValue(dcmObj, cs, Tag.Modality));
        if (datiSpecificiDICOM.getSOPClassList() == null) {
            datiSpecificiDICOM.setSOPClassList(new SOPClassList());
        }
        addAttribute(datiSpecificiDICOM.getSOPClassList().getSOPClass(), getDicomValue(dcmObj, cs, Tag.SOPClassUID));
    }

    /**
     * Effettuo il merge dei parametri che non sono stati ancora settati nei datiSpecificiDICOM
     *
     * @param dcmObj
     *            Oggetto dicom di origine
     * @param cs
     *            CharacterSet
     * @param datiSpecificiDICOM
     *            dati specifici da aggiornare
     *
     * @throws DatatypeConfigurationException
     */
    private void mergeParams(DicomObject dcmObj, SpecificCharacterSet cs, DatiSpecificiType datiSpecificiDICOM)
            throws DatatypeConfigurationException {
        if (datiSpecificiDICOM.getStudyDate() == null) {
            Date studyDate = dcmObj.getDate(Tag.StudyDate);
            Date studyTime = dcmObj.getDate(Tag.StudyTime);
            if (studyDate != null && studyTime != null) {
                Calendar calendarDate = Calendar.getInstance();
                calendarDate.setTime(studyDate);
                Calendar calendarTime = Calendar.getInstance();
                calendarTime.setTime(studyTime);
                calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
                calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
                calendarDate.set(Calendar.SECOND, calendarTime.get(Calendar.SECOND));
                calendarDate.set(Calendar.MILLISECOND, calendarTime.get(Calendar.MILLISECOND));
                GregorianCalendar combined = new GregorianCalendar();
                combined.setTime(calendarDate.getTime());
                XMLGregorianCalendar studyDateTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(combined);
                datiSpecificiDICOM.setStudyDate(studyDateTime);
            }
        }
        if (datiSpecificiDICOM.getAccessionNumber() == null) {
            datiSpecificiDICOM.setAccessionNumber(getDicomValue(dcmObj, cs, Tag.AccessionNumber));// !=null?getDicomValue(dcmObj,Tag.AccessionNumber):"NON_VALORIZZATO");
        }
        if (datiSpecificiDICOM.getInstitutionName() == null) {
            datiSpecificiDICOM.setInstitutionName(getDicomValue(dcmObj, cs, Tag.InstitutionName));
        }
        if (datiSpecificiDICOM.getReferringPhysicianName() == null) {
            datiSpecificiDICOM.setReferringPhysicianName(getDicomValue(dcmObj, cs, Tag.ReferringPhysicianName));
        }
        if (datiSpecificiDICOM.getStudyDescription() == null) {
            datiSpecificiDICOM.setStudyDescription(getDicomValue(dcmObj, cs, Tag.StudyDescription));
        }
        // if (datiSpecificiDICOM.getPatientName() == null) {
        // datiSpecificiDICOM.setPatientName(getDicomValue(dcmObj, cs, Tag.PatientName) != null ? getDicomValue(dcmObj,
        // cs, Tag.PatientName) : "");
        // }
        if (datiSpecificiDICOM.getPatientName() == null) {
            String patientName = getDicomValue(dcmObj, cs, Tag.PatientName) != null
                    ? getDicomValue(dcmObj, cs, Tag.PatientName) : "";
            if (patientName.isEmpty() && replaceEmptyTag) {
                datiSpecificiDICOM.setPatientName(DPIConstants.NON_TRASMESSO_DA_PACS);
                log.warn("Il tag PatientName non è stato trasmesso dal PACS");
            } else {
                datiSpecificiDICOM.setPatientName(patientName);
            }
        }
        // if (datiSpecificiDICOM.getPatientId() == null) {
        // datiSpecificiDICOM.setPatientId(getDicomValue(dcmObj, cs, Tag.PatientID) != null ? getDicomValue(dcmObj, cs,
        // Tag.PatientID) : "");
        // }
        if (datiSpecificiDICOM.getPatientId() == null) {
            String patientId = getDicomValue(dcmObj, cs, Tag.PatientID) != null
                    ? getDicomValue(dcmObj, cs, Tag.PatientID) : "";
            if (patientId.isEmpty() && replaceEmptyTag) {
                datiSpecificiDICOM.setPatientId(DPIConstants.NON_TRASMESSO_DA_PACS);
                log.warn("Il tag PatientID non è stato trasmesso dal PACS");
            } else {
                datiSpecificiDICOM.setPatientId(patientId);
            }
        }
        if (datiSpecificiDICOM.getPatientIdIssuer() == null) {
            datiSpecificiDICOM.setPatientIdIssuer(getDicomValue(dcmObj, cs, Tag.IssuerOfPatientID));
        }
        if (datiSpecificiDICOM.getPatientBirthDate() == null) {
            Date patientBirthDate = dcmObj.getDate(Tag.PatientBirthDate);
            if (patientBirthDate != null) {
                GregorianCalendar calendarDate = new GregorianCalendar();
                calendarDate.setTime(patientBirthDate);
                // datiSpecificiDICOM.setPatientBirthDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendarDate));
                datiSpecificiDICOM.setPatientBirthDate(patientBirthDate);
            }
        }
        if (datiSpecificiDICOM.getPatientSex() == null) {
            datiSpecificiDICOM.setPatientSex(getDicomValue(dcmObj, cs, Tag.PatientSex) != null
                    ? getDicomValue(dcmObj, cs, Tag.PatientSex).toUpperCase() : null);
        }

        // if (datiSpecificiDICOM.getStudyInstanceUID() == null) {
        // datiSpecificiDICOM.setStudyInstanceUID(getDicomValue(dcmObj, cs, Tag.StudyInstanceUID));
        // }
        if (datiSpecificiDICOM.getStudyInstanceUID() == null) {
            String studyInstanceUID = getDicomValue(dcmObj, cs, Tag.StudyInstanceUID);
            if (studyInstanceUID.isEmpty() && replaceEmptyTag) {
                datiSpecificiDICOM.setStudyInstanceUID(DPIConstants.NON_TRASMESSO_DA_PACS);
                log.warn("Il tag StudyInstanceUID non è stato trasmesso dal PACS");
            } else {
                datiSpecificiDICOM.setStudyInstanceUID(studyInstanceUID);
            }
        }
        if (datiSpecificiDICOM.getStudyID() == null) {
            datiSpecificiDICOM.setStudyID(getDicomValue(dcmObj, cs, Tag.StudyID));
        }
    }

    private void addAttribute(List<String> attrList, String attribute) {
        if (attribute != null && !attrList.contains(attribute)) {
            attrList.add(attribute);
        }
    }

    public void setXaDiskNativeFS(XAFileSystem xaDiskNativeFS) {
        this.xaDiskNativeFS = xaDiskNativeFS;
    }

    public static String getDicomValue(DicomObject dcmObj, SpecificCharacterSet cs, int tag) {
        String value = null;
        DicomElement elem = dcmObj.get(tag);
        if (elem != null) {
            value = elem.getValueAsString(cs, Integer.MAX_VALUE);
            value = escapeControlCharacter(value, tag);
        }
        return value;
    }

    // metodo per sostituire caratteri di controllo con "?".
    // Filtra solo per tag in cui è possibile inserire valori a mano.
    public static String escapeControlCharacter(String stringToParse, int tag) {
        /*
         * Questi sono i tag sui quali l'utente può intervenire a mono. Comunicati da Cesena. Poiché il dpi ne considera
         * meno. La sostituzione viene fatta solo su quelli in DicomTagToFilter 0008 0080 Instituion_name 0008 1040
         * institution_department_name 0008 1030 study_description 0008 0090 referring_physician_name 0008 1050
         * attending_physician_name 0008 0081 institution_address 0008 0070 manufacturer 0008 1090
         * manufacturer_mode_name 0018 1030 protocol_name 0032 1060 requested_procedure_description 0040 0254
         * performed_procedure_step_description 0008 1010 station_name 0008 103e series_description
         */
        for (int i = 0; i < DPIConstants.DicomTagToFilter.length; i++) {
            if (DPIConstants.DicomTagToFilter[i] == tag) {
                StringBuilder sb = new StringBuilder();
                if (stringToParse != null) {
                    char[] c_arr = stringToParse.toCharArray();
                    for (int j = 0; j < c_arr.length; j++) {
                        char ch = c_arr[j];
                        // se trovo un carattere di controllo lo sostituisco con "?"
                        if (ch < 0x20) {
                            sb.append("?");
                            continue;
                        }
                        sb.append(ch);
                    }
                    String convertedString = sb.toString();
                    log.debug("Processo il tag '" + tag + "': stringa originale = '" + stringToParse
                            + "' stringa convertita = '" + convertedString + "'");
                    if (!stringToParse.equals(convertedString)) {
                        log.warn("Il tag '" + tag
                                + "' contiene un carattere di controllo ed è stato sostituito: stringa originale: '"
                                + stringToParse + "' stringa convertita: '" + convertedString + "'");
                        return convertedString;
                    }
                }
                // stringToParse = sb.toString();
            }
        }
        return stringToParse;
    }

    // public static String getDicomValue(DicomObject dcmObj, int tag) {
    // String value = null;
    // DicomElement elem = dcmObj.get(tag);
    // if (elem != null) {
    // SpecificCharacterSet cs = SpecificCharacterSet.valueOf(dcmObj.get(Tag.SpecificCharacterSet).getStrings(null,
    // false));
    // if(cs == null) {
    // cs = SpecificCharacterSet.valueOf(new String[]{""});
    // }
    // value = elem.getValueAsString(cs, Integer.MAX_VALUE);
    // }
    // return value;
    // }

    public SpecificCharacterSet calculateSpecificCharacterSet(DicomObject dcmObj) {
        SpecificCharacterSet cs = null;
        if (dcmObj.get(Tag.SpecificCharacterSet) != null) {
            cs = SpecificCharacterSet.valueOf(dcmObj.get(Tag.SpecificCharacterSet).getStrings(null, false));
        } else {
            cs = SpecificCharacterSet.valueOf(new String[] { "" });
        }
        return cs;
    }

}
