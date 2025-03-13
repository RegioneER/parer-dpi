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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.bean.ObjectType;
import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.JAXBSingleton;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.UnexpectedPropertyException;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.xml.invioasync.ChiaveType;
import it.eng.sacerasi.ws.xml.invioasync.FileType;
import it.eng.sacerasi.ws.xml.invioasync.ListaUnitaDocumentarieType;
import it.eng.sacerasi.ws.xml.invioasync.ProfiloUnitaDocumentariaType;
import it.eng.sacerasi.ws.xml.invioasync.UnitaDocumentariaType;
import it.eng.sacerasi.ws.xml.invioasync.UnitaDocumentariaType.Files;

@Component
public class GenericObjectCreatorJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("CREA_OGGETTO_GENERICO");
    private static final Logger log = LoggerFactory.getLogger(GenericObjectCreatorJob.class);

    @Resource
    @Qualifier("objectTypes")
    private List<ObjectType> objectTypes;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private JAXBSingleton jaxbSingleton;

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            int numRaccProc = process();
            audit.info("Il job ha terminato l'esecusione. Sono state processate " + numRaccProc
                    + " cartelle raccoglitore");
            audit.info(DPIConstants.AUDIT_JOB_STOP);
        } catch (Exception e) {
            log.error("Errore durante l'esecuzione del job", e);
            audit.info(DPIConstants.AUDIT_JOB_ERROR + ": " + e.getMessage());
        }
    }

    public int process() throws Exception {
        int numRaccProc = 0;
        for (ObjectType objectType : objectTypes) {
            log.trace(objectType.toString());
            File workFolder = new File(ctx.getWorkingPath() + File.separatorChar + objectType.getObjType());
            String tiCalcRegistroUd = objectType.getTiCalcRegistroUd(); // regola per registro ud
                                                                        // (cd_registro_unita_doc)
            String tiCalcAnnoUd = objectType.getTiCalcAnnoUd(); // regola per anno ud (aa_unita_doc)
            String tiCalcKeyUd = objectType.getTiCalcKeyUd(); // regola per numero ud (cd_key_unita_doc)
            String tiCalcProfiloUd = objectType.getTiCalcProfiloUd();
            boolean flCreaZip = objectType.getFlCreaZip();
            File[] produttori = XAUtil.listFilesNewTX(xaDiskNativeFS, workFolder);
            for (File produttore : produttori) {
                if (!(produttore.getName().equals(DPIConstants.V1_NEW)
                        || produttore.getName().equals(DPIConstants.V2_OUT))) {
                    log.debug("processo il produttore: " + produttore.toString());
                    File[] raccoglitori = XAUtil.listFilesNewTX(xaDiskNativeFS, produttore);
                    Session session = null;
                    for (File raccoglitore : raccoglitori) {
                        try {
                            session = xaDiskNativeFS.createSessionForLocalTransaction();
                            log.debug("trovato raccoglitore: " + raccoglitore.toString());
                            File v1New = new File(workFolder + DPIConstants.NEW_FOLDER);
                            File prodRaccDir = new File(v1New, produttore.getName() + "-" + raccoglitore.getName());
                            if (!XAUtil.fileExistsAndIsDirectory(session, prodRaccDir)) {
                                log.debug("la cartella " + prodRaccDir + " non esiste... la creo");
                                XAUtil.createDirectory(session, prodRaccDir);

                                File xmlFile = new File(prodRaccDir, raccoglitore.getName() + ".xml");
                                ChiaveType chiaveType = buildChiaveType(tiCalcRegistroUd, tiCalcAnnoUd, tiCalcKeyUd,
                                        raccoglitore.getName(), produttore.getName());
                                Files filesType = buildFiles(objectType.getTipoFile());
                                File[] files = XAUtil.listFilesNewTX(xaDiskNativeFS, raccoglitore);
                                File propertyFile = searchPropertyFile(files);
                                try {
                                    createXmlPingVers(session, xmlFile, chiaveType, filesType, propertyFile,
                                            tiCalcProfiloUd, objectType.getObjType(), produttore.getName(),
                                            raccoglitore.getName());
                                } catch (UnexpectedPropertyException e) {
                                    log.error(
                                            "Errore durante la lettura dei valori dal file di property per il raccoglitore: '"
                                                    + raccoglitore.getName()
                                                    + "'. Eseguo rollback e passo al prossimo raccoglitore. ",
                                            e);
                                    session.rollback();
                                    continue;
                                }
                                if (flCreaZip) {
                                    // creo lo zip in V1_new prendendo i file dentro la cartella raccoglitore
                                    final File zipFile = new File(prodRaccDir, raccoglitore.getName() + ".zip");
                                    createZip(session, zipFile, raccoglitore);
                                } else {
                                    // non creo lo zip. Sposto i file da raccoglitore a V1_new
                                    for (File fileToMove : files) {
                                        if (!fileToMove.getName().endsWith(DPIConstants.PROPERTY_FILE_EXT)) {
                                            final File destDir = new File(prodRaccDir, fileToMove.getName());
                                            XAUtil.moveFile(session, fileToMove, destDir);
                                        }
                                    }
                                }
                            } else {
                                // la cartella v1New esiste già. Segnalo l'accaduto e passo avanti.
                                // La cartella sarà elaborata alla prossima esecuzione del job.
                                audit.warn("Nel file system del DPI, è già definita la cartella " + prodRaccDir);
                            }
                            if (session.fileExistsAndIsDirectory(raccoglitore)) {
                                File[] filesToDelete = XAUtil.listFiles(session, raccoglitore);
                                for (File fileToDelete : filesToDelete) {
                                    XAUtil.deleteFile(session, fileToDelete);
                                }
                                XAUtil.deleteFile(session, raccoglitore);
                            }
                            session.commit();
                            numRaccProc++;
                        } catch (Exception e) {
                            try {
                                if (!(e instanceof NoTransactionAssociatedException)) {
                                    log.error("Si è verificato un errore durante la transazione." + e);
                                    session.rollback();
                                }
                            } catch (NoTransactionAssociatedException e1) {
                                log.error(
                                        "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                                        e1);
                            }
                            throw e;
                        }
                    }
                }
            }
        }
        return numRaccProc;
    }

    /*
     * Costruisco la busta <ProfiloUnitaDocumentaria> con i tag <Oggetto> e <Data>. Se il file di property è presente
     * <Oggetto> e <Data> li prendo rispettivamente da ProfiloUnitaDocumentaria.Oggetto e ProfiloUnitaDocumentaria.Data.
     * Altrimenti se nel file di configurazione ho tipoObj.tiCalcProfiloUd.n=CALC_1 allora il tag Oggetto sarà uguale a
     * "<tipoObj> del <produttore>" e il tag Data sarà uguale a <raccoglitore>
     *
     */
    private void createXmlPingVers(Session session, File xmlDestFile, ChiaveType chiave, Files files, File propertyFile,
            String tiCalcProfiloUd, String tipoOggetto, String produttore, String raccoglietore) throws Exception {
        Properties prop = null;
        // creo la busta <UnitaDocumentaria> e gli setto <Chiave> e <Files>
        UnitaDocumentariaType uniDoc = buildUnitaDocumentaria(chiave, files);
        ProfiloUnitaDocumentariaType profiloUnitaDocumentariaTag = null;
        // creo la busta <ProfiloUnitaDocumentaria>
        if (propertyFile != null) {
            prop = retrieveItemFromPropertyFile(propertyFile);
            profiloUnitaDocumentariaTag = createProfiloUnitaDocumentariaFromProperty(prop);
            // creo la busta <DatiSpecifici>
            Document docDatiSpec = buildDatiSpecXml(prop);
            JAXBElement<Object> datiSpecificiJAXB = new it.eng.sacerasi.ws.xml.invioasync.ObjectFactory()
                    .createUnitaDocumentariaTypeDatiSpecifici(docDatiSpec.getDocumentElement());
            uniDoc.setDatiSpecifici(datiSpecificiJAXB);
        } else if (tiCalcProfiloUd.equals(DPIConstants.CALC_1)) {
            profiloUnitaDocumentariaTag = createProfiloUnitaDocumentariaFromCalc1(tipoOggetto, produttore,
                    raccoglietore);
        }
        // setto l'oggetto profiloUnitaDocumentariaTag al padre UnitaDocumentariaType solo se oggetto e data sono
        // presenti
        if (profiloUnitaDocumentariaTag != null && (profiloUnitaDocumentariaTag.getOggetto() != null
                || profiloUnitaDocumentariaTag.getData() != null)) {
            uniDoc.setProfiloUnitaDocumentaria(profiloUnitaDocumentariaTag);
        }
        ListaUnitaDocumentarieType xmlVersamentoPreI = new ListaUnitaDocumentarieType();
        xmlVersamentoPreI.setVersione(DPIConstants.VERSIONE_XML_PREINGEST);
        xmlVersamentoPreI.getUnitaDocumentaria().add(uniDoc);
        OutputStream os = null;
        try {
            os = XAUtil.createFileOS(session, xmlDestFile, true);
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

    private ProfiloUnitaDocumentariaType createProfiloUnitaDocumentariaFromProperty(Properties prop) throws Exception {
        // la busta ProfiloUnitaDocumentaria deve contenere i tag Oggetto e Data.
        ProfiloUnitaDocumentariaType profiloUD = null;
        profiloUD = new ProfiloUnitaDocumentariaType();
        // ricavo i parametri che mi servono per costruire ProfiloUnitaDocumentariaType
        String oggettoProfilo = prop.getProperty(DPIConstants.PROFILO_UD_OGGETTO_PROPERTY_VALUE);
        log.debug("Trovato Oggetto '" + oggettoProfilo + "'");
        profiloUD.setOggetto(oggettoProfilo);
        String dataProfilo = prop.getProperty(DPIConstants.PROFILO_UD_DATA_PROPERTY_VALUE);
        log.debug("Trovato Data '" + dataProfilo + "'");
        if (dataProfilo != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dataProfiloUd = formatDataProfilo(dataProfilo, formatter);
            profiloUD.setData(dataProfiloUd);
        }
        return profiloUD;
    }

    private ProfiloUnitaDocumentariaType createProfiloUnitaDocumentariaFromCalc1(String tipoOggetto, String produttore,
            String raccoglietore) throws Exception {
        // la busta ProfiloUnitaDocumentaria deve contenere i tag Oggetto e Data.
        ProfiloUnitaDocumentariaType profiloUD = null;
        profiloUD = new ProfiloUnitaDocumentariaType();
        String oggettoProfilo = tipoOggetto + " del " + produttore;
        log.debug("costruito Oggetto '" + oggettoProfilo + "'");
        profiloUD.setOggetto(oggettoProfilo);
        String dataProfilo = raccoglietore;
        log.debug("Trovato Data '" + dataProfilo + "'");
        if (dataProfilo != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            Date dataProfiloUd = formatDataProfilo(dataProfilo, formatter);
            profiloUD.setData(dataProfiloUd);
        }
        return profiloUD;
    }

    private Date formatDataProfilo(String dataProfilo, SimpleDateFormat formatter) throws UnexpectedPropertyException {
        Date dataProfiloUd;
        try {
            dataProfiloUd = formatter.parse(dataProfilo);
            log.debug("Data dopo parse " + dataProfilo);
            if (dataProfiloUd != null) {
                GregorianCalendar calendarDate = new GregorianCalendar();
                calendarDate.setTime(dataProfiloUd);
            }
        } catch (Exception e) {
            throw new UnexpectedPropertyException(
                    "Errore nella costruzione della busta ProfiloUnitaDocumentaria: data profilo letta da file property non è corretta? "
                            + e);
        }
        return dataProfiloUd;
    }

    // private ProfiloUnitaDocumentariaType createProfiloUnitaDocumentariaFromProperty(Properties prop) throws Exception
    // {
    // //la busta ProfiloUnitaDocumentaria deve contenere i tag Oggetto e Data.
    // ProfiloUnitaDocumentariaType profiloUD = null;
    // profiloUD = new ProfiloUnitaDocumentariaType();
    // //ricavo i parametri che mi servono per costruire ProfiloUnitaDocumentariaType
    // String oggettoProfilo = prop.getProperty(DPIConstants.PROFILO_UD_OGGETTO_PROPERTY_VALUE);
    // log.debug("Trovato Oggetto '" + oggettoProfilo + "'");
    // profiloUD.setOggetto(oggettoProfilo);
    // String dataProfilo = prop.getProperty(DPIConstants.PROFILO_UD_DATA_PROPERTY_VALUE);
    // log.debug("Trovato Data '" + dataProfilo + "'");
    // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    // Date dataProfiloUd;
    // try {
    // dataProfiloUd = formatter.parse(dataProfilo);
    // log.debug("Data dopo parse " + dataProfilo);
    // if (dataProfiloUd != null) {
    // GregorianCalendar calendarDate = new GregorianCalendar();
    // calendarDate.setTime(dataProfiloUd);
    // profiloUD.setData(dataProfiloUd);
    // }
    // } catch (Exception e) {
    // throw new UnexpectedPropertyException("Errore nella costruzione della busta ProfiloUnitaDocumentaria: la data
    // profilo letta dal file di property non è corretta " + e);
    // }
    // return profiloUD;
    // }

    // private ProfiloUnitaDocumentariaType createProfiloUnitaDocumentariaFromCalc1(String tipoOggetto, String
    // produttore, String raccoglietore) throws Exception {
    // //la busta ProfiloUnitaDocumentaria deve contenere i tag Oggetto e Data.
    // ProfiloUnitaDocumentariaType profiloUD = null;
    // profiloUD = new ProfiloUnitaDocumentariaType();
    // String oggettoProfilo = tipoOggetto + " del " + produttore;
    // log.debug("costruito Oggetto '" + oggettoProfilo + "'");
    // profiloUD.setOggetto(oggettoProfilo);
    // String dataProfilo = raccoglietore;
    // log.debug("Trovato Data '" + dataProfilo + "'");
    // SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    // Date dataProfiloUd;
    // try {
    // dataProfiloUd = formatter.parse(dataProfilo);
    // if (dataProfiloUd != null) {
    // GregorianCalendar calendarDate = new GregorianCalendar();
    // calendarDate.setTime(dataProfiloUd);
    // profiloUD.setData(dataProfiloUd);
    // }
    // } catch (Exception e) {
    // throw new UnexpectedPropertyException("Errore nella costruzione della busta ProfiloUnitaDocumentaria" + e);
    // }
    // return profiloUD;
    // }

    private Document buildDatiSpecXml(Properties prop) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // elemento root DatiSpecifici
        Document docDatiSpec = docBuilder.newDocument();
        Element rootElement = docDatiSpec.createElement(DPIConstants.DATI_SPECIFICI_TAG);
        docDatiSpec.appendChild(rootElement);

        Enumeration<Object> em = prop.keys();
        while (em.hasMoreElements()) {
            String propKey = (String) em.nextElement();
            log.debug("buildDatiSpecXml: " + propKey + " = '" + prop.get(propKey) + "'");
            String pattern = DPIConstants.DATI_SPECIFICI_PROPERTY_VALUE;
            if (propKey.startsWith(pattern)) {
                Element el = docDatiSpec.createElement(propKey.substring(pattern.length()));
                el.appendChild(docDatiSpec.createTextNode(prop.get(propKey).toString()));
                rootElement.appendChild(el);
            }
        }
        return docDatiSpec;
    }

    /**
     * Cerca tra i file in input il file di property.
     *
     * @param filesArray
     *            File tra i quali cercare quello di property
     *
     * @return Il file di property
     */
    private File searchPropertyFile(File[] filesArray) {
        File propertyFile = null;
        for (File file : filesArray) {
            if (file.getName().endsWith(DPIConstants.PROPERTY_FILE_EXT)) {
                propertyFile = file;
                break;
            }
        }
        return propertyFile;
    }

    private Properties retrieveItemFromPropertyFile(File propertyFile) throws UnexpectedPropertyException, IOException {
        log.debug("File .properties presente");
        Properties prop = new OrderedProperties();
        prop.load(new FileInputStream(propertyFile));
        log.debug("Elenco delle chiavi nel file property : " + prop.keySet().toString());
        log.debug("Elenco dei valori del file property : " + prop.values());
        checkItem(prop);
        return prop;
    }

    private void checkItem(Properties prop) throws UnexpectedPropertyException {
        String datiSpecPattern = DPIConstants.DATI_SPECIFICI_TAG + ".";
        String profiloUdOggettoPattern = DPIConstants.PROFILO_UD_OGGETTO_PROPERTY_VALUE;
        String profiloUdDataPattern = DPIConstants.PROFILO_UD_DATA_PROPERTY_VALUE;
        Enumeration<Object> em = prop.keys();
        while (em.hasMoreElements()) {
            String propKey = (String) em.nextElement();
            log.debug("checkItem: " + propKey + " = '" + prop.get(propKey) + "'");
            if (!propKey.startsWith(datiSpecPattern) && !propKey.startsWith(profiloUdOggettoPattern)
                    && !propKey.startsWith(profiloUdDataPattern)) {
                throw new UnexpectedPropertyException("Chiave non ammessa nel file di property: " + propKey);
            }
        }
    }

    /**
     * Crea un file zip
     *
     * @param session
     *            Sessione XADisk
     * @param zipFile
     *            File zip output
     * @param inputDirPath
     *            Directory contenente i file da inserire nell'archivio zip
     *
     * @throws XAGenericException
     * @throws IOException
     */
    private void createZip(Session session, File zipFile, File inputDirPath) throws XAGenericException, IOException {
        OutputStream os = XAUtil.createFileOS(session, zipFile, true);
        OutputStream tmpZipOutputStream = new ZipOutputStream(os);
        try {
            File[] filesToZip = XAUtil.listFiles(session, inputDirPath);
            log.debug("Creazione ZIP: processo la directory " + inputDirPath);
            for (File file : filesToZip) {
                ZipEntry tmpEntry = null;
                if (!file.getName().endsWith(".properties")) {
                    tmpEntry = new ZipEntry(file.getName());
                    ((ZipOutputStream) tmpZipOutputStream).putNextEntry(tmpEntry);
                    InputStream is = null;
                    try {
                        is = XAUtil.createFileIS(session, file, false);
                        IOUtils.copy(is, tmpZipOutputStream);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(tmpZipOutputStream);
        }
    }

    private ChiaveType buildChiaveType(String tiCalcRegistroUd, String tiCalcAnnoUd, String tiCalcKeyUd,
            String raccoglitoreName, String produttoreName) {
        // quando verranno introdotti nuovi tipi oggetto valutare come sostituire
        // PRODUTTORE, PRIMI_4_CRT e DA_5_A_8_CRT con espressioni regolari
        String cdRegistroUnitaDoc = null;
        // TODO: vedere se aumentare il controllo sulla variabile seguente. Potrebbe passare un -1 nel caso non venisse
        // settata?
        int aaUnitaDoc = -1;
        String cdKeyUnitaDoc = null;
        if (tiCalcRegistroUd.equals(DPIConstants.PRODUTTORE)) {
            cdRegistroUnitaDoc = produttoreName;
            log.debug("tiCalcRegistroUd = " + cdRegistroUnitaDoc);
        }
        if (tiCalcAnnoUd.equals(DPIConstants.PRIMI_4_CRT)) {
            aaUnitaDoc = Integer.parseInt((raccoglitoreName.substring(0, 4)));
            log.debug("tiCalcAnnoUd = " + aaUnitaDoc);
        }
        if (tiCalcKeyUd.equals(DPIConstants.DA_5_A_8_CRT)) {
            cdKeyUnitaDoc = raccoglitoreName.substring(4, 8);
            log.debug("tiCalcKeyUd = " + cdKeyUnitaDoc);
        }
        ChiaveType chiave = new ChiaveType();
        chiave.setNumero(cdKeyUnitaDoc);
        chiave.setAnno(aaUnitaDoc);
        chiave.setTipoRegistro(cdRegistroUnitaDoc);
        return chiave;
    }

    private Files buildFiles(String tipoFile) {
        Files files = new Files();
        FileType fileType = new FileType();
        fileType.setTipoFile(tipoFile);
        files.getFile().add(fileType);
        return files;
    }

    private UnitaDocumentariaType buildUnitaDocumentaria(ChiaveType chiave, Files files) {
        // creo l'oggetto UnitaDocumentariaType
        UnitaDocumentariaType uniDoc = new UnitaDocumentariaType();
        uniDoc.setChiave(chiave);
        uniDoc.setFiles(files);
        return uniDoc;
    }

}
