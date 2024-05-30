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

package it.eng.dpi.controller;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.dpi.business.impl.DPIAuthenticator;
import it.eng.dpi.exception.AuthenticationException;
import it.eng.spagoLite.security.User;

@RestController
@RequestMapping("/admin")
public class AppInfos {

    private static final Logger log = LoggerFactory.getLogger(AppInfos.class);

    // constant
    private static final String ROOT_GIT = "git";
    private static final String ROOT_ENV = "env";
    private static final String ROOT_SYSPROPS = "sysprops";
    private static final String ETAG = "v1.0";
    //
    private static final String SYS_CONFIG_ROOT_TO_SKIP = "admin-appinfos.roottoskip";
    private static final String SYS_CONFIG_PROP_TO_SKIP = "admin-appinfos.proptoskip";

    private Properties gitproperties = null;

    @Autowired
    private DPIAuthenticator authenticator;

    @PostConstruct
    public void init() {
        // custom
        try (InputStream input = getClass().getResourceAsStream("/git.properties")) {
            gitproperties = new Properties();
            // load a properties file
            gitproperties.load(input);
        } catch (IOException e) {
            log.error("Errore init", e);
        }
    }

    @GetMapping(value = { "/infos.do" }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Map<String, String>>> infos(HttpServletRequest req) {

        if (!isUserAuthenticated(req.getHeader("authorization"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else {

            // final result
            final Map<String, Map<String, String>> infos = Collections.synchronizedMap(new LinkedHashMap<>());
            // props or root to skip
            final String rootToSkip = StringUtils.defaultString(System.getProperty(SYS_CONFIG_ROOT_TO_SKIP),
                    StringUtils.EMPTY);
            final String propToSkip = StringUtils.defaultString(System.getProperty(SYS_CONFIG_PROP_TO_SKIP),
                    StringUtils.EMPTY);
            // infos
            // git
            Map<String, String> git = gitproperties.entrySet().stream()
                    .filter(p -> !String.valueOf(p.getKey()).matches(propToSkip))
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue()),
                            (prev, next) -> next, HashMap::new));
            // filter
            if (!ROOT_GIT.matches(rootToSkip)) {
                infos.put(ROOT_GIT, git);
            }
            // env
            Map<String, String> env = System.getenv().entrySet().stream().filter(p -> !p.getKey().matches(propToSkip))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // filter
            if (!ROOT_ENV.matches(rootToSkip)) {
                infos.put(ROOT_ENV, env);
            }
            // sys props
            Map<String, String> sysprops = System.getProperties().entrySet().stream()
                    .filter(p -> !String.valueOf(p.getKey()).matches(propToSkip))
                    .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue()),
                            (prev, next) -> next, HashMap::new));
            // filter
            if (!ROOT_SYSPROPS.matches(rootToSkip)) {
                infos.put(ROOT_SYSPROPS, sysprops);
            }

            return ResponseEntity.ok().lastModified(LocalDateTime.now().atZone(ZoneId.systemDefault())).eTag(ETAG)
                    .body(infos);
        }
    }

    /*
     * Basic Auth con credenziali da anagrafica. Nota: si ri-utilizza per semplificare il processo di gestione, il
     * servizio di monitoraggio che è comunque da considerasi ad utilizzo esclusivo interno / amministrativo.
     */
    private boolean isUserAuthenticated(String authString) {
        if (StringUtils.isBlank(authString))
            return false;
        String decodedAuth = "";
        // Header is in the format "Basic 5tyc0uiDat4"
        // We need to extract data before decoding it back to original string
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[1];
        // Decode the data back to original string
        byte[] bytes = null;
        bytes = new Base64().decode(authInfo);
        decodedAuth = new String(bytes);

        final String AUTH_SPLITTERATOR = ":";
        final String loginName = decodedAuth.split(AUTH_SPLITTERATOR)[0];
        final String pwd = decodedAuth.split(AUTH_SPLITTERATOR)[1];

        // check credentials
        User user = null;
        try {
            user = authenticator.doLogin(loginName, pwd);
        } catch (AuthenticationException e) {
            log.error("Errore generico in fase di autenticazione", e);
            return false;
        }
        return !Objects.isNull(user) && user.getScadenzaPwd().after(new Date()) && user.isAttivo();
    }

}
