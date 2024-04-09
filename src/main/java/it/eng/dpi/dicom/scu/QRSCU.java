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

import it.eng.dpi.bean.QueryBean;
import it.eng.dpi.dicom.scu.QRConstants.QueryRetrieveLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QRSCU {

    private static final Logger log = LoggerFactory.getLogger(QRSCU.class);

    private NetworkApplicationEntity ae;
    private String moveDestAET;
    private boolean evalRetrieveAET;

    public QRSCU(NetworkApplicationEntity ae) {
        this.ae = ae;
        moveDestAET = ae.getAETitle();

    }

    public NetworkApplicationEntity createRemoteAE(String hostname, int port, String remoteAET) {
        NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
        remoteAE.setAETitle(remoteAET);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        NetworkConnection remoteConn = new NetworkConnection();
        remoteConn.setPort(port);
        remoteConn.setHostname(hostname);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
        return remoteAE;
    }

    public Association open(NetworkApplicationEntity remoteAE, ExecutorService executor)
            throws IOException, ConfigurationException, InterruptedException {
        ae.setReuseAssocationToAETitle(new String[] { remoteAE.getAETitle() });
        return ae.connect(remoteAE, executor, true);
    }

    public List<DicomObject> query(Association assoc, NetworkApplicationEntity remoteAE, QueryBean query)
            throws IOException, InterruptedException {
        List<DicomObject> result = new ArrayList<DicomObject>();
        TransferCapability tc = QRUtils.selectFindTransferCapability(remoteAE, assoc, query);
        String cuid = tc.getSopClass();
        String tsuid = QRUtils.selectTransferSyntax(tc);
        // Query con Patient Level oppure Study Level e Study Root Information
        // Model
        if (tc.getExtInfoBoolean(ExtQueryTransferCapability.RELATIONAL_QUERIES)
                || QRUtils.containsUpperLevelUIDs(cuid, query)) {
            log.info(
                    "Send Query Request using " + UIDDictionary.getDictionary().prompt(cuid) + ":\n" + query.getKeys());
            DimseRSP rsp = assoc.cfind(cuid, CommandUtils.NORMAL, query.getKeys(), tsuid, query.getCancelAfter());
            while (rsp.next()) {
                DicomObject cmd = rsp.getCommand();
                if (CommandUtils.isPending(cmd)) {
                    DicomObject data = rsp.getDataset();
                    result.add(data);
                    log.debug("Query Response #" + Integer.valueOf(result.size()) + ":\n" + data);
                }
            }
            // Query con Study Level e Patient Root Information Model
        } else {
            List<DicomObject> upperLevelUIDs = QRUtils.queryUpperLevelUIDs(cuid, tsuid, assoc, query);
            List<DimseRSP> rspList = new ArrayList<DimseRSP>(upperLevelUIDs.size());
            for (int i = 0, n = upperLevelUIDs.size(); i < n; i++) {
                upperLevelUIDs.get(i).copyTo(query.getKeys());
                log.info("Send Query Request #" + Integer.valueOf(i + 1) + "/" + Integer.valueOf(n) + " using "
                        + UIDDictionary.getDictionary().prompt(cuid) + ":\n" + query.getKeys());
                rspList.add(assoc.cfind(cuid, CommandUtils.NORMAL, query.getKeys(), tsuid, query.getCancelAfter()));
            }
            for (int i = 0, n = rspList.size(); i < n; i++) {
                DimseRSP rsp = rspList.get(i);
                for (int j = 0; rsp.next(); ++j) {
                    DicomObject cmd = rsp.getCommand();
                    if (CommandUtils.isPending(cmd)) {
                        DicomObject data = rsp.getDataset();
                        result.add(data);
                        log.debug("Query Response #" + Integer.valueOf(j + 1) + " for Query Request #"
                                + Integer.valueOf(i + 1) + "/" + Integer.valueOf(n) + ":\n" + data);
                    }
                }
            }
        }
        log.info("Query Response, ricevuti " + Integer.valueOf(result.size()) + " record\n");
        return result;
    }

    public void move(List<DicomObject> findResults, NetworkApplicationEntity remoteAE, Association assoc,
            final QueryBean query
    // ,DimseRSPHandler rspHandler
    ) throws IOException, InterruptedException {
        QueryRetrieveLevel qrLevel = query.getQueryLevel();
        if (moveDestAET == null)
            throw new IllegalStateException("moveDest == null");
        TransferCapability tc = QRUtils.selectTransferCapability(assoc, qrLevel.getMoveClassUids());
        if (tc == null)
            throw new NoPresentationContextException(UIDDictionary.getDictionary().prompt(qrLevel.getMoveClassUids()[0])
                    + " not supported by " + remoteAE.getAETitle());
        String cuid = tc.getSopClass();
        String tsuid = QRUtils.selectTransferSyntax(tc);
        for (int i = 0; i < findResults.size(); i++) {
            DicomObject keys = findResults.get(i).subSet(QRConstants.MOVE_KEYS);
            if (evalRetrieveAET
                    && QRUtils.containsMoveDest(findResults.get(i).getStrings(Tag.RetrieveAETitle), moveDestAET)) {
                log.info("Skipping " + UIDDictionary.getDictionary().prompt(cuid) + ":\n" + keys);
            } else {
                log.info("Send Retrieve Request using " + UIDDictionary.getDictionary().prompt(cuid) + ":\n" + keys);

                DimseRSPHandler rspHandler = new DimseRSPHandler() {
                    @Override
                    public void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
                        QRSCU.this.onMoveRSP(as, cmd, data, query);
                    }
                };
                for (int n = 0; n < query.getCancelAfter(); n++) {
                    assoc.cmove(cuid, CommandUtils.NORMAL, keys, tsuid, moveDestAET, rspHandler);
                    assoc.waitForDimseRSP();
                    log.info("MOVE Response - NumberOfCompletedSuboperations: " + query.getCompleted()
                            + " - NumberOfWarningSuboperations: " + query.getWarning()
                            + " - NumberOfFailedSuboperations: " + query.getFailed());
                    if (query.getWarning() > 0 || query.getFailed() > 0) {
                        log.warn("L'operazione di MOVE ha restituito un esito negativo. Tentativo numero " + (n + 1));
                        if (n + 1 == query.getCancelAfter()) {
                            log.error("L'operazione di MOVE, dopo " + query.getCancelAfter()
                                    + " tentativi, non ha completato il trasferimento.");
                        }
                    } else
                        break;
                }
            }
        }
        log.info("MOVE RESULTS - Total NumberOfCompletedSuboperations: " + query.getTotalCompleted()
                + " - Total NumberOfWarningSuboperations: " + query.getTotalWarning()
                + " - Total NumberOfFailedSuboperations: " + query.getTotalFailed());
        // assoc.waitForDimseRSP();
    }

    protected void onMoveRSP(Association as, DicomObject cmd, DicomObject data, QueryBean query) {
        if (!CommandUtils.isPending(cmd)) {
            query.setCompleted(cmd.getInt(Tag.NumberOfCompletedSuboperations));
            query.setWarning(cmd.getInt(Tag.NumberOfWarningSuboperations));
            query.setFailed(cmd.getInt(Tag.NumberOfFailedSuboperations));
            query.setTotalCompleted(query.getTotalCompleted() + cmd.getInt(Tag.NumberOfCompletedSuboperations));
            query.setTotalWarning(query.getTotalWarning() + cmd.getInt(Tag.NumberOfWarningSuboperations));
            query.setTotalFailed(query.getTotalFailed() + cmd.getInt(Tag.NumberOfFailedSuboperations));

        }

    }

    public void setDefReturnKeys(QueryBean query) {
        DicomObject keys = query.getKeys();
        for (int tag : query.getQueryLevel().getReturnKeys())
            keys.putNull(tag, null);
    }

    public void close(Association assoc) throws InterruptedException {
        assoc.release(false);
    }

}
