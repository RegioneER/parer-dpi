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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.dpi.business.impl;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.dpi.component.DPIContext;
import it.eng.dpi.component.Util;
import it.eng.dpi.exception.AuthenticationException;
import it.eng.dpi.web.util.Constants;
import it.eng.integriam.client.util.UserUtil;
import it.eng.integriam.client.ws.IAMSoapClients;
import it.eng.integriam.client.ws.recauth.AuthWSException_Exception;
import it.eng.integriam.client.ws.recauth.RecuperoAutorizzazioni;
import it.eng.integriam.client.ws.recauth.RecuperoAutorizzazioniRisposta;
import it.eng.spagoLite.SessionManager;
import it.eng.spagoLite.security.User;
import it.eng.spagoLite.security.auth.Authenticator;

/**
 * 
 * @author Quaranta_M
 */
@Component
public class DPIAuthenticator extends Authenticator {

    @Autowired
    private DPIContext ctx;

    public User recuperoAutorizzazioni(HttpSession httpSession) {
        User utente = (User) SessionManager.getUser(httpSession);
        RecuperoAutorizzazioni client = IAMSoapClients.recuperoAutorizzazioniClient(ctx.getUserIam(), ctx.getPassIam(),
                ctx.getWsdlRecuperoAutorizzazionioUrl());
        RecuperoAutorizzazioniRisposta resp;
        try {
            resp = client.recuperoAutorizzazioniPerNome(utente.getUsername(), ctx.getNmApplic(), null);
        } catch (AuthWSException_Exception e) {
            throw new RuntimeException(e);
        }
        UserUtil.fillComponenti(utente, resp);
        Map<String, String> organizzazione = new LinkedHashMap<String, String>();
        organizzazione.put("AMBIENTE", ctx.getNmAmbiente());
        organizzazione.put("VERSATORE", ctx.getNmVersatore());
        utente.setOrganizzazioneMap(organizzazione);
        SessionManager.setUser(httpSession, utente);

        return utente;
    }

    public User doLogin(String user, String password) throws AuthenticationException {
        String encHashedPwd = Util.encodePassword(password);
        User utente = new User();
        boolean authorized = false;
        if (ctx.getAdminUser().equals(user) && ctx.getAdminPwd().equals(encHashedPwd)) {
            authorized = true;
            utente.setIdApplicazione(Long.valueOf(1));
        }
        if (!authorized) {
            throw new AuthenticationException(
                    "Autenticazione fallita. Utente non abilitato all'applicativo " + Constants.DPI);
        }
        utente.setIdUtente(1);
        Calendar c = Calendar.getInstance();
        c.set(2444, 10, 10);
        utente.setScadenzaPwd(c.getTime());
        utente.setUsername(ctx.getAdminUser());
        utente.setCognome(ctx.getAdminSurname());
        utente.setNome(ctx.getAdminName());
        utente.setAttivo(ctx.isAdminAttivo());

        return utente;
    }

    @Override
    protected String getAppName() {
        return ctx.getNmApplic();
    }

}
