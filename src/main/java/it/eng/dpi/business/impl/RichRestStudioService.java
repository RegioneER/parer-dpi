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

import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.component.Messages;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.WebGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.RichiestaRestituzioneOggetto;
import it.eng.sacerasi.ws.RichiestaRestituzioneOggettoRisposta;

import java.io.File;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

@Service
public class RichRestStudioService extends AbstractWSClient {

    private static final Logger log = LoggerFactory.getLogger(RichRestStudioService.class);

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private Messages messagesCtx;

    private File richiesteDir;

    private RichiestaRestituzioneOggetto client;

    @PostConstruct
    public void postConstruct() {
        // richiesteDir = new File(ctx.getWorkingPath() + DPIConstants.RICHIESTE_FOLDER);
        richiesteDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.RICHIESTE_FOLDER);
    }

    public void callWS(String cdKeyObject) throws WebGenericException {
        if (client == null) {
            client = init(ctx.getWsdlRichiestaRestituzioneOggettoUrl(), RichiestaRestituzioneOggetto.class);
        }
        if (client == null) {
            throw new WebGenericException(
                    messagesCtx.getServiceUnavailable(RichiestaRestituzioneOggetto.class.getSimpleName()));
        }
        RichiestaRestituzioneOggettoRisposta resp;
        try {
            resp = client.richiestaRestituzioneOggetto(ctx.getNmAmbiente(), ctx.getNmVersatore(), cdKeyObject);
        } catch (Exception e) {
            throw new WebGenericException(
                    messagesCtx.getServiceUnavailable(RichiestaRestituzioneOggetto.class.getSimpleName()), e);
        }
        if (resp != null) {
            log.debug("Risposta richiesta restituzione oggetto: " + resp.getCdEsito());
            if (resp.getCdEsito().equals(EsitoServizio.OK)) {
                Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
                File richFile = new File(richiesteDir, cdKeyObject);
                try {
                    if (!XAUtil.fileExistsAndIsDirectory(xaSession, richFile)) {
                        XAUtil.createDirectory(xaSession, richFile);
                    }
                    xaSession.commit();
                } catch (Exception e) {
                    try {
                        log.error("Si è verificato un errore durante la transazione.", e);
                        xaSession.rollback();
                    } catch (NoTransactionAssociatedException e1) {
                        log.error(
                                "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                                e1);
                    }
                    throw new WebGenericException(messagesCtx.getGeneralError(e.getMessage()));
                }
            } else {
                throw new WebGenericException(messagesCtx.getServiceError(resp.getCdErr(), resp.getDlErr()));
            }
        } else {
            throw new WebGenericException(
                    messagesCtx.getServiceUnavailable(RichiestaRestituzioneOggetto.class.getSimpleName()));
        }

    }
}
