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

package it.eng.dpi.web.bean;

import it.eng.spagoLite.db.oracle.bean.column.ColumnDescriptor;
import it.eng.spagoLite.db.oracle.bean.column.TableDescriptor;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sloth
 *
 *         Bean per la tabella Pig_V_Ric_Recup
 *
 */
public class PigVRicRecupTableDescriptor extends TableDescriptor {

    /*
     * @Generated( value = "it.eg.dbtool.db.oracle.beangen.Oracle4JPAClientBeanGen$ViewBeanWriter", comments =
     * "This class was generated by OraTool", date = "Thursday, 24 January 2013 17:54" )
     */

    public static final String SELECT = "Select * from Pig_V_Ric_Recup /**/";
    public static final String TABLE_NAME = "Pig_V_Ric_Recup";
    public static final String COL_ID_OBJECT = "id_object";
    public static final String COL_CD_KEY_OBJECT = "cd_key_object";
    public static final String COL_TI_STATO_SESSIONE = "ti_stato_sessione";
    public static final String COL_DT_APERTURA_SESSIONE = "dt_apertura_sessione";
    public static final String COL_DT_CHIUSURA_SESSIONE = "dt_chiusura_sessione";
    public static final String COL_ID_SESSIONE = "id_sessione";
    public static final String COL_CD_ERR = "cd_err";
    public static final String COL_DL_ERR = "dl_err";
    public static final String COL_CHIAVE_UNITA_DOC = "chiave_unita_doc";
    public static final String COL_CD_AET_NODO_DICOM = "cd_aet_nodo_dicom";
    public static final String COL_DT_STUDY_DATE = "dt_study_date";
    public static final String COL_DS_ACCESSION_NUMBER = "ds_accession_number";
    public static final String COL_DS_PATIENT_NAME = "ds_patient_name";
    public static final String COL_TI_PATIENT_SEX = "ti_patient_sex";
    public static final String COL_DT_PATIENT_BIRTH_DATE = "dt_patient_birth_date";
    public static final String COL_CD_PATIENT_ID = "cd_patient_id";
    public static final String COL_CD_PATIENT_ID_ISSUER = "cd_patient_id_issuer";
    public static final String COL_DS_REF_PHYSICIAN_NAME = "ds_ref_physician_name";
    public static final String COL_DL_LISTA_MODALITY_IN_STUDY = "dl_lista_modality_in_study";

    private static Map<String, ColumnDescriptor> map = new LinkedHashMap<String, ColumnDescriptor>();

    static {
        map.put(COL_ID_OBJECT, new ColumnDescriptor(COL_ID_OBJECT, Types.DECIMAL, 22, true));
        map.put(COL_CD_KEY_OBJECT, new ColumnDescriptor(COL_CD_KEY_OBJECT, Types.VARCHAR, 100, true));
        map.put(COL_TI_STATO_SESSIONE, new ColumnDescriptor(COL_TI_STATO_SESSIONE, Types.VARCHAR, 20, true));
        map.put(COL_DT_APERTURA_SESSIONE, new ColumnDescriptor(COL_DT_APERTURA_SESSIONE, Types.TIMESTAMP, 7, true));
        map.put(COL_DT_CHIUSURA_SESSIONE, new ColumnDescriptor(COL_DT_CHIUSURA_SESSIONE, Types.TIMESTAMP, 7, true));
        map.put(COL_ID_SESSIONE, new ColumnDescriptor(COL_ID_SESSIONE, Types.DECIMAL, 22, true));
        map.put(COL_CD_ERR, new ColumnDescriptor(COL_CD_ERR, Types.VARCHAR, 100, true));
        map.put(COL_DL_ERR, new ColumnDescriptor(COL_DL_ERR, Types.VARCHAR, 1024, true));
        map.put(COL_CHIAVE_UNITA_DOC, new ColumnDescriptor(COL_CHIAVE_UNITA_DOC, Types.VARCHAR, 242, true));
        map.put(COL_CD_AET_NODO_DICOM, new ColumnDescriptor(COL_CD_AET_NODO_DICOM, Types.VARCHAR, 100, true));
        map.put(COL_DT_STUDY_DATE, new ColumnDescriptor(COL_DT_STUDY_DATE, Types.TIMESTAMP, 7, true));
        map.put(COL_DS_ACCESSION_NUMBER, new ColumnDescriptor(COL_DS_ACCESSION_NUMBER, Types.VARCHAR, 254, true));
        map.put(COL_DS_PATIENT_NAME, new ColumnDescriptor(COL_DS_PATIENT_NAME, Types.VARCHAR, 100, true));
        map.put(COL_TI_PATIENT_SEX, new ColumnDescriptor(COL_TI_PATIENT_SEX, Types.VARCHAR, 1, true));
        map.put(COL_DT_PATIENT_BIRTH_DATE, new ColumnDescriptor(COL_DT_PATIENT_BIRTH_DATE, Types.TIMESTAMP, 7, true));
        map.put(COL_CD_PATIENT_ID, new ColumnDescriptor(COL_CD_PATIENT_ID, Types.VARCHAR, 100, true));
        map.put(COL_CD_PATIENT_ID_ISSUER, new ColumnDescriptor(COL_CD_PATIENT_ID_ISSUER, Types.VARCHAR, 100, true));
        map.put(COL_DS_REF_PHYSICIAN_NAME, new ColumnDescriptor(COL_DS_REF_PHYSICIAN_NAME, Types.VARCHAR, 254, true));
        map.put(COL_DL_LISTA_MODALITY_IN_STUDY,
                new ColumnDescriptor(COL_DL_LISTA_MODALITY_IN_STUDY, Types.VARCHAR, 2048, true));
    }

    public Map<String, ColumnDescriptor> getColumnMap() {
        return map;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public String getStatement() {
        return SELECT;
    }

}
