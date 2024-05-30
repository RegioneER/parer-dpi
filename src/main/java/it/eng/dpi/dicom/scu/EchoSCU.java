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

package it.eng.dpi.dicom.scu;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;

public class EchoSCU {

    private NetworkApplicationEntity ae;

    public EchoSCU(NetworkApplicationEntity ae) {
        this.ae = ae;
    }

    public NetworkApplicationEntity createRemoteAE(String hostname, int port, String remoteAET) {
        NetworkConnection remoteConn = new NetworkConnection();
        remoteConn.setPort(port);
        remoteConn.setHostname(hostname);
        NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
        remoteAE.setAETitle(remoteAET);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);

        // MEV#30349
        NetworkConnection c = ae.getNetworkConnection()[0];
        if (c.isInstalled() && c.isTLS()) {
            remoteConn.setTlsProtocol(c.getTlsProtocol());
            remoteConn.setTlsCipherSuite(c.getTlsCipherSuite());
        }

        // end MEV#30349

        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
        return remoteAE;
    }

    public Association open(NetworkApplicationEntity remoteAE, ExecutorService executor)
            throws IOException, ConfigurationException, InterruptedException {
        return ae.connect(remoteAE, executor);
    }

    public void echo(Association assoc) throws IOException, InterruptedException {
        assoc.cecho().next();
    }

    public void close(Association assoc) throws InterruptedException {
        assoc.release(false);
    }
}
