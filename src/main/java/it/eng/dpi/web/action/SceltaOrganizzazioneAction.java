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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import it.eng.dpi.business.impl.DPIAuthenticator;
import it.eng.dpi.slite.gen.Application;
import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.actions.ActionBase;

/**
 * 
 * @author Quaranta_M
 * 
 *         1 - process() 2 - passIn() 3 - doLogin()
 * 
 */
public class SceltaOrganizzazioneAction extends ActionBase {

    private static final Logger logger = LoggerFactory.getLogger(SceltaOrganizzazioneAction.class);
    @Autowired
    private DPIAuthenticator authenticator;

    @Override
    public String getControllerName() {
        return "SceltaOrganizzazione.html";
    }

    @Override
    protected String getDefaultPublsherName() {
        return null;
    }

    @Override
    public void process() throws EMFError {
        try {
            authenticator.recuperoAutorizzazioni(getSession());
        } catch (Exception ex) {
            logger.error("Errore durante il recupero delle autorizzazioni: " + ex.getMessage());
            getMessageBox().addError("Errore durante il recupero delle autorizzazioni: " + ex.getMessage());
        }
        redirectToAction(Application.Actions.HOME + "?clearhistory=true");
    }

    @Override
    protected boolean isAuthorized(String destination) {
        return true;
    }

    @Override
    public void reloadAfterGoBack(String publisherName) {
    }
}
