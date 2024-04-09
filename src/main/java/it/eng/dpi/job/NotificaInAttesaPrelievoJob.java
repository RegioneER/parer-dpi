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
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.NotificaInAttesaPrelievo;
import it.eng.sacerasi.ws.NotificaInAttesaPrelievoRisposta;
import it.eng.sacerasi.ws.OggRicRecuperatiType;
import it.eng.sacerasi.ws.RicercaRecuperati;
import it.eng.sacerasi.ws.RicercaRecuperatiRisposta;

@Component
public class NotificaInAttesaPrelievoJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("NOTIFICA_IN_ATTESA_PRELIEVO");
    private static final Logger log = LoggerFactory.getLogger(NotificaInAttesaPrelievoJob.class);

    private File richiesteDir;
    private File disponibiliDir;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    private NotificaInAttesaPrelievo notificaInAttesaClient;
    private RicercaRecuperati ricercaRecuperatiClient;

    @PostConstruct
    public void postConstruct() {
        // richiesteDir = new File(ctx.getWorkingPath() + DPIConstants.RICHIESTE_FOLDER);
        // disponibiliDir = new File(ctx.getWorkingPath() + DPIConstants.DISPONIBILI_FOLDER);
        richiesteDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.RICHIESTE_FOLDER);
        disponibiliDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.DISPONIBILI_FOLDER);
    }

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            if (ricercaRecuperatiClient == null) {
                ricercaRecuperatiClient = init(ctx.getWsdlRicercaRecuperatiUrl(), RicercaRecuperati.class);
            }
            if (notificaInAttesaClient == null) {
                notificaInAttesaClient = init(ctx.getWsdlNotificaInAttesaPrelievoUrl(), NotificaInAttesaPrelievo.class);
            }
            if (notificaInAttesaClient == null || ricercaRecuperatiClient == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                findRecuperati();
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void findRecuperati() {
        log.debug("Ricerco gli studi RECUPERATI");
        RicercaRecuperatiRisposta resp = null;
        try {
            resp = ricercaRecuperatiClient.ricercaRecuperati(ctx.getNmAmbiente(), ctx.getNmVersatore());
        } catch (Exception e) {
            log.error("Si è verificato un'errore nella comunicazione con PRE-INGEST", e);
            audit.info(
                    "SACER PreIngest non risponde alla richiesta di ricercare gli studi la cui sessione di recupero ha stato = RECUPERATO");
        }
        if (resp != null && resp.getCdEsito().equals(EsitoServizio.OK)) {
            log.debug(
                    "Risposta OK dal servizio di ricerca recuperati, procedo alla notifica in attesa prelievo per ogni studio recuperato");
            for (OggRicRecuperatiType oggetto : resp.getListaOggetti().getOggetto()) {
                callNotifica(oggetto.getCdKeyObject());
            }
        } else if (resp != null) {
            audit.info(
                    "Esito KO per richiesta di ricercare gli studi la cui sessione di recupero ha stato = RECUPERATO - ERRORE: "
                            + resp.getCdEsito() + " - " + resp.getDsErr());
        }
    }

    private void callNotifica(String cdKeyObject) {
        log.debug("Procedo alla notifica in attesa prelievo per lo studio " + cdKeyObject);
        NotificaInAttesaPrelievoRisposta resp = null;
        try {
            resp = notificaInAttesaClient.notificaInAttesaPrelievo(ctx.getNmAmbiente(), ctx.getNmVersatore(),
                    cdKeyObject);
        } catch (Exception e) {
            log.error("Si è verificato un'errore nella comunicazione con PRE-INGEST", e);
            audit.info("SACER PreIngest non risponde alla notifica che lo studio " + cdKeyObject
                    + " è in attesa di prelievo");
        }
        if (resp != null && resp.getCdEsito().equals(EsitoServizio.OK)) {
            log.debug("Risposta OK dal servizio di notifica in attesa prelievo, procedo allo spostamento del file");
            // Crea directory
            Session session = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                File dirToDelete = new File(richiesteDir, cdKeyObject);
                File dirToCreate = new File(disponibiliDir, cdKeyObject);
                if (!session.fileExistsAndIsDirectory(dirToCreate)) {
                    XAUtil.createDirectory(session, dirToCreate);
                }
                if (session.fileExistsAndIsDirectory(dirToDelete)) {
                    XAUtil.deleteFile(session, dirToDelete);
                }
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
        } else if (resp != null && resp.getCdErr().equals("666")) {
            audit.info("SACER PreIngest non è riuscito ad registrare / aggiornare la sessione di recupero dello studio "
                    + cdKeyObject);
        } else if (resp != null) {
            audit.info("Esito KO per notifica che lo studio " + cdKeyObject + " è in attesa di prelievo - ERRORE: "
                    + resp.getCdEsito() + " - " + resp.getDlErr());

            // Elimina directory
            Session session = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                File dir = new File(richiesteDir, cdKeyObject);
                if (session.fileExistsAndIsDirectory(dir)) {
                    XAUtil.deleteFile(session, dir);
                }
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
