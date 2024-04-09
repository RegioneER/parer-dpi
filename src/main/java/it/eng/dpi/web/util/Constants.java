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

package it.eng.dpi.web.util;

public class Constants {

    public static final String DPI = "DPI";
    public static final int PASSWORD_EXPIRATION_DAYS = 60;
    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";
    public static final String DB_TRUE = "1";
    public static final String DB_FALSE = "0";
    public static final String UTF_ENCODING = "UTF-8";

    // Costanti dei prefissi dei nomi file di recupero
    public static final String UD_FILE_PREFIX_DPI = "UD_";
    public static final String UD_INDEX_FILE = "indice.xml";
    public static final String PC_FILE_PREFIX_DPI = "PC_";
    public static final String PC_INDEX_FILE = "indice_PC.xml";
    public static final String ZIP_EXTENSION = ".zip";
    // Costanti dei nomi dei job
    public static final String ObjectCreatorQJob1 = "objectCreatorQJob1";
    public static final String ObjectCreatorQJob2 = "objectCreatorQJob2";
    public static final String ObjectCreatorQJob3 = "objectCreatorQJob3";
    public static final String ObjectCreatorCoordinatorQJob = "objectCreatorCoordinatorQJob";
    public static final String ObjectSenderQJob = "objectSenderQJob";
    public static final String FTPTransfertQJob = "FTPTransfertQJob";
    public static final String UpdateSOPClassQJob = "updateSOPClassQJob";
    public static final String QueryPacsPingQJobD = "queryPacsPingQJobD";
    public static final String QueryPacsPingQJobW = "queryPacsPingQJobW";
    public static final String QueryPacsPingQJobM = "queryPacsPingQJobM";
    public static final String PrelievoFTPQJob = "prelievoFTPQJob";
    public static final String NotificaPrelievoQJob = "notificaPrelievoQJob";
    public static final String NotificaInAttesaPrelievoQJob = "notificaInAttesaPrelievoQJob";
    public static final String PuliziaInAttesaFileQJob = "puliziaInAttesaFileQJob";
    public static final String ObjectCopyQJob = "objectCopyQJob";
    public static final String GenericObjectCreatorQJob = "genericObjectCreatorQJob";

    public enum StatoStudio {

        CHIUSO_ERR, CHIUSO_ERR_CODA, CHIUSO_ERR_NOTIF, CHIUSO_ERR_SCHED, CHIUSO_ERR_VERS, CHIUSO_ERR_NOTIFICATO,
        CHIUSO_ERR_RECUPERATO, CHIUSO_ERR_PRELEVATO, CHIUSO_ERR_TIMEOUT, CHIUSO_OK, CHIUSO_WARNING, CHIUSO_FORZATA,
        IN_ATTESA_FILE, IN_ATTESA_SCHED, IN_ATTESA_VERS, IN_ATTESA_PRELIEVO, IN_ATTESA_RECUP, WARNING, IN_CODA_VERS,
        DA_VERSARE, VERSATA_OK, VERSATA_ERR, RECUPERATO, CHIUSO_ERR_ELIMINATO, ELIMINATO;

        public static StatoStudio[] getEnums(StatoStudio... vals) {
            return vals;
        }

        public static StatoStudio[] getRicercaDiarioEnums() {
            return getEnums(CHIUSO_OK, IN_ATTESA_FILE, IN_ATTESA_SCHED, IN_ATTESA_VERS, IN_CODA_VERS, WARNING,
                    CHIUSO_WARNING, CHIUSO_ERR_NOTIF, CHIUSO_ERR_SCHED, CHIUSO_ERR_CODA, CHIUSO_ERR_VERS,
                    CHIUSO_ERR_TIMEOUT);
        }

        public static StatoStudio[] getRicercaRestituzioniEnums() {
            return getEnums(CHIUSO_ERR, CHIUSO_ERR_NOTIFICATO, CHIUSO_ERR_RECUPERATO, CHIUSO_ERR_PRELEVATO, CHIUSO_OK,
                    IN_ATTESA_PRELIEVO, IN_ATTESA_RECUP, RECUPERATO, CHIUSO_ERR_ELIMINATO, ELIMINATO);
        }
    }

    public enum Sex {

        M, F, O
    }

