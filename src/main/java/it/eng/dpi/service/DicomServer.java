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
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.dcm4che2.data.UID;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DicomServer {

    private static final Logger log = LoggerFactory.getLogger(DicomServer.class);
    private final NetworkConnection networkConnection = new NetworkConnection();
    private final NetworkApplicationEntity aeStoreSCP = new NetworkApplicationEntity();
    private final NetworkApplicationEntity aeSCU = new NetworkApplicationEntity();
    private final NetworkApplicationEntity aeStoreSCU = new NetworkApplicationEntity();
    private final Device device = new Device();
    // The number of threads here likely corresponds to how many concurrent
    // associations can be served
    // (but need at least two to serve a single association; also, starting with
    // a smaller core pool
    // does not seem to work, so keep the two numbers in sync)

    public DicomServer(List<String> validAET, String[] sopClasses, Integer reaperTO, Integer connTO) {

        initNetworkAESCP(aeStoreSCP, getTcStoreScp(sopClasses), validAET);
        initNetworkAESCU(aeSCU, getTcScu(), validAET);
        initNetworkAESCU(aeStoreSCU, null, validAET);
        networkConnection.setTcpNoDelay(true);
        networkConnection.setAcceptTimeout(connTO);
        networkConnection.setRequestTimeout(connTO);
        device.setAssociationReaperPeriod(reaperTO);
        device.setNetworkApplicationEntity(new NetworkApplicationEntity[] { aeStoreSCP, aeSCU, aeStoreSCU });
        device.setNetworkConnection(networkConnection);

    }

    // TransferSyntax per la echo
    // private TransferCapability[] getTcEchoScp() {
    // // TC per la storage
    // final TransferCapability[] tcs = new TransferCapability[DPIConstants.verificationSOPClasses.length];
    // int i = 0;
    // for (String cuid : DPIConstants.verificationSOPClasses){
    // tcs[i++] = new TransferCapability(cuid, DPIConstants.transferSyntaxes,TransferCapability.SCP);
    // }
    // return tcs;
    //
    // }

    // TransferSyntax SCP per la storage, la storage CMT e la ECHO
    private TransferCapability[] getTcStoreScp(String[] sopClasses) {
        final TransferCapability[] tcs = new TransferCapability[sopClasses.length + 1
                + DPIConstants.verificationSOPClasses.length];
        int i = 0;
        for (String cuid : sopClasses) {
            tcs[i++] = new TransferCapability(cuid, DPIConstants.transferSyntaxes, TransferCapability.SCP);
        }
        for (String cuid : DPIConstants.verificationSOPClasses) {
            tcs[i++] = new TransferCapability(cuid, DPIConstants.EVLE_TS, TransferCapability.SCP);
        }
        tcs[i++] = new TransferCapability(UID.StorageCommitmentPushModelSOPClass, DPIConstants.transferSyntaxes,
                TransferCapability.SCP);
        return tcs;

    }

    // TransferSyntax SCU per ECHO e STORE
    private TransferCapability[] getTcStoreScu(String[] sopClasses) {
        final TransferCapability[] tcs = new TransferCapability[sopClasses.length
                + DPIConstants.verificationSOPClasses.length];
        int i = 0;
        for (String cuid : sopClasses) {
            tcs[i++] = new TransferCapability(cuid, DPIConstants.transferSyntaxes, TransferCapability.SCU);
        }
        for (String cuid : DPIConstants.verificationSOPClasses) {
            tcs[i++] = new TransferCapability(cuid, DPIConstants.EVLE_TS, TransferCapability.SCU);
        }
        return tcs;

    }

    // transfer per la query scu e ECHO
    private TransferCapability[] getTcScu() {

        final TransferCapability[] tcs = new TransferCapability[
        // DPIConstants.PATIENT_LEVEL_FIND_CUID.length
        // + DPIConstants.PATIENT_LEVEL_MOVE_CUID.length +
        DPIConstants.STUDY_LEVEL_FIND_CUID.length + DPIConstants.STUDY_LEVEL_MOVE_CUID.length
                + DPIConstants.verificationSOPClasses.length];

        int i = 0;

        for (String cuid : DPIConstants.verificationSOPClasses)
            tcs[i++] = new TransferCapability(cuid, DPIConstants.EVLE_TS, TransferCapability.SCU);
        // for (String cuid : DPIConstants.PATIENT_LEVEL_FIND_CUID)
        // tcs[i++] = new TransferCapability(cuid, DPIConstants.transferSyntaxes,TransferCapability.SCU);
        // for (String cuid : DPIConstants.PATIENT_LEVEL_MOVE_CUID)
        // tcs[i++] = new TransferCapability(cuid, DPIConstants.transferSyntaxes,TransferCapability.SCU);
        for (String cuid : DPIConstants.STUDY_LEVEL_FIND_CUID)
            tcs[i++] = new TransferCapability(cuid, DPIConstants.EVLE_TS, TransferCapability.SCU);
        for (String cuid : DPIConstants.STUDY_LEVEL_MOVE_CUID)
            tcs[i++] = new TransferCapability(cuid, DPIConstants.EVLE_TS, TransferCapability.SCU);

        return tcs;

    }

    public final void start(ExecutorService executor) throws IOException {
        device.startListening(executor);
        log.debug("DICOM SERVER started.");
    }

    public final void stop() {
        device.stopListening();
        log.debug("DICOM SERVER stopped.");
    }

    public NetworkConnection getNetworkConnection() {
        return networkConnection;
    }

    public final void setDeviceName(final String name) {
        device.setDeviceName(name);
    }

    public final void setAETitle(final String aeTitle) {
        // aeEchoSCP.setAETitle(aeTitle);
        aeStoreSCP.setAETitle(aeTitle);
        aeSCU.setAETitle(aeTitle);
        aeStoreSCU.setAETitle(aeTitle);
    }

    public final void setPort(final int port) {
        networkConnection.setPort(port);
    }

    public final void setHostName(final String hostName) {
        networkConnection.setHostname(hostName);
    }

    public final void setReaperTimeout(final int timeoutMS) {
        device.setAssociationReaperPeriod(timeoutMS);
    }

    // public NetworkApplicationEntity getAeEchoSCP() {
    // return aeEchoSCP;
    // }

    public NetworkApplicationEntity getAeStoreSCP() {
        return aeStoreSCP;
    }

    public NetworkApplicationEntity getAeSCU() {
        return aeSCU;
    }

    public NetworkApplicationEntity getAeStoreSCU() {
        return aeStoreSCU;
    }

    private void initNetworkAESCP(NetworkApplicationEntity nae, TransferCapability[] tc, List<String> validAET) {
        nae.setNetworkConnection(networkConnection);
        nae.setAssociationAcceptor(true);
        nae.setAssociationInitiator(false);
        nae.setPackPDV(true);
        nae.setTransferCapability(tc);
        nae.setPreferredCallingAETitle(validAET.toArray(new String[validAET.size()]));
    }

    private void initNetworkAESCU(NetworkApplicationEntity nae, TransferCapability[] tc, List<String> validAET) {
        nae.setNetworkConnection(networkConnection);
        nae.setAssociationAcceptor(false);
        nae.setAssociationInitiator(true);
        nae.setPackPDV(true);
        nae.setTransferCapability(tc);
        nae.setPreferredCallingAETitle(validAET.toArray(new String[validAET.size()]));

    }

    public void refreshSopClass(String[] sopClasses) {
        aeStoreSCP.setTransferCapability(getTcStoreScp(sopClasses));
        aeStoreSCU.setTransferCapability(getTcStoreScu(sopClasses));
    }

}
