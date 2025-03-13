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

package it.eng.dpi.web.security;

import it.eng.spagoLite.security.saml.SliteSAMLUserDetail;

/**
 *
 * @author MIacolucci
 *
 *         Classe al momento lasciata vuota perché il framework (baseSecurityContext.xml) lo vuole. In questa classe si
 *         implementa la ricerca sul db locale. Vedere PING e altre applicazioni. I metodi non vengono ridefiniti in
 *         quanto DPI non accede al db (credo) e quindi tutti i metodi della superclasse tornano null.
 */
public class DpiSAMLUserDetail extends SliteSAMLUserDetail {

}