    public enum ValoriOperatore {
        UGUALE, DIVERSO, MAGGIORE, MAGGIORE_UGUALE, MINORE, MINORE_UGUALE, INIZIA_PER, CONTIENE, NON_CONTIENE, IN,
        COMPRESO_FRA;

        public static ValoriOperatore[] getEnums(ValoriOperatore... vals) {
            return vals;
        }
    }

    public static final String AETNodoDicom = "AETNodoDicom";
    private static final String AETNodoDicomMethod = "cdAetNodoDicom";
    public static final String StudyDate = "StudyDate";
    private static final String StudyDateMethod = "dtStudyDate";
    public static final String AccessionNumber = "AccessionNumber";
    private static final String AccessionNumberMethod = "dsAccessionNumber";
    public static final String ModalityInStudyList = "ModalityInStudyList";
    private static final String ModalityInStudyListMethod = "dlListaModalityInStudy";
    public static final String SOPClassList = "SOPClassList";
    private static final String SOPClassListMethod = "dlListaSopClass";
    public static final String InstitutionName = "InstitutionName";
    private static final String InstitutionNameMethod = "dsInstitutionName";
    public static final String ReferringPhysicianName = "ReferringPhysicianName";
    private static final String ReferringPhysicianNameMethod = "dsRefPhysicianName";
    public static final String StudyDescription = "StudyDescription";
    private static final String StudyDescriptionMethod = "dlStudyDescription";
    public static final String PatientName = "PatientName";
    private static final String PatientNameMethod = "dsPatientName";
    public static final String PatientId = "PatientId";
    private static final String PatientIdMethod = "cdPatientId";
    public static final String PatientIdIssuer = "PatientIdIssuer";
    private static final String PatientIdIssuerMethod = "cdPatientIdIssuer";
    public static final String PatientBirthDate = "PatientBirthDate";
    private static final String PatientBirthDateMethod = "dtPatientBirthDate";
    public static final String PatientSex = "PatientSex";
    private static final String PatientSexMethod = "tiPatientSex";
    public static final String StudyInstanceUID = "StudyInstanceUID";
    private static final String StudyInstanceUIDMethod = "dsStudyInstanceUid";
    public static final String NumberStudyRelatedSeries = "NumberStudyRelatedSeries";
    private static final String NumberStudyRelatedSeriesMethod = "niStudyRelatedSeries";
    public static final String NumberStudyRelatedImages = "NumberStudyRelatedImages";
    private static final String NumberStudyRelatedImagesMethod = "niStudyRelatedImages";
    public static final String StudyID = "StudyID";
    private static final String StudyIDMethod = "dsStudyId";
    public static final String DataPresaInCarico = "DataPresaInCarico";
    private static final String DataPresaInCaricoMethod = "dtPresaInCarico";
    public static final String DCM_hash = "DCM-hash";
    private static final String DCM_hashMethod = "dsDcmHash";
    public static final String DCM_hash_algo = "DCM-hash-algo";
    private static final String DCM_hash_algoMethod = "tiAlgoDcmHash";
    public static final String DCM_hash_encoding = "DCM-hash-encoding";
    private static final String DCM_hash_encodingMethod = "cdEncodingDcmHash";
    public static final String DCM_hash_Descrizione = "DCM-hash-Descrizione";
    private static final String DCM_hash_DescrizioneMethod = "blDcmHashTxt";
    public static final String GLOBAL_hash = "GLOBAL-hash";
    private static final String GLOBAL_hashMethod = "dsGlobalHash";
    public static final String GLOBAL_hash_algo = "GLOBAL-hash-algo";
    private static final String GLOBAL_hash_algoMethod = "tiAlgoGlobalHash";
    public static final String GLOBAL_hash_encoding = "GLOBAL-hash-encoding";
    private static final String GLOBAL_hash_encodingMethod = "cdEncodingGlobalHash";
    public static final String GLOBAL_hash_Descrizione = "GLOBAL-hash-Descrizione";
    private static final String GLOBAL_hash_DescrizioneMethod = "blGlobalHashTxt";
    public static final String FILE_hash = "FILE-hash";
    private static final String FILE_hashMethod = "dsFileHash";
    public static final String FILE_hash_algo = "FILE-hash-algo";
    private static final String FILE_hash_algoMethod = "tiAlgoFileHash";
    public static final String FILE_hash_encoding = "FILE-hash-encoding";
    private static final String FILE_hash_encodingMethod = "cdEncodingFileHash";

