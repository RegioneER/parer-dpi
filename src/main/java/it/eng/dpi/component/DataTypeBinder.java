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

package it.eng.dpi.component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataTypeBinder {
    private static DateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static DateFormat date = new SimpleDateFormat("yyyy-MM-dd");

    public static Date unmarshalDate(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        Date d = null;

        try {
            d = date.parse(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

    public static String marshalDate(Date value) {
        if (value == null) {
            return null;
        }

        return date.format(value.getTime());
    }

    public static Date unmarshalDateTime(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        Date d = null;

        try {
            d = dateTime.parse(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return d;
    }

}
