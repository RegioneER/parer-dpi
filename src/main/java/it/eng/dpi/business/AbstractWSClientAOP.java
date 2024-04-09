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

package it.eng.dpi.business;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import it.eng.dpi.component.DPIContext;
import it.eng.spagoLite.security.auth.AuthenticationHandlerConstants;
import it.eng.spagoLite.security.auth.SOAPClientLoginHandlerResolver;

public abstract class AbstractWSClientAOP {

    @Autowired
    private ApplicationContext context;

    @Autowired
    protected DPIContext ctx;

    private static final Logger log = LoggerFactory.getLogger(AbstractWSClientAOP.class);
    private static final String SERVICE_NS = "http://ws.sacerasi.eng.it/";

    @SuppressWarnings("unchecked")
    public synchronized <T> T init(String wsURL, Class<T> portType, String advisorBeanName) {
        try {
            log.info("Connessione al WS: " + wsURL);
            URL wsdlURL = new URL(wsURL + "?wsdl");

            QName SERVICE_NAME = new QName(SERVICE_NS, portType.getSimpleName());
            javax.xml.ws.Service service = javax.xml.ws.Service.create(wsdlURL, SERVICE_NAME);
            service.setHandlerResolver(new SOAPClientLoginHandlerResolver());
            T clientTemp = service.getPort(portType);

            ProxyFactory factory = new ProxyFactory(clientTemp);
            factory.addAdvisor((Advisor) context.getBean(advisorBeanName));
            T client = (T) factory.getProxy();
            Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();
            // Timeout in millis
            requestContext.put("com.sun.xml.internal.ws.connect.timeout", Integer.parseInt(ctx.getWsTimeout()));
            // Timeout in millis
            requestContext.put("com.sun.xml.internal.ws.request.timeout", Integer.parseInt(ctx.getWsTimeout()));
            // Endpoint URL
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsURL);
            requestContext.put(AuthenticationHandlerConstants.USER, ctx.getUserPing());
            requestContext.put(AuthenticationHandlerConstants.PWD, ctx.getPassPing());
            return client;
        } catch (Exception e) {
            log.error("Errore durante l'inizializzazione del bean. WS Sever offline?", e);
            return null;
        }
    }

}
