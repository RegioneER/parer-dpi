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

package it.eng.dpi.controller;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.bean.NotificaDisponibilitaRisposta;
import it.eng.dpi.bean.NotificaDisponibilitaRisposta.EsitoServizio;
import it.eng.dpi.component.DPIContext;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.service.DPIConstants;

@Controller
public class NotificaDisponibilita {

    private static final Logger log = LoggerFactory.getLogger(NotificaDisponibilita.class);

    @Autowired
    private DPIContext ctx;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    private File disponibiliDir;
    private File richiesteDir;

    @PostConstruct
    public void init() {
        // disponibiliDir = new File(ctx.getWorkingPath() + DPIConstants.DISPONIBILI_FOLDER);
        // richiesteDir = new File(ctx.getWorkingPath() + DPIConstants.RICHIESTE_FOLDER);
        disponibiliDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.DISPONIBILI_FOLDER);
        richiesteDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.RICHIESTE_FOLDER);
    }

    @RequestMapping(value = "/NotificaDisponibilita", method = RequestMethod.GET)
    public @ResponseBody NotificaDisponibilitaRisposta notificaDisponibilitaREST(final HttpServletResponse res,
            @RequestParam(value = "nmAmbiente", required = false) String nmAmbiente,
            @RequestParam(value = "nmVersatore", required = false) String nmVersatore,
            @RequestParam(value = "globalHash", required = false) String globalHash) {
        NotificaDisponibilitaRisposta resp = new NotificaDisponibilitaRisposta(nmAmbiente, nmVersatore, globalHash);
        if (nmAmbiente == null || !nmAmbiente.equals(ctx.getNmAmbiente())) {
            resp.setCdErr("DPI-NOTIFDISP-001");
            resp.setDlErr("L’ambiente " + nmAmbiente
                    + " non è valorizzato o è valorizzato con un valore non corrispondente al DPI");
            log.debug("L’ambiente " + nmAmbiente
                    + " non è valorizzato o è valorizzato con un valore non corrispondente al DPI");
            resp.setCdEsito(EsitoServizio.KO);
            return resp;
        }
        if (nmVersatore == null || !nmVersatore.equals(ctx.getNmVersatore())) {
            resp.setCdErr("DPI-NOTIFDISP-002");
            resp.setDlErr("Il versatore " + nmVersatore
                    + " non è valorizzato o è valorizzato con un valore non corrispondente al DPI");
            log.debug("Il versatore " + nmVersatore
                    + " non è valorizzato o è valorizzato con un valore non corrispondente al DPI");
            resp.setCdEsito(EsitoServizio.KO);
            return resp;
        }
        if (globalHash == null || globalHash.trim().length() == 0) {
            resp.setCdErr("DPI-NOTIFDISP-003");
            resp.setDlErr("La chiave identificante lo studio notificato " + globalHash + " non è definita");
            log.debug("La chiave identificante lo studio notificato " + globalHash + " non è definita");
            resp.setCdEsito(EsitoServizio.KO);
            return resp;
        }
        Session session = xaDiskNativeFS.createSessionForLocalTransaction();
        try {
            log.debug("Ricevuta notifica per studio con global hash " + globalHash);
            File dispSubDir = new File(disponibiliDir, globalHash);
            File richSubDir = new File(richiesteDir, globalHash);
            if (!XAUtil.fileExists(session, dispSubDir)) {
                XAUtil.createDirectory(session, dispSubDir);
            }
            if (XAUtil.fileExists(session, richSubDir)) {
                XAUtil.deleteFile(session, richSubDir);
            }
            session.commit();
        } catch (Exception e) {
            log.error("Si è verificato un errore durante le operazioni su filesystem ... procedo al rollback", e);
            try {
                session.rollback();
            } catch (NoTransactionAssociatedException e1) {
                log.error("Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                        e1);
            }
            resp.setCdErr("666");
            resp.setDlErr("Si è verificato un errore durante le operazioni su filesystem ... procedo al rollback");
            log.debug("Si è verificato un errore durante le operazioni su filesystem ... procedo al rollback");
            resp.setCdEsito(EsitoServizio.KO);
            return resp;
        }
        resp.setCdEsito(EsitoServizio.OK);
        return resp;

    }

}
