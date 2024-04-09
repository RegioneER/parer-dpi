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

import it.eng.dpi.service.DPIConstants;

import org.dcm4che2.data.Tag;

public class QRConstants {

    public static enum QueryRetrieveLevel {
        PATIENT("PATIENT", PATIENT_RETURN_KEYS, DPIConstants.PATIENT_LEVEL_FIND_CUID,
                // PATIENT_LEVEL_GET_CUID,
                DPIConstants.PATIENT_LEVEL_MOVE_CUID),
        STUDY_STUDY_ROOT_FIRST("STUDY", STUDY_RETURN_KEYS, DPIConstants.STUDY_LEVEL_FIND_CUID,
                // STUDY_LEVEL_GET_CUID,
                DPIConstants.STUDY_LEVEL_MOVE_CUID),
        STUDY_PAT_ROOT_FIRST("STUDY", STUDY_RETURN_KEYS, DPIConstants.STUDY_LEVEL_FIND_CUID_PAT_ROOT_FIRST,
                // STUDY_LEVEL_GET_CUID,
                DPIConstants.STUDY_LEVEL_MOVE_CUID_PAT_ROOT_FIRST);
        // SERIES("SERIES", SERIES_RETURN_KEYS, SERIES_LEVEL_FIND_CUID,
        // SERIES_LEVEL_GET_CUID, SERIES_LEVEL_MOVE_CUID),
        // IMAGE("IMAGE", INSTANCE_RETURN_KEYS, SERIES_LEVEL_FIND_CUID,
        // SERIES_LEVEL_GET_CUID, SERIES_LEVEL_MOVE_CUID);

        private final String code;
        private final int[] returnKeys;
        private final String[] findClassUids;
        // private final String[] getClassUids;
        private final String[] moveClassUids;

        private QueryRetrieveLevel(String code, int[] returnKeys, String[] findClassUids,
                // String[] getClassUids,
                String[] moveClassUids) {
            this.code = code;
            this.returnKeys = returnKeys;
            this.findClassUids = findClassUids;
            // this.getClassUids = getClassUids;
            this.moveClassUids = moveClassUids;
        }

        public String getCode() {
            return code;
        }

        public int[] getReturnKeys() {
            return returnKeys;
        }

        public String[] getFindClassUids() {
            return findClassUids;
        }

        // public String[] getGetClassUids() {
        // return getClassUids;
        // }

        public String[] getMoveClassUids() {
            return moveClassUids;
        }
    }

    public static final int[] PATIENT_RETURN_KEYS = { Tag.PatientName, Tag.PatientID, Tag.PatientBirthDate,
            Tag.PatientSex, Tag.NumberOfPatientRelatedStudies, Tag.NumberOfPatientRelatedSeries,
            Tag.NumberOfPatientRelatedInstances };

    public static final int[] STUDY_RETURN_KEYS = { Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber, Tag.StudyID,
            Tag.StudyInstanceUID, Tag.NumberOfStudyRelatedSeries, Tag.NumberOfStudyRelatedInstances };

    public static final int[] PATIENT_MATCHING_KEYS = { Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
            Tag.PatientBirthDate, Tag.PatientSex };

    public static final int[] STUDY_MATCHING_KEYS = { Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber,
            Tag.ModalitiesInStudy, Tag.ReferringPhysicianName, Tag.StudyID, Tag.StudyInstanceUID };

    public static final int[] PATIENT_STUDY_MATCHING_KEYS = { Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber,
            Tag.ModalitiesInStudy, Tag.ReferringPhysicianName, Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
            Tag.PatientBirthDate, Tag.PatientSex, Tag.StudyID, Tag.StudyInstanceUID };

    public static final int[] DPI_CUSTOM_RETURN_KEYS = {
            // StudyUID
            Tag.StudyInstanceUID,
            // o Number of Images in Study
            Tag.NumberOfStudyRelatedInstances,
            // Patient Name
            Tag.PatientName,
            // Patient ID
            Tag.PatientID,
            // Patient BirthDate
            Tag.PatientBirthDate,
            // Patient Sex
            Tag.PatientSex,
            // Accession Number
            Tag.AccessionNumber };

    public static final int[] MOVE_KEYS = { Tag.QueryRetrieveLevel, Tag.PatientID, Tag.StudyInstanceUID,
            Tag.SeriesInstanceUID, Tag.SOPInstanceUID };

}
