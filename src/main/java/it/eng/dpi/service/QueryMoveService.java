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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.NetworkApplicationEntity;

import it.eng.dpi.bean.Patient;
import it.eng.dpi.bean.QueryBean;
import it.eng.dpi.bean.Study;
import it.eng.dpi.dicom.scu.QRSCU;

public class QueryMoveService {

    private QRSCU qrScu;
    private ExecutorService executor;

    public QueryMoveService(QRSCU qrScu, ExecutorService executor) {
        this.qrScu = qrScu;
        this.executor = executor;
    }

    public List<DicomObject> doCFind(QueryBean query, String hostname, int port, String aet)
            throws IOException, ConfigurationException, InterruptedException {
        NetworkApplicationEntity remoteAE = qrScu.createRemoteAE(hostname, port, aet);
        Association assoc = null;
        List<DicomObject> result = null;
        try {
            assoc = qrScu.open(remoteAE, executor);
            result = qrScu.query(assoc, remoteAE, query);
        } finally {
            if (assoc != null) {
                qrScu.close(assoc);
            }
        }
        return result;

    }

    public void doCFindCMove(QueryBean query, String hostname, int port, String aet)
            throws IOException, ConfigurationException, InterruptedException {
        NetworkApplicationEntity remoteAE = qrScu.createRemoteAE(hostname, port, aet);
        Association assoc = null;
        try {
            assoc = qrScu.open(remoteAE, executor);
            List<DicomObject> result = qrScu.query(assoc, remoteAE, query);
            qrScu.move(result, remoteAE, assoc, query);
        } finally {
            if (assoc != null) {
                qrScu.close(assoc);
            }
        }

    }

    public void doCMove(List<DicomObject> result, QueryBean query, String hostname, int port, String aet)
            throws IOException, ConfigurationException, InterruptedException {
        NetworkApplicationEntity remoteAE = qrScu.createRemoteAE(hostname, port, aet);
        Association assoc = null;
        try {
            assoc = qrScu.open(remoteAE, executor);
            qrScu.move(result, remoteAE, assoc, query);
        } finally {
            if (assoc != null) {
                qrScu.close(assoc);
            }
        }

    }

    /**
     * Utilizzato dal testController
     *
     * @param query
     *            bean rappresentante la query da eseguire {@link QueryBean}
     * @param hostname
     *            nome host
     * @param port
     *            porta
     * @param aet
     *            indirizzo aet
     *
     * @return lista oggetti di tipo {@link Patient}
     *
     * @throws IOException
     *             eccezione generica
     * @throws ConfigurationException
     *             eccezione generica
     * @throws InterruptedException
     *             eccezione generica
     */
    public List<Patient> doCFindPatLevel(QueryBean query, String hostname, int port, String aet)
            throws IOException, ConfigurationException, InterruptedException {
        List<DicomObject> dicomList = this.doCFind(query, hostname, port, aet);
        List<Patient> patients = new ArrayList<Patient>();
        for (DicomObject d : dicomList) {
            patients.add(new Patient(d));
        }
        return patients;
    }

    /**
     * Utilizzato dal testController
     *
     * @param query
     *            bean rappresentante la query da eseguire {@link QueryBean}
     * @param hostname
     *            nome host
     * @param port
     *            porta
     * @param aet
     *            indirizzo aet
     *
     * @return lista oggetti di tipo {@link Study}
     *
     * @throws IOException
     *             eccezione generica
     * @throws ConfigurationException
     *             eccezione generica
     * @throws InterruptedException
     *             eccezione generica
     */
    public List<Study> doCFindStudyLevel(QueryBean query, String hostname, int port, String aet)
            throws IOException, ConfigurationException, InterruptedException {
        List<DicomObject> dicomList = this.doCFind(query, hostname, port, aet);
        List<Study> studies = new ArrayList<Study>();
        for (DicomObject d : dicomList) {
            studies.add(new Study(d));
        }
        return studies;
    }

}
