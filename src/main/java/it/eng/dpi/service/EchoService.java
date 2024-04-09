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

package it.eng.dpi.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;

import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.dpi.dicom.scu.EchoSCU;

public class EchoService {

    private static final Logger log = LoggerFactory.getLogger(EchoService.class);

    private EchoSCU echoScu;
    private ExecutorService executor;

    public EchoService(EchoSCU echoScu, ExecutorService executor) {
        this.echoScu = echoScu;
        this.executor = executor;
    }

    public void doCEcho(String hostname, int port, String aet)
            throws IOException, ConfigurationException, InterruptedException {
        NetworkApplicationEntity remoteAE = echoScu.createRemoteAE(hostname, port, aet);
        Association assoc = echoScu.open(remoteAE, executor);
        try {
            echoScu.echo(assoc);
        } finally {
            echoScu.close(assoc);
        }
    }

    /**
     * Metodo utilizzato dal controller di test per informazioni sull'echo
     * 
     * @param hostname
     *            nome host
     * @param port
     *            porta
     * @param aet
     *            indirizzo aet
     * @param nrOfTests
     *            numero di test
     * 
     * @return risultato
     */
    public String echo(String hostname, int port, String aet, Integer nrOfTests) {
        StringWriter swr = new StringWriter(nrOfTests * 20 + 50);
        StringBuffer echoResult = swr.getBuffer();
        NetworkApplicationEntity remoteAE = echoScu.createRemoteAE(hostname, port, aet);
        echoResult.append("DICOM Echo to ").append(aet).append(":\n");
        try {
            long t0 = System.currentTimeMillis();
            Association assoc = echoScu.open(remoteAE, executor);
            try {
                long t1 = System.currentTimeMillis();
                echoResult.append("Open Association in ").append(t1 - t0).append(" ms.\n");
                echo(assoc, nrOfTests.intValue(), nrOfTests > 1 ? echoResult : null);
                echoResult.append("Total time for successfully echo ").append(aet);
                if (nrOfTests > 1) {
                    echoResult.append(' ').append(nrOfTests).append(" times");
                }
                echoResult.append(": ").append(System.currentTimeMillis() - t0).append(" ms!");
            } finally {
                try {
                    echoScu.close(assoc);
                } catch (Exception e) {
                    log.warn("Failed to release " + assoc);
                }
            }
        } catch (Throwable e) {
            log.error("Echo " + aet + " failed", e);
            echoResult.append("Echo failed! Reason: ").append(e.getMessage());
            e.printStackTrace(new PrintWriter(swr));// write to echoResult
        }
        return echoResult.toString();
    }

    private void echo(Association assoc, int nrOfTests, StringBuffer echoResult)
            throws InterruptedException, IOException {
        long t0, t1, diff;
        int nrOfLT1ms = 0;
        for (int i = 0; i < nrOfTests; i++) {
            t0 = System.currentTimeMillis();
            echoScu.echo(assoc);
            if (echoResult != null) {
                t1 = System.currentTimeMillis();
                diff = t1 - t0;
                if (diff < 1) {
                    nrOfLT1ms++;
                } else {
                    if (nrOfLT1ms > 0) {
                        echoResult.append(nrOfLT1ms).append(" Echoes, each done in less than 1 ms!\n");
                        nrOfLT1ms = 0;
                    }
                    echoResult.append("Echo done in ").append(System.currentTimeMillis() - t0).append(" ms!\n");
                }
            }
        }
        if (nrOfLT1ms > 0)
            echoResult.append(nrOfLT1ms).append(" Echoes, each done in less than 1 ms!\n");
    }

}
