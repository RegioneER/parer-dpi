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

package it.eng.dpi.web.action;

import javax.xml.ws.soap.SOAPFaultException;

import it.eng.dpi.business.impl.DPIAuthenticator;
import it.eng.dpi.slite.gen.Application;

import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.SessionManager;
import it.eng.spagoLite.actions.ActionBase;
import it.eng.spagoLite.security.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Quaranta_M
 * 
 *         1 - process() 2 - passIn() 3 - doLogin()
 * 
 */
public class LoginAction extends ActionBase {

    private static final Logger logger = LoggerFactory.getLogger(LoginAction.class);
    @Autowired
    private DPIAuthenticator authenticator;

    @Override
    public String getControllerName() {
        return Application.Actions.LOGIN;
    }

    @Override
    protected String getDefaultPublsherName() {
        return Application.Publisher.HOME;
    }

    @Override
    public void process() throws EMFError {
        User utente = (User) SessionManager.getUser(getSession());
        if (utente != null) {
            logger.info("Login già effettuato per l'utente " + utente.getUsername());
            redirectToAction(Application.Actions.HOME + "?clearhistory=true");
        } else {
            try {
                utente = authenticator.doLogin(getSession());
                if (utente != null) {
                    redirectToAction(Application.Actions.HOME + "?clearhistory=true");
                } else {
                    forwardToPublisher(Application.Publisher.NOT_AUTHORIZED);
                }
            } catch (SOAPFaultException ex) {
                getMessageBox().addFatal(ex.getMessage());
            }
        }
    }

    @Override
    protected boolean isAuthorized(String destination) {
        return true;
    }

    @Override
    public void reloadAfterGoBack(String publisherName) {
    }
}
