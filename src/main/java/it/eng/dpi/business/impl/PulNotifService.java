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
import it.eng.dpi.exception.WebGenericException;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.PuliziaNotificato;
import it.eng.sacerasi.ws.PuliziaNotificatoRisposta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PulNotifService extends AbstractWSClient {
    private static final Logger log = LoggerFactory.getLogger(PulNotifService.class);
    private PuliziaNotificato client;

    @Autowired
    private Messages messagesCtx;

    public void callWS(String cdKeyObject) throws WebGenericException {
        log.debug("Chiamata al servizio PULIZIA_NOTIFICATO per l'oggetto " + cdKeyObject);
        if (client == null) {
            client = init(ctx.getWsdlPuliziaNotificatoUrl(), PuliziaNotificato.class);
        }
        if (client == null) {
            log.debug("Servizio del Pre Ingest non disponibile");
            throw new WebGenericException(messagesCtx.getServiceUnavailable(PuliziaNotificato.class.getSimpleName()));
        }
        PuliziaNotificatoRisposta resp;
        try {
            resp = client.puliziaNotificato(ctx.getNmAmbiente(), ctx.getNmVersatore(), cdKeyObject);
        } catch (Exception e) {
            throw new WebGenericException(messagesCtx.getServiceUnavailable(PuliziaNotificato.class.getSimpleName()));
        }
        elaborateResponse(resp);
    }

    private void elaborateResponse(PuliziaNotificatoRisposta risposta) throws WebGenericException {
        if (risposta != null) {
            if (risposta.getCdEsito().equals(EsitoServizio.KO)) {
                throw new WebGenericException(messagesCtx.getServiceError(risposta.getCdErr(), risposta.getDlErr()));
            }
        }
    }
}
