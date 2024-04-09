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

import it.eng.dpi.slite.gen.Application;
import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.SessionManager;
import it.eng.spagoLite.actions.ActionBase;
import it.eng.spagoLite.security.User;

public class LogoutAction extends ActionBase {

    @Override
    public String getControllerName() {
        return Application.Actions.LOGOUT;
    }

    @Override
    public void process() throws EMFError {

        User utente = (User) SessionManager.getUser(getSession());
        if (utente != null) {
            // SessionManager.clear(getSession());
            // getSession().invalidate();
            // setLastPublisher("");
            // logger.info("Logout eseguito correttamente per l'utente " + utente.getUsername());
            // getMessageBox().setViewMode(ViewMode.plain);
            getMessageBox().addInfo("L'utente " + utente.getUsername() + " ha richiesto il logout");
            redirectToAction("saml/logout/");
        }

    }

    @Override
    protected String getDefaultPublsherName() {
        return Application.Publisher.LOGOUT;
    }

    @Override
    protected boolean isAuthorized(String destination) {
        return true;
    }

    @Override
    public void reloadAfterGoBack(String publisherName) {

    }
}
