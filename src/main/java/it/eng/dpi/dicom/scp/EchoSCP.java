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

package it.eng.dpi.dicom.scp;

import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EchoSCP {

    private static final Logger log = LoggerFactory.getLogger(EchoSCP.class);
    private final VerificationService cEchoSCP = new VerificationService();

    public EchoSCP(NetworkApplicationEntity ae) {
        log.info("Registering ECHO SCP Service ...");
        ae.register(cEchoSCP);
        log.info("Registering ECHO SCP Service ... done");
    }

}
