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
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;

import it.eng.dpi.bean.ObjectType;
import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.FTPUtil;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.FileDepositatoType;
import it.eng.sacerasi.ws.ListaFileDepositatoType;
import it.eng.sacerasi.ws.NotificaTrasferimento;
import it.eng.sacerasi.ws.NotificaTrasferimentoRisposta;

@Component
public class FTPTransfertJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("TRASFERIMENTO_FTP");
    private static final Logger log = LoggerFactory.getLogger(FTPTransfertJob.class);

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Resource
    @Qualifier("objectTypes")
    private List<ObjectType> objectTypes;

    private File outFolder;

    private NotificaTrasferimento client;

    @PostConstruct
    public void postConstruct() {
        // outFolder = new File(ctx.getWorkingPath() + DPIConstants.OUT_FOLDER);
        outFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.OUT_FOLDER);
    }

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            if (client == null) {
                client = init(ctx.getWsdlNotificaUrl(), NotificaTrasferimento.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                processOutFolder();
                processGenericObject();
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void processGenericObject() throws MalformedURLException, XAGenericException {
        File outFolder;
        for (ObjectType objectType : objectTypes) {
            log.debug(objectType.toString());
            outFolder = new File(
                    ctx.getWorkingPath() + File.separatorChar + objectType.getObjType() + DPIConstants.OUT_FOLDER);
            File[] folders = XAUtil.listFilesNewTX(xaDiskNativeFS, outFolder);
            Counters counters = new Counters(folders.length, outFolder.getName());
            // ciclo su ogni cartella folder=<produttore>-<raccoglitore>
            for (File folder : folders) {
                File[] files = XAUtil.listFilesNewTX(xaDiskNativeFS, folder);
                String cdKeyObject = objectType.getObjType() + "-" + folder.getName();
                // ciclo sui file contenuti nella cartella file=<produttore>-<raccoglitore>i-esimo
                boolean fileSended = true;

                ListaFileDepositatoType lista = new ListaFileDepositatoType();

                for (File file : files) {
                    if (!FTPUtil.sendFile(file, file.getName(), ctx.getFtpInputFolder() + "/" + cdKeyObject,
                            ctx.getFtpIP(), ctx.getFtpPort(), ctx.getFtpUser(), ctx.getFtpPassword(),
                            ctx.getSecureFtp())) {
                        fileSended = false;
                        break;
                    }
                    FileDepositatoType fileDepositato = new FileDepositatoType();
                    fileDepositato.setNmTipoFile(objectType.getTipoFile());
                    fileDepositato.setNmNomeFile(file.getName());
                    lista.getFileDepositato().add(fileDepositato);
                }
                if (fileSended) {
                    NotificaTrasferimentoRisposta risposta = callWSNotifyFileTransfert(folder, cdKeyObject, lista);
                    log.info("RISPOSTA DEL WS: " + risposta.getCdEsito() + " "
                            + ((risposta.getCdErr() != null) ? risposta.getCdErr() + " " + risposta.getDsErr() : ""));
                    if (risposta.getCdEsito().equals(EsitoServizio.KO.name()) && risposta.getCdErr() != null
                            && (risposta.getCdErr().equals("666") || risposta.getCdErr().equals("PING-NOT-006"))) {
                        log.info("Errore nella notifica, non elimino il file " + folder);
                    } else {
                        XAUtil.deleteFolderNewTx(xaDiskNativeFS, folder);
                        counters.incrSuccess();
                    }
                }
            }
            audit.info("TIPO OGGETTO: " + objectType.getObjType() + " | " + counters.toString());
        }

    }

    private void processOutFolder() throws MalformedURLException, XAGenericException {
        File[] folder = XAUtil.listFilesNewTX(xaDiskNativeFS, outFolder);
        Counters counters = new Counters(folder.length, outFolder.getName());
        for (File zipStudy : folder) {
            // if(zipStudy.length()>Integer.MAX_VALUE){
            // log.warn("File "+zipStudy.getName()+" più grande di 2GB, per ora non lo invio in FTP.");
            // continue;
            // }
            String globalHash = zipStudy.getName().substring(0, zipStudy.getName().length() - 4);
            boolean fileSended = FTPUtil.sendFile(zipStudy, globalHash + ".zip",
                    ctx.getFtpInputFolder() + "/" + globalHash, ctx.getFtpIP(), ctx.getFtpPort(), ctx.getFtpUser(),
                    ctx.getFtpPassword(), ctx.getSecureFtp());
            if (fileSended) {
                FileDepositatoType file = new FileDepositatoType();
                file.setNmTipoFile(DPIConstants.WS_NM_TIPO_OBJECT);
                file.setNmNomeFile(globalHash + ".zip");
                ListaFileDepositatoType lista = new ListaFileDepositatoType();
                lista.getFileDepositato().add(file);
                NotificaTrasferimentoRisposta risposta = callWSNotifyFileTransfert(zipStudy, globalHash, lista);
                log.info("RISPOSTA DEL WS: " + risposta.getCdEsito() + " "
                        + ((risposta.getCdErr() != null) ? risposta.getCdErr() + " " + risposta.getDsErr() : ""));
                if (risposta.getCdEsito().equals(EsitoServizio.KO.name()) && risposta.getCdErr() != null
                        && (risposta.getCdErr().equals("666") || risposta.getCdErr().equals("PING-NOT-006"))) {
                    log.info("Errore nella notifica, non elimino il file " + zipStudy);
                } else {
                    XAUtil.deleteFileNewTx(xaDiskNativeFS, zipStudy);
                    counters.incrSuccess();
                }
            }
        }
        audit.info(counters.toString());

    }

    private NotificaTrasferimentoRisposta callWSNotifyFileTransfert(File study, String cdKeyObject,
            ListaFileDepositatoType lista) throws MalformedURLException {

        log.debug("Invoco il WS: NotificaTrasferimento - URL: " + ctx.getWsdlNotificaUrl());
        log.debug("Parametri: nmAmbiente: " + ctx.getNmAmbiente() + "\nnmVersatore: " + ctx.getNmVersatore()
                + "\ncdKeyObject: " + cdKeyObject);
        return client.notificaAvvenutoTrasferimentoFile(ctx.getNmAmbiente(), ctx.getNmVersatore(), cdKeyObject, lista);
    }

    private final class Counters {
        public Counters(int total, String foldername) {
            super();
            this.total = total;
            this.foldername = foldername;
        }

        private int total;
        private int success;

        private String foldername;

        public void incrSuccess() {
            this.success++;
        }

        @Override
        public String toString() {
            return "CARTELLA: " + foldername + " | TOTALI: " + total + " | PROCESSATI: " + success;
        }
    }

}
