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
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.InvioOggettoAsincrono;
import it.eng.sacerasi.ws.InvioOggettoAsincronoRisposta;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

@Component
public class InvioOggettoService extends AbstractWSClient {
    private InvioOggettoAsincrono client;
    private static final Logger log = LoggerFactory.getLogger(InvioOggettoService.class);
    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private Messages messagesCtx;

    private static final Boolean flFileCifrato = false;
    private static final Boolean flForzaAccettazione = true;
    private static final Boolean flForzaWarning = false;
    private String motivazione;

    private File warnFolder;
    private File outFolder;

    @PostConstruct
    public void postConstruct() {
        outFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.OUT_FOLDER);
        warnFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.WARN_FOLDER);
        // outFolder = new File(ctx.getWorkingPath() + DPIConstants.OUT_FOLDER);
        // warnFolder = new File(ctx.getWorkingPath() + DPIConstants.WARN_FOLDER);
    }

    public void processWarnFolder() throws IOException, XAGenericException, WebGenericException {
        if (client == null) {
            init(ctx.getWsdlInvioOggettoUrl(), InvioOggettoAsincrono.class);
        }
        if (client != null) {
            setMotivazione("TEST");
            File[] folders = XAUtil.listFilesNewTX(xaDiskNativeFS, warnFolder);
            List<File> folder = new ArrayList<File>();
            for (File f : folders) {
                if (f.getName().endsWith(".zip")) {
                    folder.add(f);
                }
            }

            for (File zipStudy : folder) {
                String globalHash = zipStudy.getName().substring(0, zipStudy.getName().length() - 4);
                File xmlFile = new File(warnFolder, globalHash + ".xml");
                String xml = FileUtils.readFileToString(xmlFile, "UTF-8");
                InvioOggettoAsincronoRisposta risposta = callWSInvioOggettoPreIngest(globalHash, xml);
                elaborateResponse(risposta, zipStudy, xmlFile);
            }
        }
    }

    public void sendWarnStudy(String globalHash) throws IOException, WebGenericException {
        if (client == null) {
            client = init(ctx.getWsdlInvioOggettoUrl(), InvioOggettoAsincrono.class);
        }
        if (client != null) {
            File xmlFile = new File(warnFolder, globalHash + ".xml");
            File zipFile = new File(warnFolder, globalHash + ".zip");
            if (xmlFile.exists() && zipFile.exists()) {
                String xml = FileUtils.readFileToString(xmlFile, "UTF-8");
                InvioOggettoAsincronoRisposta risposta;
                try {
                    risposta = callWSInvioOggettoPreIngest(globalHash, xml);
                } catch (Exception e) {
                    throw new WebGenericException(
                            messagesCtx.getServiceUnavailable(InvioOggettoAsincrono.class.getSimpleName()));
                }
                elaborateResponse(risposta, zipFile, xmlFile);
                if (risposta.getCdEsito().equals(EsitoServizio.KO)) {
                    throw new WebGenericException(risposta.getCdErr(), risposta.getDsErr());
                }
            } else {
                throw new WebGenericException(messagesCtx.getGeneralError("File non presenti nella cartella warn"));
            }
        }
    }

    private void elaborateResponse(InvioOggettoAsincronoRisposta risposta, File zipStudy, File xmlFile)
            throws WebGenericException {
        if (risposta != null) {
            log.info("RISPOSTA DEL WS: " + risposta.getCdEsito() + " "
                    + ((risposta.getCdErr() != null) ? risposta.getCdErr() + " " + risposta.getDsErr() : ""));
            Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                switch (risposta.getCdEsito()) {
                case OK:
                    XAUtil.moveFile(xaSession, zipStudy, new File(outFolder, zipStudy.getName()));
                    XAUtil.deleteFile(xaSession, xmlFile);

                    break;
                case KO:
                    XAUtil.deleteFile(xaSession, zipStudy);
                    XAUtil.deleteFile(xaSession, xmlFile);

                    break;
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

        }

    }

    private InvioOggettoAsincronoRisposta callWSInvioOggettoPreIngest(String globalHash, String xml)
            throws MalformedURLException {
        log.debug("Invoco il WS: InvioOggettoAsincrono - URL: " + ctx.getWsdlInvioOggettoUrl());
        log.debug("Parametri: nmAmbiente: " + ctx.getNmAmbiente() + "\nnmVersatore: " + ctx.getNmVersatore()
                + "\nglobalHash: " + globalHash + "\nflForzaWarning:" + flForzaWarning + "\nxml:" + xml);
        return client.invioOggettoAsincrono(ctx.getNmAmbiente(), ctx.getNmVersatore(), globalHash,
                DPIConstants.WS_NM_TIPO_OBJECT, flFileCifrato, flForzaWarning, flForzaAccettazione, getMotivazione(),
                DPIConstants.VERSIONE_XML_PREINGEST, xml);
    }

    public String getMotivazione() {
        return motivazione;
    }

    public void setMotivazione(String motivazione) {
        this.motivazione = motivazione;
    }
}
