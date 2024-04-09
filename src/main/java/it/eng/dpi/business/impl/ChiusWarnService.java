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
import it.eng.sacerasi.ws.RichiestaChiusuraWarning;
import it.eng.sacerasi.ws.RichiestaChiusuraWarningRisposta;

import java.io.File;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

@Component
public class ChiusWarnService extends AbstractWSClient {
    private static final Logger log = LoggerFactory.getLogger(ChiusWarnService.class);
    private RichiestaChiusuraWarning client;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private Messages messagesCtx;

    private File warnFolder;

    @PostConstruct
    public void postConstruct() {
        // warnFolder = new File(ctx.getWorkingPath() + DPIConstants.WARN_FOLDER);
        warnFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.WARN_FOLDER);

    }

    public void callWS(String cdKeyObject, String dlMotivazione) throws WebGenericException {
        if (client == null) {
            client = init(ctx.getWsdlRichiestaChiusuraWarningUrl(), RichiestaChiusuraWarning.class);
        }
        if (client == null) {
            throw new WebGenericException(
                    messagesCtx.getServiceUnavailable(RichiestaChiusuraWarning.class.getSimpleName()));
        }
        RichiestaChiusuraWarningRisposta resp;
        try {
            resp = client.richiestaChiusuraWarning(ctx.getNmAmbiente(), ctx.getNmVersatore(), cdKeyObject,
                    dlMotivazione);
        } catch (Exception e) {
            throw new WebGenericException(
                    messagesCtx.getServiceUnavailable(RichiestaChiusuraWarning.class.getSimpleName()));
        }
        File xmlFile = new File(warnFolder, cdKeyObject + ".xml");
        File zipFile = new File(warnFolder, cdKeyObject + ".zip");
        elaborateResponse(resp, zipFile, xmlFile);
    }

    private void elaborateResponse(RichiestaChiusuraWarningRisposta risposta, File zipStudy, File xmlFile)
            throws WebGenericException {
        if (risposta != null) {
            if (risposta.getCdEsito().equals(EsitoServizio.OK)) {
                Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
                try {
                    if (XAUtil.fileExists(xaSession, zipStudy)) {
                        XAUtil.deleteFile(xaSession, zipStudy);
                    }
                    if (XAUtil.fileExists(xaSession, xmlFile)) {
                        XAUtil.deleteFile(xaSession, xmlFile);
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
                    throw new WebGenericException(messagesCtx.getFatalError());
                }
            } else {
                throw new WebGenericException(risposta.getCdErr(), risposta.getDlErr());
            }
        }
    }
}
