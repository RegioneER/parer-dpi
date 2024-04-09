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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.xadisk.bridge.proxies.interfaces.Session;

import it.eng.dpi.bean.CStoreBean;
import it.eng.dpi.dicom.scu.StoreSCU;
import it.eng.dpi.exception.XAGenericException;

public class SendService {

    private StoreSCU storeScu;
    private ExecutorService executor;

    public SendService(StoreSCU storeScu, ExecutorService executor) {
        this.storeScu = storeScu;
        this.executor = executor;
    }

    /**
     * Metodo che effettua il trasferimento di uno studio
     * 
     * @param hostname
     *            nome host
     * @param port
     *            porta
     * @param aet
     *            indirizzo aet
     * @param studyRootDir
     *            directory principale
     * @param session
     *            sessione applicativa
     * 
     * @return Numero di immagini trasferite correttamente
     * 
     * @throws IOException
     *             eccezione generica
     * @throws InterruptedException
     *             eccezione generica
     * @throws ConfigurationException
     *             eccezione generica
     * @throws XAGenericException
     *             eccezione generica
     */
    public CStoreBean doTxCStore(String hostname, int port, String aet, File studyRootDir, Session session)
            throws IOException, InterruptedException, ConfigurationException, XAGenericException {
        NetworkApplicationEntity remoteAE = storeScu.createRemoteAE(hostname, port, aet);
        CStoreBean infoBean = storeScu.sendTxStudy(remoteAE, studyRootDir, session, executor);
        return infoBean;
    }

    public CStoreBean doCStore(String hostname, int port, String aet, File studyRootDir)
            throws IOException, InterruptedException, ConfigurationException, XAGenericException {
        NetworkApplicationEntity remoteAE = storeScu.createRemoteAE(hostname, port, aet);
        CStoreBean infoBean = storeScu.sendStudy(remoteAE, studyRootDir, executor);
        return infoBean;
    }

}
