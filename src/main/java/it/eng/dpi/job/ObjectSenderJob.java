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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.bean.ObjectType;
import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.Util;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.InvioOggettoAsincrono;
import it.eng.sacerasi.ws.InvioOggettoAsincronoRisposta;

@Component
public class ObjectSenderJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("INVIO_OGGETTO");
    private static final Logger log = LoggerFactory.getLogger(ObjectSenderJob.class);

    private static final Boolean flFileCifrato = false;
    private static final Boolean flForzaAccettazione = false;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Resource
    @Qualifier("objectTypes")
    private List<ObjectType> objectTypes;

    private File warnFolder;
    private File outFolder;

    private InvioOggettoAsincrono client;

    @PostConstruct
    public void postConstruct() {
        // outFolder = new File(ctx.getWorkingPath() + DPIConstants.OUT_FOLDER);
        // warnFolder = new File(ctx.getWorkingPath() +
        // DPIConstants.WARN_FOLDER);
        outFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.OUT_FOLDER);
        warnFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.WARN_FOLDER);
    }

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            if (client == null) {
                client = init(ctx.getWsdlInvioOggettoUrl(), InvioOggettoAsincrono.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                // File newFolderM = new File(ctx.getWorkingPath() +
                // DPIConstants.NEW_FOLDER, DPIConstants.CMOVE_DIR);
                // File newFolderC = new File(ctx.getWorkingPath() +
                // DPIConstants.NEW_FOLDER, DPIConstants.CSTORE_DIR);
                File newFolderM = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.NEW_FOLDER,
                        DPIConstants.CMOVE_DIR);
                File newFolderC = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.NEW_FOLDER,
                        DPIConstants.CSTORE_DIR);
                processNewFolder(newFolderM, new Boolean(ctx.getFlForzaWarningCMove()), DPIConstants.CMOVE_DIR);
                processNewFolder(newFolderC, false, DPIConstants.CSTORE_DIR);

                sendGenericObject();

                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void sendGenericObject() throws IOException, XAGenericException {
        File newFolder;
        File outFolder;
        final boolean flForzaWarning = false;

        // determino l'insieme dei tipi oggetto gestiti dal DPI (tipoObj.n)
        for (ObjectType objectType : objectTypes) {
            log.debug("-" + objectType.toString());
            // determino le cartelle <produttore>-<raccoglitore> presenti in V1_new
            newFolder = new File(
                    ctx.getWorkingPath() + File.separatorChar + objectType.getObjType() + DPIConstants.NEW_FOLDER);
            outFolder = new File(
                    ctx.getWorkingPath() + File.separatorChar + objectType.getObjType() + DPIConstants.OUT_FOLDER);
            // ricavo i file
            File[] folders = XAUtil.listFilesNewTX(xaDiskNativeFS, newFolder);
            Counters counters = new Counters(folders.length, newFolder.getName());
            // ciclo su ogni cartella folder=<produttore>-<raccoglitore>
            for (File folder : folders) {
                String cdKeyObject = objectType.getObjType() + "-" + folder.getName();
                log.debug("-" + folder.getName());
                // cerco il file xml di versamento a Preingest
                File xmlFile = null;
                File[] files = XAUtil.listFilesNewTX(xaDiskNativeFS, folder);
                for (File file : files) {
                    if (file.getName().endsWith(".xml")) {
                        xmlFile = new File(folder, file.getName());
                        break;
                    }
                }
                InvioOggettoAsincronoRisposta risposta = null;
                String xml = FileUtils.readFileToString(xmlFile, "UTF-8");
                String nomeTipoOggetto = objectType.getObjType();
                risposta = callWSInvioOggettoPreIngest(cdKeyObject, xml, flForzaWarning, nomeTipoOggetto);
                elaborateResponseGenericObj(risposta, folder, outFolder, xmlFile, counters);
            }
            audit.info("TIPO OGGETTO: " + objectType.getObjType() + " | " + counters.toString());
        }
    }

    private void processNewFolder(File newFolder, Boolean flForzaWarning, String origFolder)
            throws IOException, XAGenericException {
        File[] folders = XAUtil.listFilesNewTX(xaDiskNativeFS, newFolder);
        List<File> folder = new ArrayList<File>();
        for (File f : folders) {
            if (f.getName().endsWith(".zip")) {
                folder.add(f);
            }
        }
        Counters counters = new Counters(folder.size(), newFolder.getName());
        for (File zipStudy : folder) {
            String globalHash = zipStudy.getName().substring(0, zipStudy.getName().length() - 4);
            File xmlFile = new File(newFolder, globalHash + ".xml");
            InvioOggettoAsincronoRisposta risposta = null;

            String xml = FileUtils.readFileToString(xmlFile, "UTF-8");
            String nomeTipoOggetto = DPIConstants.WS_NM_TIPO_OBJECT;

            risposta = callWSInvioOggettoPreIngest(globalHash, xml, flForzaWarning, nomeTipoOggetto);

            elaborateResponse(risposta, zipStudy, xmlFile, counters, origFolder);
        }
        audit.info(counters.toString());
    }

    private InvioOggettoAsincronoRisposta callWSInvioOggettoPreIngest(String cdKeyObject, String xml,
            Boolean flForzaWarning, String nomeTipoOggetto) throws MalformedURLException {
        log.debug("Invoco il WS: InvioOggettoAsincrono - URL: " + ctx.getWsdlInvioOggettoUrl());
        log.debug("Parametri: nmAmbiente: " + ctx.getNmAmbiente() + "\nnmVersatore: " + ctx.getNmVersatore()
                + "\ncdPassword: " + "\ncdKeyObject: " + cdKeyObject + "\nflForzaWarning:" + flForzaWarning + "\nxml:"
                + StringUtils.abbreviate(xml, 10000));
        String motivazione = "";

        return client.invioOggettoAsincrono(ctx.getNmAmbiente(), ctx.getNmVersatore(), cdKeyObject, nomeTipoOggetto,
                flFileCifrato, flForzaWarning, flForzaAccettazione, motivazione, DPIConstants.VERSIONE_XML_PREINGEST,
                xml);
    }

    private void elaborateResponse(InvioOggettoAsincronoRisposta risposta, File zipObj, File xmlFile, Counters counters,
            String origFolder) {
        if (risposta != null) {
            log.info("RISPOSTA DEL WS: " + risposta.getCdEsito() + " "
                    + ((risposta.getCdErr() != null) ? risposta.getCdErr() + " " + risposta.getDsErr() : ""));
            Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                switch (risposta.getCdEsito()) {
                case OK:
                    XAUtil.moveFile(xaSession, zipObj, new File(outFolder, zipObj.getName()));
                    XAUtil.deleteFile(xaSession, xmlFile);
                    break;
                case KO:
                    /*
                     * AGGIUNTA LA SEGUENTE LOGICA per fixare un eventuale problema di timeout nella risposta di
                     * preinget. Se infatti il dpi va in timeout il preingest potrebbe cmq aver committato ma il dpi
                     * avrebbe rollbackato ed un 2° invio da parte del DPI eliminerebbe il file (la risposta sarebbe KO)
                     *
                     * Se codErr == PING-SENDOBJ-OBJ-003 (Oggetto già in warning) eseguire la logica WARN (ma solo se i
                     * file non esistono già nella destinazione) Se codErr == PING-SENDOBJ-OBJ-010 (oggetto già in
                     * attesa file) eseguire la logica OK (ma solo se i file non esistono già nella destinazione)
                     *
                     * Se codErr == 666 rollback
                     */
                    if (risposta.getCdErr() != null) {
                        switch (risposta.getCdErr()) {
                        case "PING-SENDOBJ-OBJ-003": // (Oggetto già in warning)
                            if (origFolder.equals(DPIConstants.CSTORE_DIR)) {
                                log.info(
                                        "Oggetto già in stato WARNING ma lo studio proviene da una CSTORE, elimino i file .. ");
                                XAUtil.deleteFile(xaSession, zipObj);
                                XAUtil.deleteFile(xaSession, xmlFile);
                            } else {
                                if (!XAUtil.fileExists(xaSession, new File(warnFolder, zipObj.getName()))
                                        && !XAUtil.fileExists(xaSession, new File(warnFolder, xmlFile.getName()))) {
                                    log.info("Oggetto già in stato WARNING sposto nella cartella warning i file .. ");
                                    XAUtil.moveFile(xaSession, zipObj, new File(warnFolder, zipObj.getName()));
                                    XAUtil.moveFile(xaSession, xmlFile, new File(warnFolder, xmlFile.getName()));
                                } else {
                                    log.info(
                                            "Oggetto già in stato WARNING ma sono già presenti nella cartella warning gli stessi file .. elimino i file");
                                    XAUtil.deleteFile(xaSession, zipObj);
                                    XAUtil.deleteFile(xaSession, xmlFile);
                                }
                            }
                            break;
                        case "PING-SENDOBJ-OBJ-010": // (oggetto già in attesa
                                                     // file)
                            if (origFolder.equals(DPIConstants.CMOVE_DIR)) {
                                log.info(
                                        "Oggetto già in stato IN_ATTESA_FILE ma lo studio proviene da una CMOVE, elimino i file .. ");
                                XAUtil.deleteFile(xaSession, zipObj);
                                XAUtil.deleteFile(xaSession, xmlFile);
                            } else {
                                if (!XAUtil.fileExists(xaSession, new File(outFolder, zipObj.getName()))) {
                                    log.info(
                                            "Oggetto già in stato IN_ATTESA_FILE sposto nella cartella out il file zip .. ");
                                    XAUtil.moveFile(xaSession, zipObj, new File(outFolder, zipObj.getName()));
                                    XAUtil.deleteFile(xaSession, xmlFile);
                                } else {
                                    log.info(
                                            "Oggetto già in stato IN_ATTESA_FILE ma sono già presenti nella cartella out gli stessi file .. elimino i file");
                                    XAUtil.deleteFile(xaSession, zipObj);
                                    XAUtil.deleteFile(xaSession, xmlFile);
                                }
                            }
                            break;
                        case "666":
                            // NON FARE NIENTE
                            break;
                        default: // Generico KO .. cancello
                            XAUtil.deleteFile(xaSession, zipObj);
                            XAUtil.deleteFile(xaSession, xmlFile);
                            break;
                        }
                    } else { // Generico KO .. cancello senza codice errore (NON
                             // DOVREBBE MAI CAPITARE, il preingest deve
                             // sempre tornare un codice in caso di KO)
                        XAUtil.deleteFile(xaSession, zipObj);
                        XAUtil.deleteFile(xaSession, xmlFile);
                    }
                    break;
                case WARN:
                    XAUtil.moveFile(xaSession, zipObj, new File(warnFolder, zipObj.getName()));
                    XAUtil.moveFile(xaSession, xmlFile, new File(warnFolder, xmlFile.getName()));
                    break;
                }
                xaSession.commit();
                counters.increment(risposta.getCdEsito());
            } catch (Exception e) {
                try {
                    log.error("Si è verificato un errore durante la transazione.", e);
                    xaSession.rollback();
                } catch (NoTransactionAssociatedException e1) {
                    log.error(
                            "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                            e1);
                }
                counters.incrError();
            }
        }
    }

    /**
     * Elabora la risposta ottenuta dall'invocazione di Preingest
     *
     * @param risposta
     *            Risposta ritornata da Sacer Preingest
     * @param objFolder
     *            Cartella <produttore>-<raccoglitore>
     * @param xmlFile
     *            file xml contenuto in objFolder
     * @param xmlFile
     * @param counters
     *            contatore per gestire l'esito della risposta
     */
    private void elaborateResponseGenericObj(InvioOggettoAsincronoRisposta risposta, File objFolder, File outFolder,
            File xmlFile, Counters counters) {
        if (risposta != null) {
            log.info("RISPOSTA DEL WS: " + risposta.getCdEsito() + " "
                    + ((risposta.getCdErr() != null) ? risposta.getCdErr() + " " + risposta.getDsErr() : ""));
            Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                switch (risposta.getCdEsito()) {
                case OK:
                    XAUtil.deleteFile(xaSession, xmlFile);
                    XAUtil.moveFile(xaSession, objFolder, new File(outFolder, objFolder.getName()));
                    break;
                case KO:
                    /*
                     * AGGIUNTA LA SEGUENTE LOGICA per fixare un eventuale problema di timeout nella risposta di
                     * preinget. Se infatti il dpi va in timeout il preingest potrebbe cmq aver committato ma il dpi
                     * avrebbe rollbackato ed un 2° invio da parte del DPI eliminerebbe il file (la risposta sarebbe KO)
                     *
                     * Se codErr == PING-SENDOBJ-OBJ-010 (oggetto già in attesa file) eseguire la logica OK (ma solo se
                     * i file non esistono già nella destinazione)
                     *
                     * Se codErr == 666 rollback
                     */
                    if (risposta.getCdErr() != null) {
                        switch (risposta.getCdErr()) {
                        case "PING-SENDOBJ-OBJ-010": // (oggetto già in attesa file)
                            if (!XAUtil.fileExists(xaSession, new File(outFolder, objFolder.getName()))) {
                                log.info(
                                        "Oggetto già in stato IN_ATTESA_FILE sposto nella cartella out il/i file ed elimino l'xml .. ");
                                XAUtil.deleteFile(xaSession, xmlFile);
                                XAUtil.moveFile(xaSession, objFolder, new File(outFolder, objFolder.getName()));
                            } else {
                                log.info(
                                        "Oggetto già in stato IN_ATTESA_FILE ma sono già presenti nella cartella out gli stessi file .. elimino i file");
                                Util.rimuoviFileRicorsivamente(objFolder, xaSession);
                            }

                            break;
                        case "666":
                            // NON FARE NIENTE
                            break;
                        default: // Generico KO .. cancello
                            Util.rimuoviFileRicorsivamente(objFolder, xaSession);
                            break;
                        }
                    } else { // Generico KO .. cancello senza codice errore (NON
                             // DOVREBBE MAI CAPITARE, il preingest deve
                             // sempre tornare un codice in caso di KO)
                        Util.rimuoviFileRicorsivamente(objFolder, xaSession);
                    }
                    break;
                }
                xaSession.commit();
                counters.increment(risposta.getCdEsito());
            } catch (Exception e) {
                try {
                    log.error("Si è verificato un errore durante la transazione.", e);
                    xaSession.rollback();
                } catch (NoTransactionAssociatedException e1) {
                    log.error(
                            "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                            e1);
                }
                counters.incrError();
            }

        }

    }

    private final class Counters {
        public Counters(int total, String foldername) {
            super();
            this.total = total;
            this.foldername = foldername;
        }

        private int total;
        private int success;
        private int fail;
        private int warn;
        private int error;
        private String foldername;

        public void increment(EsitoServizio cdEsito) {
            switch (cdEsito) {
            case OK:
                success++;
                break;
            case KO:
                fail++;
                break;
            case WARN:
                warn++;
                break;
            }

        }

        public void incrError() {
            error++;
        }

        @Override
        public String toString() {
            return "CARTELLA: " + foldername + " | TOTALI: " + total + " | OK: " + success + " | KO: " + fail
                    + " | WARN: " + warn + " | ERRORE: " + error;
        }
    }

}
