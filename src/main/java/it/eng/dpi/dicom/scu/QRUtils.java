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
import it.eng.dpi.service.DPIConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QRUtils {

    private static final Logger log = LoggerFactory.getLogger(QRSCU.class);

    public static TransferCapability selectFindTransferCapability(NetworkApplicationEntity remoteAE, Association assoc,
            QueryBean query) throws NoPresentationContextException {
        TransferCapability tc;
        if ((tc = selectTransferCapability(assoc, query.getPrivateFind())) != null)
            return tc;
        if ((tc = selectTransferCapability(assoc, query.getQueryLevel().getFindClassUids())) != null)
            return tc;
        throw new NoPresentationContextException(
                UIDDictionary.getDictionary().prompt(query.getQueryLevel().getFindClassUids()[0]) + " not supported by "
                        + remoteAE.getAETitle());
    }

    public static String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

    public static TransferCapability selectTransferCapability(Association assoc, String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }

    public static TransferCapability selectTransferCapability(Association assoc, List<String> cuid) {
        TransferCapability tc;
        for (int i = 0, n = cuid.size(); i < n; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid.get(i));
            if (tc != null)
                return tc;
        }
        return null;
    }

    public static List<DicomObject> queryUpperLevelUIDs(String cuid, String tsuid, Association assoc, QueryBean query)
            throws IOException, InterruptedException {
        QueryRetrieveLevel qrLevel = query.getQueryLevel();
        List<DicomObject> keylist = new ArrayList<DicomObject>();
        if (Arrays.asList(DPIConstants.PATIENT_LEVEL_FIND_CUID).contains(cuid)) {
            keylist = queryPatientIDs(cuid, tsuid, assoc, query);
            if (qrLevel == QueryRetrieveLevel.STUDY_STUDY_ROOT_FIRST
                    || qrLevel == QueryRetrieveLevel.STUDY_PAT_ROOT_FIRST) {
                return keylist;
            }
            keylist = queryStudyOrSeriesIUIDs(cuid, tsuid, keylist, Tag.StudyInstanceUID,
                    QRConstants.STUDY_MATCHING_KEYS, assoc, query);
        } else {
            keylist.add(new BasicDicomObject());
            keylist = queryStudyOrSeriesIUIDs(cuid, tsuid, keylist, Tag.StudyInstanceUID,
                    QRConstants.PATIENT_STUDY_MATCHING_KEYS, assoc, query);
        }
        // if (qrlevel == QueryRetrieveLevel.IMAGE) {
        // keylist = queryStudyOrSeriesIUIDs(cuid, tsuid, keylist,
        // Tag.SeriesInstanceUID, QRConstants.SERIES_MATCHING_KEYS, QRConstants.QueryRetrieveLevel.SERIES);
        // }
        return keylist;
    }

    public static List<DicomObject> queryPatientIDs(String cuid, String tsuid, Association assoc, QueryBean query)
            throws IOException, InterruptedException {
        int priority = CommandUtils.HIGH;
        List<DicomObject> keylist = new ArrayList<DicomObject>();
        DicomObject keys = query.getKeys();
        String patID = keys.getString(Tag.PatientID);
        String issuer = keys.getString(Tag.IssuerOfPatientID);
        if (patID != null) {
            DicomObject patIdKeys = new BasicDicomObject();
            patIdKeys.putString(Tag.PatientID, VR.LO, patID);
            if (issuer != null) {
                patIdKeys.putString(Tag.IssuerOfPatientID, VR.LO, issuer);
            }
            keylist.add(patIdKeys);
        } else {
            DicomObject patLevelQuery = new BasicDicomObject();
            keys.subSet(QRConstants.PATIENT_MATCHING_KEYS).copyTo(patLevelQuery);
            patLevelQuery.putNull(Tag.PatientID, VR.LO);
            patLevelQuery.putNull(Tag.IssuerOfPatientID, VR.LO);
            patLevelQuery.putString(Tag.QueryRetrieveLevel, VR.CS, "PATIENT");
            log.info("Send Query Request using " + UIDDictionary.getDictionary().prompt(cuid) + ":\n" + patLevelQuery);
            DimseRSP rsp = assoc.cfind(cuid, priority, patLevelQuery, tsuid, Integer.MAX_VALUE);
            for (int i = 0; rsp.next(); ++i) {
                DicomObject cmd = rsp.getCommand();
                if (CommandUtils.isPending(cmd)) {
                    DicomObject data = rsp.getDataset();
                    log.info("Query Response #" + Integer.valueOf(i + 1) + ":\n" + data);
                    DicomObject patIdKeys = new BasicDicomObject();
                    patIdKeys.putString(Tag.PatientID, VR.LO, data.getString(Tag.PatientID));
                    issuer = keys.getString(Tag.IssuerOfPatientID);
                    if (issuer != null) {
                        patIdKeys.putString(Tag.IssuerOfPatientID, VR.LO, issuer);
                    }
                    // TEST MQ
                    if (!keylist.contains(patIdKeys)) {
                        keylist.add(patIdKeys);
                    }
                }
            }
        }
        return keylist;
    }

    public static List<DicomObject> queryStudyOrSeriesIUIDs(String cuid, String tsuid, List<DicomObject> upperLevelIDs,
            int uidTag, int[] matchingKeys, Association assoc, QueryBean query)
            throws IOException, InterruptedException {
        int priority = CommandUtils.HIGH;
        DicomObject keys = query.getKeys();
        QueryRetrieveLevel qrLevel = query.getQueryLevel();
        List<DicomObject> keylist = new ArrayList<DicomObject>();
        String uid = keys.getString(uidTag);
        for (DicomObject upperLevelID : upperLevelIDs) {
            if (uid != null) {
                DicomObject suidKey = new BasicDicomObject();
                upperLevelID.copyTo(suidKey);
                suidKey.putString(uidTag, VR.UI, uid);
                keylist.add(suidKey);
            } else {
                DicomObject keys2 = new BasicDicomObject();
                keys.subSet(matchingKeys).copyTo(keys2);
                upperLevelID.copyTo(keys2);
                keys2.putNull(uidTag, VR.UI);
                keys2.putString(Tag.QueryRetrieveLevel, VR.CS, qrLevel.getCode());
                log.info("Send Query Request using " + UIDDictionary.getDictionary().prompt(cuid) + ":\n" + keys2);
                DimseRSP rsp = assoc.cfind(cuid, priority, keys2, tsuid, Integer.MAX_VALUE);
                for (int i = 0; rsp.next(); ++i) {
                    DicomObject cmd = rsp.getCommand();
                    if (CommandUtils.isPending(cmd)) {
                        DicomObject data = rsp.getDataset();
                        log.info("Query Response #" + Integer.valueOf(i + 1) + ":\n" + data);
                        DicomObject suidKey = new BasicDicomObject();
                        upperLevelID.copyTo(suidKey);
                        suidKey.putString(uidTag, VR.UI, data.getString(uidTag));
                        keylist.add(suidKey);
                    }
                }
            }
        }
        return keylist;
    }

    @SuppressWarnings("fallthrough")
    public static boolean containsUpperLevelUIDs(String cuid, QueryBean query) {
        DicomObject keys = query.getKeys();
        switch (query.getQueryLevel()) {
        // case QRConstants.QueryRetrieveLevel.IMAGE:
        // if (!keys.containsValue(Tag.SeriesInstanceUID)) {
        // return false;
        // }
        // // fall through
        // case QRConstants.QueryRetrieveLevel.SERIES:
        // if (!keys.containsValue(Tag.StudyInstanceUID)) {
        // return false;
        // }
        // fall through
        case STUDY_STUDY_ROOT_FIRST:
            if (Arrays.asList(DPIConstants.PATIENT_LEVEL_FIND_CUID).contains(cuid)
                    && !keys.containsValue(Tag.PatientID)) {
                return false;
            }
        case STUDY_PAT_ROOT_FIRST:
            if (Arrays.asList(DPIConstants.PATIENT_LEVEL_FIND_CUID).contains(cuid)
                    && !keys.containsValue(Tag.PatientID)) {
                return false;
            }
            // fall through
        case PATIENT:
            // fall through
        }
        return true;
    }

    public static boolean containsMoveDest(String[] retrieveAETs, String moveDest) {
        if (retrieveAETs != null) {
            for (String aet : retrieveAETs) {
                if (moveDest.equals(aet)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addMatchingKey(QueryBean query, int[] tagPath, String value) {
        DicomObject keys = query.getKeys();
        keys.putString(tagPath, null, value);
    }

    public static void addReturnKey(QueryBean query, int[] tagPath) {
        DicomObject keys = query.getKeys();
        keys.putNull(tagPath, null);
    }

}
