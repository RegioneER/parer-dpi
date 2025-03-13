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
 *         Bean per la tabella Pig_V_Ric_Diario
 *
 */
public class ConfrontiPacsTableDescriptor extends TableDescriptor {

    /*
     * @Generated( value = "it.eg.dbtool.db.oracle.beangen.Oracle4JPAClientBeanGen$ViewBeanWriter", comments =
     * "This class was generated by OraTool", date = "Wednesday, 9 January 2013 13:20" )
     */

    public static final String SELECT = "Select * from Confronti_Pacs /**/";
    public static final String TABLE_NAME = "Confronti_Pacs";
    public static final String COL_NUM_STUDY_PING = "num_study_ping";
    public static final String COL_NUM_STUDY_PACS = "num_study_pacs";
    public static final String COL_PACS_AET = "pacs_aet";
    public static final String COL_NUM_STUDY_DA_TRASFERIRE = "num_study_da_trasferire";

    private static Map<String, ColumnDescriptor> map = new LinkedHashMap<String, ColumnDescriptor>();

    static {
        map.put(COL_NUM_STUDY_PING, new ColumnDescriptor(COL_NUM_STUDY_PING, Types.DECIMAL, 22, true));
        map.put(COL_NUM_STUDY_PACS, new ColumnDescriptor(COL_NUM_STUDY_PACS, Types.DECIMAL, 22, true));
        map.put(COL_PACS_AET, new ColumnDescriptor(COL_PACS_AET, Types.VARCHAR, 100, true));
        map.put(COL_NUM_STUDY_DA_TRASFERIRE,
                new ColumnDescriptor(COL_NUM_STUDY_DA_TRASFERIRE, Types.DECIMAL, 22, true));
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
