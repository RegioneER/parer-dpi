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

package it.eng.dpi.paginator;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

import it.eng.spagoLite.db.base.table.LazyListReflectionBean;

public class CountRecordAspect implements AfterReturningAdvice {

    private final static String RECORD_TOTALI = "getNiRecordTotale";

    @Override
    public void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable {
        LazyListReflectionBean llBean = PaginatorAspect.getLazyListReflectionBean();
        if (llBean != null) {
            int countResult = (int) returnValue.getClass().getMethod(RECORD_TOTALI, null).invoke(returnValue);
            llBean.setCountResultSize(countResult);
        }

    }

}
