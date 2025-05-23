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

/**
 * ViewBean per la vista Confronti_Pacs
 *
 */
import it.eng.spagoLite.db.base.table.AbstractBaseTable;
import it.eng.spagoLite.db.oracle.bean.column.TableDescriptor;

import java.util.Iterator;

/**
 * TableBean per la tabella Confronti_Pacs
 *
 */
public class ConfrontiPacsTableBean extends AbstractBaseTable<ConfrontiPacsRowBean> {

    /*
     * @Generated( value = "it.eg.dbtool.db.oracle.beangen.Oracle4JPAClientBeanGen$ViewBeanWriter", comments =
     * "This class was generated by OraTool", date = "Wednesday, 9 January 2013 13:20" )
     */

    private static final long serialVersionUID = 1L;
    public static ConfrontiPacsTableDescriptor TABLE_DESCRIPTOR = new ConfrontiPacsTableDescriptor();

    public ConfrontiPacsTableBean() {
        super();
    }

    protected ConfrontiPacsRowBean createRow() {
        return new ConfrontiPacsRowBean();
    }

    public TableDescriptor getTableDescriptor() {
        return TABLE_DESCRIPTOR;
    }

    @Deprecated
    public Iterator<ConfrontiPacsRowBean> getRowsIterator() {
        return iterator();
    }
}