    public enum DatiSpecDicom {

        AETNodoDicom(Constants.AETNodoDicom, Constants.AETNodoDicomMethod),
        StudyDate(Constants.StudyDate, Constants.StudyDateMethod),
        AccessionNumber(Constants.AccessionNumber, Constants.AccessionNumberMethod),
        ModalityInStudyList(Constants.ModalityInStudyList, Constants.ModalityInStudyListMethod),
        SOPClassList(Constants.SOPClassList, Constants.SOPClassListMethod),
        InstitutionName(Constants.InstitutionName, Constants.InstitutionNameMethod),
        ReferringPhysicianName(Constants.ReferringPhysicianName, Constants.ReferringPhysicianNameMethod),
        StudyDescription(Constants.StudyDescription, Constants.StudyDescriptionMethod),
        PatientName(Constants.PatientName, Constants.PatientNameMethod),
        PatientId(Constants.PatientId, Constants.PatientIdMethod),
        PatientIdIssuer(Constants.PatientIdIssuer, Constants.PatientIdIssuerMethod),
        PatientBirthDate(Constants.PatientBirthDate, Constants.PatientBirthDateMethod),
        PatientSex(Constants.PatientSex, Constants.PatientSexMethod),
        StudyInstanceUID(Constants.StudyInstanceUID, Constants.StudyInstanceUIDMethod),
        NumberStudyRelatedSeries(Constants.NumberStudyRelatedSeries, Constants.NumberStudyRelatedSeriesMethod),
        NumberStudyRelatedImages(Constants.NumberStudyRelatedImages, Constants.NumberStudyRelatedImagesMethod),
        StudyID(Constants.StudyID, Constants.StudyIDMethod),
        DataPresaInCarico(Constants.DataPresaInCarico, Constants.DataPresaInCaricoMethod),
        DCM_hash(Constants.DCM_hash, Constants.DCM_hashMethod),
        DCM_hash_algo(Constants.DCM_hash_algo, Constants.DCM_hash_algoMethod),
        DCM_hash_encoding(Constants.DCM_hash_encoding, Constants.DCM_hash_encodingMethod),
        DCM_hash_Descrizione(Constants.DCM_hash_Descrizione, Constants.DCM_hash_DescrizioneMethod),
        GLOBAL_hash(Constants.GLOBAL_hash, Constants.GLOBAL_hashMethod),
        GLOBAL_hash_algo(Constants.GLOBAL_hash_algo, Constants.GLOBAL_hash_algoMethod),
        GLOBAL_hash_encoding(Constants.GLOBAL_hash_encoding, Constants.GLOBAL_hash_encodingMethod),
        GLOBAL_hash_Descrizione(Constants.GLOBAL_hash_Descrizione, Constants.GLOBAL_hash_DescrizioneMethod),
        FILE_hash(Constants.FILE_hash, Constants.FILE_hashMethod),
        FILE_hash_algo(Constants.FILE_hash_algo, Constants.FILE_hash_algoMethod),
        FILE_hash_encoding(Constants.FILE_hash_encoding, Constants.FILE_hash_encodingMethod);

        String value = "";
        String method = "";

        DatiSpecDicom(String value, String method) {
            this.value = value;
            this.method = method;
        }

        public String getValue() {
            return this.value;
        }

