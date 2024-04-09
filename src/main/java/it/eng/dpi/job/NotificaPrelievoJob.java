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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.NotificaPrelievo;
import it.eng.sacerasi.ws.NotificaPrelievoRisposta;

@Component
public class NotificaPrelievoJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("NOTIFICA_PRELIEVO");
    private static final Logger log = LoggerFactory.getLogger(NotificaPrelievoJob.class);

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    private File prelevatiDir;
    private File notificatiDir;

    private NotificaPrelievo client;

    @PostConstruct
    public void postConstruct() {
        // prelevatiDir = new File(ctx.getWorkingPath() + DPIConstants.PRELEVATI_FOLDER);
        // notificatiDir = new File(ctx.getWorkingPath() + DPIConstants.NOTIFICATI_FOLDER);
        prelevatiDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.PRELEVATI_FOLDER);
        notificatiDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.NOTIFICATI_FOLDER);
    }

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            if (client == null) {
                client = init(ctx.getWsdlNotificaPrelievoUrl(), NotificaPrelievo.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                processFolder();
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void processFolder() throws MalformedURLException, XAGenericException {
        File[] folder = XAUtil.listFilesNewTX(xaDiskNativeFS, prelevatiDir);
        for (File ghFolder : folder) {
            log.debug("Elaboro la cartella " + ghFolder + " procedo alla notifica prelievo");
            NotificaPrelievoRisposta resp;
            try {
                resp = client.notificaPrelievo(ctx.getNmAmbiente(), ctx.getNmVersatore(), ghFolder.getName());
            } catch (Exception e) {
                log.error("Si è verificato un'errore nella comunicazione con PRE-INGEST", e);
                audit.info(
                        "SACER PreIngest non risponde alla notifica del prelievo dello studio " + ghFolder.getName());
                continue;
            }
            if (resp != null && resp.getCdEsito().equals(EsitoServizio.OK)) {
                log.debug("Risposta OK dal servizio di notifica prelievo, procedo allo spostamento del file");
                Session session = xaDiskNativeFS.createSessionForLocalTransaction();
                try {
                    XAUtil.moveFile(session, ghFolder, new File(notificatiDir, ghFolder.getName()));
                    session.commit();
                } catch (Exception e) {
                    try {
                        log.error("Si è verificato un errore durante la transazione.", e);
                        session.rollback();
                    } catch (NoTransactionAssociatedException e1) {
                        log.error(
                                "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                                e1);
                    }
                }
            } else if (resp != null) {
                audit.info("Esito KO per notifica prelievo dello studio " + ghFolder.getName() + " - ERRORE: "
                        + resp.getCdEsito() + " - " + resp.getDlErr());
                Session session = xaDiskNativeFS.createSessionForLocalTransaction();

                try {
                    XAUtil.deleteFile(session, ghFolder);
                    session.commit();
                } catch (Exception e) {
                    try {
                        log.error("Si è verificato un errore durante la transazione.", e);
                        session.rollback();
                    } catch (NoTransactionAssociatedException e1) {
                        log.error(
                                "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                                e1);
                    }
                }
            }

        }

    }

}
