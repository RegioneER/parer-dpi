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

import it.eng.dpi.service.DPIConstants;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StoreCmtSCP {

    private static final Logger log = LoggerFactory.getLogger(StoreCmtSCP.class);
    private static final int NO_SUCH_OBJECT_INSTANCE = 0x0112;

    private File storageRootDir;

    // private static final ExecutorService executor = new ThreadPoolExecutor(100, 100, 1, TimeUnit.MINUTES, new
    // LinkedBlockingQueue<Runnable>());
    private NetworkApplicationEntity ae;
    private final StgCmtSCP stgCmt = new StgCmtSCP();
    private ExecutorService executor;

    public StoreCmtSCP(NetworkApplicationEntity ae, ExecutorService executor) {
        this.ae = ae;
        this.executor = executor;
        log.info("Registering STORE COMMITMENT SCP Service ...");
        ae.register(stgCmt);
        log.info("Registering STORE COMMITMENT SCP Service ... done");

    }

    private final class StgCmtSCP extends StorageCommitmentService {

        @Override
        protected void onNActionRQ(Association as, int pcid, DicomObject rq, DicomObject info, DicomObject rsp) {

        }

        @Override
        protected void onNActionRSP(Association as, int pcid, DicomObject rq, DicomObject info, DicomObject rsp) {
            try {
                sendStgCmtResult(createRemoteAE(as), mkStgCmtResult(as, info));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private NetworkApplicationEntity createRemoteAE(Association as) {
        NetworkConnection remoteConn = new NetworkConnection();
        remoteConn.setHostname(as.getSocket().getInetAddress().getHostAddress());
        remoteConn.setPort(as.getSocket().getPort());
        // remoteConn.setTlsCipherSuite(nc.getTlsCipherSuite());
        NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
        remoteAE.setNetworkConnection(remoteConn);
        remoteAE.setAETitle(as.getRemoteAET());
        remoteAE.setTransferCapability(
                new TransferCapability[] { new TransferCapability(UID.StorageCommitmentPushModelSOPClass,
                        DPIConstants.transferSyntaxes, TransferCapability.SCU) });
        return remoteAE;
    }

    void sendStgCmtResult(NetworkApplicationEntity stgcmtAE, DicomObject result)
            throws ConfigurationException, IOException, InterruptedException {
        final DimseRSPHandler nEventReportRspHandler = new DimseRSPHandler();
        synchronized (ae) {
            ae.setReuseAssocationFromAETitle(new String[] { stgcmtAE.getAETitle() });
            ae.setReuseAssocationToAETitle(new String[] { stgcmtAE.getAETitle() });
            Association as = ae.connect(stgcmtAE, executor);
            as.nevent(UID.StorageCommitmentPushModelSOPClass, UID.StorageCommitmentPushModelSOPInstance,
                    eventTypeIdOf(result), result, UID.ImplicitVRLittleEndian, nEventReportRspHandler);
            // as.release(true);
        }
    }

    private DicomObject mkStgCmtResult(Association as, DicomObject rqdata) throws IOException {
        DicomObject result = new BasicDicomObject();
        result.putString(Tag.TransactionUID, VR.UI, rqdata.getString(Tag.TransactionUID));
        DicomElement rqsq = rqdata.get(Tag.ReferencedSOPSequence);
        DicomElement resultsq = result.putSequence(Tag.ReferencedSOPSequence);
        // if (stgcmtRetrieveAET != null)
        // result.putString(Tag.RetrieveAETitle, VR.AE, stgcmtRetrieveAET);
        DicomElement failedsq = null;
        File dir = new File(getStorageRootDir().getPath() + "/" + as.getCallingAET());
        // getDir(as);
        for (int i = 0, n = rqsq.countItems(); i < n; i++) {
            DicomObject rqItem = rqsq.getDicomObject(i);
            String uid = rqItem.getString(Tag.ReferencedSOPInstanceUID);
            DicomObject resultItem = new BasicDicomObject();
            rqItem.copyTo(resultItem);
            // if (stgcmtRetrieveAETs != null)
            // resultItem.putString(Tag.RetrieveAETitle, VR.AE, stgcmtRetrieveAETs);

            // File f = new File(dir, uid);
            if (FileUtils.listFiles(dir, FileFilterUtils.nameFileFilter(uid + ".dcm"), TrueFileFilter.INSTANCE)
                    .size() > 0) {
                resultsq.addDicomObject(resultItem);
            } else {
                resultItem.putInt(Tag.FailureReason, VR.US, NO_SUCH_OBJECT_INSTANCE);
                if (failedsq == null)
                    failedsq = result.putSequence(Tag.FailedSOPSequence);
                failedsq.addDicomObject(resultItem);
            }
        }
        return result;
    }

    private static int eventTypeIdOf(DicomObject result) {
        return result.contains(Tag.FailedSOPInstanceUIDList) ? 2 : 1;
    }

    public final File getStorageRootDir() {
        return storageRootDir;
    }

    public final void setStorageRootDir(final File storageRootDir) {
        this.storageRootDir = storageRootDir;
    }

    // public final void stop() {
    // executor.shutdown();
    // try {
    // if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
    // executor.shutdownNow();
    // if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
    // log.warn("DICOM QR SCU did not terminate.");
    // }
    // }
    // } catch (final InterruptedException e) {
    // executor.shutdownNow();
    //// Thread.currentThread().interrupt();
    // }
    // }

}