        public String getMethod() {
            return this.method;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static DatiSpecDicom getEnum(String value) {
            DatiSpecDicom result = null;
            if (value == null) {
                return null;
            }
            for (DatiSpecDicom v : values()) {
                if (value.equalsIgnoreCase(v.getValue())) {
                    result = v;
                    break;
                }
            }
            return result;
        }

        public static DatiSpecDicom[] getEnums(DatiSpecDicom... vals) {
            return vals;
        }

        public static DatiSpecDicom[] getRicercaOutputEnums() {
            return getEnums(AETNodoDicom, StudyDate, DataPresaInCarico, AccessionNumber, PatientName, PatientSex,
                    PatientBirthDate, PatientId, PatientIdIssuer, ReferringPhysicianName, ModalityInStudyList);
        }

        public static DatiSpecDicom[] getRecuperoOutputEnums() {
            return getEnums(AETNodoDicom, StudyDate, AccessionNumber, PatientName, PatientSex, PatientBirthDate,
                    PatientId, PatientIdIssuer, ReferringPhysicianName, ModalityInStudyList);
        }

        public static DatiSpecDicom[] getDettaglioDiarioOutputEnums() {
            return getEnums(AETNodoDicom, StudyDate, AccessionNumber, PatientName, PatientSex, PatientBirthDate,
                    PatientId, PatientIdIssuer, ReferringPhysicianName, ModalityInStudyList, SOPClassList,
                    InstitutionName, StudyDescription, StudyInstanceUID, NumberStudyRelatedSeries,
                    NumberStudyRelatedImages, StudyID, DataPresaInCarico, DCM_hash, DCM_hash_algo, DCM_hash_encoding,
                    DCM_hash_Descrizione, GLOBAL_hash, GLOBAL_hash_algo, GLOBAL_hash_encoding, GLOBAL_hash_Descrizione,
                    FILE_hash, FILE_hash_algo, FILE_hash_encoding);
        }
    }

    public static final String STORESCP = "STORESCP";
    public static final String CONFRONTO_PACS_PREINGEST = "CONFRONTO_PACS_PREINGEST";
    public static final String CREA_OGGETTO = "CREA_OGGETTO";
    public static final String TRASFERIMENTO_FTP = "TRASFERIMENTO_FTP";
    public static final String INVIO_OGGETTO = "INVIO_OGGETTO";
    public static final String PRELIEVO_FTP = "PRELIEVO_FTP";
    public static final String NOTIFICA_PRELIEVO = "NOTIFICA_PRELIEVO";
    public static final String MISSINGSOPCLASSLOG = "MISSINGSOPCLASSLOG";
    public static final String UPDATE_SOPCLASS = "UPDATE_SOPCLASS";
    public static final String NOTIFICA_IN_ATTESA_PRELIEVO = "NOTIFICA_IN_ATTESA_PRELIEVO";
    public static final String SPRINGFRAMEWORK = "org.springframework";
    public static final String DCM4CHE2 = "org.dcm4che2";
    public static final String QUARTZ = "org.quartz";
    public static final String ENG = "it.eng";

    public enum LoggerElements {

        STORESCP(Constants.STORESCP), CONFRONTO_PACS_PREINGEST(Constants.CONFRONTO_PACS_PREINGEST),
        CREA_OGGETTO(Constants.CREA_OGGETTO), TRASFERIMENTO_FTP(Constants.TRASFERIMENTO_FTP),
        INVIO_OGGETTO(Constants.INVIO_OGGETTO), PRELIEVO_FTP(Constants.PRELIEVO_FTP),
        NOTIFICA_PRELIEVO(Constants.NOTIFICA_PRELIEVO), MISSINGSOPCLASSLOG(Constants.MISSINGSOPCLASSLOG),
        UPDATE_SOPCLASS(Constants.UPDATE_SOPCLASS), NOTIFICA_IN_ATTESA_PRELIEVO(Constants.NOTIFICA_IN_ATTESA_PRELIEVO),
        SPRINGFRAMEWORK(Constants.SPRINGFRAMEWORK), DCM4CHE2(Constants.DCM4CHE2), QUARTZ(Constants.QUARTZ),
        ENG(Constants.ENG);

        String name = "";

