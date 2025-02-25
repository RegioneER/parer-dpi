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

/*
 *   Copyright 2010 MINT Working Group
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package it.eng.dpi.service;

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;

public final class DPIConstants {

    public static final String DICOM_FILE_EXTENSION = ".dcm";
    public static final String PARTIAL_FILE_EXTENSION = ".part";
    public static final String TEMP_DIR_EXTENSION = "TEMP";
    // folder per il versamento
    public static final String CMOVE_DIR = "cmove";
    public static final String CSTORE_DIR = "cstore";
    public static final String NEW_FOLDER = "/V1_new";
    public static final String OUT_FOLDER = "/V2_out";
    public static final String WARN_FOLDER = "/V2_warn";
    // folder per il recupero
    public static final String RICHIESTE_FOLDER = "/R1_richieste";
    public static final String DISPONIBILI_FOLDER = "/R2_disponibili";
    public static final String PRELEVATI_FOLDER = "/R3_prelevati";
    public static final String NOTIFICATI_FOLDER = "/R4_notificati";
    public static final String WORK_FOLDER = "work";

    public static final String VERSIONE_DATI_SPEC_DICOM = "1.1";
    public static final String VERSIONE_XML_PREINGEST = "1.0";
    public static final String WS_NM_TIPO_OBJECT = "StudioDicom";

    public static final String HASH_ALGO = "SHA-1";
    public static final String HASH_ENCODING = "hexBinary";
    public static final String FILE_ENCODING = "BASE64";

    public static final String AUDIT_JOB_START = "JOB START";
    public static final String AUDIT_JOB_STOP = "JOB STOP";
    public static final String AUDIT_JOB_ERROR = "JOB ERROR";

    public static final int JOB_CLEAN_WARN_LIMIT = 500;

    public static final String V1_NEW = "V1_new";
    public static final String V2_OUT = "V2_out";
    public static final String DATI_SPECIFICI_TAG = "DatiSpecifici";
    public static final String PROPERTY_FILE_EXT = ".properties";
    public static final String PRODUTTORE = "PRODUTTORE";
    public static final String PRIMI_4_CRT = "PRIMI_4_CRT";
    public static final String DA_5_A_8_CRT = "DA_5_A_8_CRT";
    public static final String CALC_1 = "CALC_1";
    public static final String DATI_SPECIFICI_PROPERTY_VALUE = "DatiSpecifici.";
    public static final String PROFILO_UD_OGGETTO_PROPERTY_VALUE = "ProfiloUnitaDocumentaria.Oggetto";
    public static final String PROFILO_UD_DATA_PROPERTY_VALUE = "ProfiloUnitaDocumentaria.Data";

    public static final String NON_TRASMESSO_DA_PACS = "NON_TRASMESSO_DA_PACS";

    public static final String[] verificationSOPClasses = { UID.VerificationSOPClass };

    public static final String[] defaultStorageSOPClasses = { UID.StoredPrintStorageSOPClassRetired,
            UID.HardcopyGrayscaleImageStorageSOPClassRetired, UID.HardcopyColorImageStorageSOPClassRetired,
            // UID._12leadECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage, UID.ArterialPulseWaveformStorage, UID.AutorefractionMeasurementsStorage,
            UID.BasicStructuredDisplayStorage, UID.BasicTextSRStorage, UID.BasicVoiceAudioWaveformStorage,
            UID.BlendingSoftcopyPresentationStateStorageSOPClass, UID.BreastTomosynthesisImageStorage,
            UID.CardiacElectrophysiologyWaveformStorage, UID.ChestCADSRStorage, UID.ColonCADSRStorage,
            UID.ColorSoftcopyPresentationStateStorageSOPClass, UID.ComprehensiveSRStorage,
            UID.ComputedRadiographyImageStorage, UID.CTImageStorage, UID.DeformableSpatialRegistrationStorage,
            // UID.DigitalIntraoralXRayImageStorageForPresentation,
            // UID.DigitalIntraoralXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation, UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalXRayImageStorageForPresentation, UID.DigitalXRayImageStorageForProcessing,
            UID.EncapsulatedCDAStorage, UID.EncapsulatedPDFStorage, UID.EnhancedCTImageStorage,
            UID.EnhancedMRColorImageStorage, UID.EnhancedMRImageStorage, UID.EnhancedPETImageStorage,
            UID.EnhancedSRStorage, UID.EnhancedUSVolumeStorage, UID.EnhancedXAImageStorage, UID.EnhancedXRFImageStorage,
            UID.GeneralAudioWaveformStorage, UID.GeneralECGWaveformStorage,
            UID.GrayscaleSoftcopyPresentationStateStorageSOPClass, UID.HemodynamicWaveformStorage,
            UID.KeratometryMeasurementsStorage, UID.KeyObjectSelectionDocumentStorage,
            UID.LensometryMeasurementsStorage,
            // UID.MacularGridThicknessandVolumeReportStorage,
            UID.MammographyCADSRStorage, UID.MRImageStorage, UID.MRSpectroscopyStorage,
            // UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
            // UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
            // UID.MultiframeSingleBitSecondaryCaptureImageStorage,
            // UID.MultiframeTrueColorSecondaryCaptureImageStorage,
            UID.NuclearMedicineImageStorage, UID.OphthalmicPhotography16BitImageStorage,
            UID.OphthalmicPhotography8BitImageStorage, UID.OphthalmicTomographyImageStorage,
            UID.PositronEmissionTomographyImageStorage, UID.ProcedureLogStorage,
            UID.PseudoColorSoftcopyPresentationStateStorageSOPClass, UID.RawDataStorage,
            UID.RealWorldValueMappingStorage, UID.RespiratoryWaveformStorage, UID.RTBeamsTreatmentRecordStorage,
            UID.RTBrachyTreatmentRecordStorage, UID.RTDoseStorage, UID.RTImageStorage,
            UID.RTIonBeamsTreatmentRecordStorage, UID.RTIonPlanStorage, UID.RTPlanStorage, UID.RTStructureSetStorage,
            UID.RTTreatmentSummaryRecordStorage, UID.SecondaryCaptureImageStorage, UID.SegmentationStorage,
            UID.SiemensCSANonImageStorage, UID.SpatialFiducialsStorage, UID.SpatialRegistrationStorage,
            // UID.SpectaclePrescriptionReportsStorage,
            UID.StereometricRelationshipStorage, UID.UltrasoundImageStorage,
            // UID.UltrasoundMultiframeImageStorage,
            UID.VideoEndoscopicImageStorage, UID.VideoMicroscopicImageStorage, UID.VideoPhotographicImageStorage,
            UID.VLEndoscopicImageStorage, UID.VLMicroscopicImageStorage, UID.VLPhotographicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage, UID.XAXRFGrayscaleSoftcopyPresentationStateStorage,
            UID.XRay3DAngiographicImageStorage, UID.XRay3DCraniofacialImageStorage, UID.XRayAngiographicImageStorage,
            UID.XRayRadiationDoseSRStorage, UID.XRayRadiofluoroscopicImageStorage };
    //
    // public static final String[] PATIENT_LEVEL_GET_CUID = {
    // UID.PatientRootQueryRetrieveInformationModelGET,
    // UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired };
    //
    // public static final String[] STUDY_LEVEL_GET_CUID = {
    // UID.StudyRootQueryRetrieveInformationModelGET,
    // UID.PatientRootQueryRetrieveInformationModelGET,
    // UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired };

    public static final String[] PATIENT_LEVEL_FIND_CUID = { UID.PatientRootQueryRetrieveInformationModelFIND,
            UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    public static final String[] PATIENT_LEVEL_MOVE_CUID = { UID.PatientRootQueryRetrieveInformationModelMOVE,
            UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    public static final String[] STUDY_LEVEL_FIND_CUID = { UID.StudyRootQueryRetrieveInformationModelFIND,
            UID.PatientRootQueryRetrieveInformationModelFIND,
            UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    public static final String[] STUDY_LEVEL_MOVE_CUID = { UID.StudyRootQueryRetrieveInformationModelMOVE,
            UID.PatientRootQueryRetrieveInformationModelMOVE,
            UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    public static final String[] STUDY_LEVEL_FIND_CUID_PAT_ROOT_FIRST = {
            UID.PatientRootQueryRetrieveInformationModelFIND, UID.StudyRootQueryRetrieveInformationModelFIND,
            UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    public static final String[] STUDY_LEVEL_MOVE_CUID_PAT_ROOT_FIRST = {
            UID.PatientRootQueryRetrieveInformationModelMOVE, UID.StudyRootQueryRetrieveInformationModelMOVE,
            UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    /**
     * Come da Specifica funzionale ecco le codifiche supportate Codifiche supportate:
     *
     * LittleEndianImplicitTransferSyntax 1.2.840.10008.1.2 LittleEndianExplicitTransferSyntax 1.2.840.10008.1.2.1
     * DeflatedExplicitVRLittleEndianTransferSyntax 1.2.840.10008.1.2.1.99 BigEndianExplicitTransferSyntax
     * 1.2.840.10008.1.2.2 JPEGProcess1TransferSyntax 1.2.840.10008.1.2.4.50 JPEGProcess2_4TransferSyntax
     * 1.2.840.10008.1.2.4.51 JPEGProcess14SV1TransferSyntax 1.2.840.10008.1.2.4.70 JPEGLSLosslessTransferSyntax
     * 1.2.840.10008.1.2.4.80 JPEGLSLossyTransferSyntax 1.2.840.10008.1.2.4.81 JPEG2000LosslessOnlyTransferSyntax
     * 1.2.840.10008.1.2.4.90 JPEG2000TransferSyntax 1.2.840.10008.1.2.4.91 MPEG2MainProfileAtMainLevelTransferSyntax
     * 1.2.840.10008.1.2.4.100 MPEG2MainProfileAtHighLevelTransferSyntax 1.2.840.10008.1.2.4.101
     * RLELosslessTransferSyntax 1.2.840.10008.1.2.5
     */
    public static final String[] transferSyntaxes = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
            UID.DeflatedExplicitVRLittleEndian, UID.ExplicitVRBigEndian, UID.JPEGBaseline1, UID.JPEGExtended24,
            UID.JPEGLossless, UID.JPEGLSLossless, UID.JPEGLSLossyNearLossless, UID.MPEG2, UID.MPEG2MainProfileHighLevel,
            UID.RLELossless, UID.JPEG2000LosslessOnly, UID.JPEG2000 };

    public static final String[] IVLE_TS = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, };

    public static final String[] EVLE_TS = { UID.ExplicitVRLittleEndian, UID.ImplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, };

    public static final String[] EVBE_TS = { UID.ExplicitVRBigEndian, UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian, };

    public static final int[] DicomTagToFilter = {
            // Tag utilizzati dal DPI nei quali è possibile inserire valori a mano
            Tag.InstitutionName, Tag.StudyDescription, Tag.ReferringPhysicianName, };

    // public static final int[] DicomTagToFilter = {
    // //Tag nei quali è possibile inserire valori a mano
    // Tag.InstitutionName,
    // Tag.InstitutionalDepartmentName,
    // Tag.StudyDescription,
    // Tag.ReferringPhysicianName,
    // Tag.InstitutionAddress,
    // Tag.Manufacturer,
    // Tag.ManufacturerModelName,
    // Tag.ProtocolName,
    // Tag.RequestedProcedureDescription,
    // Tag.PerformedProcedureStepDescription,
    // Tag.StationName,
    // Tag.SeriesDescription,
    // };

}
