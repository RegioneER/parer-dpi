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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import it.eng.dpi.component.DPIContext;
import it.eng.dpi.slite.gen.Application;
import it.eng.dpi.slite.gen.action.HomeAbstractAction;
import it.eng.integriam.client.ws.IAMSoapClients;
import it.eng.integriam.client.ws.renews.News;
import it.eng.integriam.client.ws.renews.RestituzioneNewsApplicazione;
import it.eng.integriam.client.ws.renews.RestituzioneNewsApplicazioneRisposta;
import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.form.fields.Field;
import it.eng.spagoLite.form.fields.Fields;
import it.eng.spagoLite.security.auth.PwdUtil;

public class HomeAction extends HomeAbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(HomeAction.class);

    private static final String SEC = "dsfjajnoi4jh983nkj43nfkjrenf90rg834jnlkj";

    @Autowired
    private DPIContext ctx;

    @Override
    public void initOnClick() throws EMFError {
    }

    public void process() throws EMFError {
        try {
            findNews();
        } catch (Exception e) {
            logger.error("Errore nel recupero delle news", e);
        }
        forwardToPublisher(getDefaultPublsherName());
    }

    @Override
    public String getControllerName() {
        return Application.Actions.HOME;
    }

    @Override
    protected String getDefaultPublsherName() {
        return Application.Publisher.HOME;
    }

    @Override
    public void loadDettaglio() throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void undoDettaglio() throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveDettaglio() throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dettaglioOnClick() throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void elencoOnClick() throws EMFError {
        goBack();
    }

    public void changePwd() throws EMFError, IOException {
        this.freeze();
        StringBuilder sb = new StringBuilder();
        sb.append(getRequest().getScheme());
        sb.append("://");
        sb.append(getRequest().getServerName());
        sb.append(":");
        sb.append(getRequest().getServerPort());
        sb.append(getRequest().getContextPath());
        String retURL = sb.toString();
        String salt = Base64.encodeBase64URLSafeString(PwdUtil.generateSalt());
        String hmac = getHMAC(retURL + ":" + salt);
        this.getResponse().sendRedirect(ctx.getModificaPasswordUrl() + "?r=" + retURL + "&h=" + hmac + "&s=" + salt);
    }

    @Override
    public void insertDettaglio() throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(Fields<Field> fields) throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Fields<Field> fields) throws EMFError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reloadAfterGoBack(String publisherName) {
    }

    private void findNews() {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        RestituzioneNewsApplicazione client = IAMSoapClients.restituzioneNewsApplicazioneClient(ctx.getUserIam(),
                ctx.getPassIam(), ctx.getWsdlRestituzioneNewsApplicazioneUrl());
        RestituzioneNewsApplicazioneRisposta resp = client.restituzioneNewsApplicazione(ctx.getNmApplic());
        String newline = System.getProperty("line.separator");
        if (resp.getListaNews() != null) {
            for (News row : resp.getListaNews().getNews()) {
                Map<String, String> news = new HashMap<String, String>();
                String line = "";
                if (row.getDlTesto() != null) {
                    line = row.getDlTesto().replaceAll(newline, "<br />");
                }
                SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
                String dateFormatted = fmt.format(row.getDtIniPubblic().toGregorianCalendar().getTime());
                news.put("dsOggetto", "<font size=\"1\">" + dateFormatted + "</font></br><b><font size=\"2\"> "
                        + row.getDsOggetto() + "</font></b>");
                news.put("dlTesto", line);
                news.put("dtIniPubblic", row.getDtIniPubblic().toGregorianCalendar().getTime().toString());

                list.add(news);
            }
        }
        getRequest().setAttribute("news", list);

    }

    private String getHMAC(String msg) throws EMFError {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SEC.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] result = mac.doFinal(msg.getBytes());
            return Base64.encodeBase64URLSafeString(result);
        } catch (NoSuchAlgorithmException ex) {
            throw new EMFError(ex.getMessage(), ex);
        } catch (InvalidKeyException ex) {
            throw new EMFError(ex.getMessage(), ex);
        }
    }

    @Override
    public void mostraInformativa() throws EMFError {
        forwardToPublisher("/login/informativa");
    }

}