        LoggerElements(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    // Lista Modality
    public static final String CR = "Computed Radiography ";
    public static final String CT = "Computed Tomography";
    public static final String MR = "Magnetic Resonance ";
    public static final String NM = "Nuclear Medicine";
    public static final String US = "Ultrasound ";
    public static final String OT = "Other";
    public static final String BI = "Biomagnetic imaging";
    public static final String DG = "Diaphanography";
    public static final String ES = "Endoscopy ";
    public static final String LS = "Laser surface scan";
    public static final String PT = "Positron emission tomography (PET) ";
    public static final String RG = "Radiographic imaging";
    public static final String TG = "Thermography";
    public static final String XA = "X-Ray Angiography ";
    public static final String RF = "Radio Fluoroscopy";
    public static final String RTIMAGE = "Radiotherapy Image ";
    public static final String RTDOSE = "Radiotherapy Dose";
    public static final String RTSTRUCT = "Radiotherapy Structure Set ";
    public static final String RTPLAN = "Radiotherapy Plan";
    public static final String RTRECORD = "RT Treatment Record ";
    public static final String HC = "Hard Copy";
    public static final String DX = "Digital Radiography ";
    public static final String MG = "Mammography";
    public static final String IO = "Intra-oral Radiography ";
    public static final String PX = "Panoramic X-Ray";
    public static final String GM = "General Microscopy ";
    public static final String SM = "Slide Microscopy";
    public static final String XC = "External-camera Photography ";
    public static final String PR = "Presentation State";
    public static final String AU = "Audio ";
    public static final String ECG = "Electrocardiography";
    public static final String EPS = "Cardiac Electrophysiology ";
    public static final String HD = "Hemodynamic Waveform";
    public static final String SR = "SR Document ";
    public static final String IVUS = "Intravascular Ultrasound";
    public static final String OP = "Ophthalmic Photography ";
    public static final String SMR = "Stereometric Relationship";
    public static final String AR = "Autorefraction ";
    public static final String KER = "Keratometry";
    public static final String VA = "Visual Acuity ";
    public static final String SRF = "Subjective Refraction";
    public static final String OCT = "Optical Coherence Tomography";
    public static final String LEN = "Lensometry";
    public static final String OPV = "Ophthalmic Visual Field ";
    public static final String OPM = "Ophthalmic Mapping";
    public static final String OAM = "Ophthalmic Axial Measurements";
    public static final String RESP = "Respiratory Waveform";
    public static final String KO = "Key Object Selection ";
    public static final String SEG = "Segmentation";
    public static final String REG = "Registration ";
    public static final String OPT = "Ophthalmic Tomography";
    public static final String BDUS = "Bone Densitometry (ultrasound) ";
    public static final String BMD = "Bone Densitometry (X-Ray)";
    public static final String DOC = "Document ";
    public static final String FID = "Fiducials";
    public static final String PLAN = "Plan";
    public static final String IOL = "Intraocular Lens Data";
    public static final String IVOCT = "Intravascular Optical Coherence Tomography";

    public enum Modalities {

        CR(Constants.CR), CT(Constants.CT), MR(Constants.MR), NM(Constants.NM), US(Constants.US), OT(Constants.OT),
        BI(Constants.BI), DG(Constants.DG), ES(Constants.ES), LS(Constants.LS), PT(Constants.PT), RG(Constants.RG),
        TG(Constants.TG), XA(Constants.XA), RF(Constants.RF), RTIMAGE(Constants.RTIMAGE), RTDOSE(Constants.RTDOSE),
        RTSTRUCT(Constants.RTSTRUCT), RTPLAN(Constants.RTPLAN), RTRECORD(Constants.RTRECORD), HC(Constants.HC),
        DX(Constants.DX), MG(Constants.MG), IO(Constants.IO), PX(Constants.PX), GM(Constants.GM), SM(Constants.SM),
        XC(Constants.XC), PR(Constants.PR), AU(Constants.AU), ECG(Constants.ECG), EPS(Constants.EPS), HD(Constants.HD),
        SR(Constants.SR), IVUS(Constants.IVUS), OP(Constants.OP), SMR(Constants.SMR), AR(Constants.AR),
        KER(Constants.KER), VA(Constants.VA), SRF(Constants.SRF), OCT(Constants.OCT), LEN(Constants.LEN),
        OPV(Constants.OPV), OPM(Constants.OPM), OAM(Constants.OAM), RESP(Constants.RESP), KO(Constants.KO),
        SEG(Constants.SEG), REG(Constants.REG), OPT(Constants.OPT), BDUS(Constants.BDUS), BMD(Constants.BMD),
        DOC(Constants.DOC), FID(Constants.FID), PLAN(Constants.PLAN), IOL(Constants.IOL), IVOCT(Constants.IVOCT);

        String desc = "";

        Modalities(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return this.desc;
        }

        @Override
        public String toString() {
            return this.desc;
        }

    }

    public enum NumPages {
        NUM_RECORDS_10(10), NUM_RECORDS_20(20), NUM_RECORDS_50(50), NUM_RECORDS_100(100);

        int value;

        NumPages(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
