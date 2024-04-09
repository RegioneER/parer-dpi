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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyMethodInvocation;

import it.eng.dpi.exception.DPIException;
import it.eng.spagoLite.db.base.table.AbstractBaseTable;
import it.eng.spagoLite.db.base.table.LazyListReflectionBean;

public class PaginatorAspect implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation inv) throws Throwable {
        try {
            pre(inv);
            Object obj = invokeMethod(inv);
            post(obj);
            return obj;
        } catch (Exception e) {
            throw new DPIException("Exception in PaginatorAspect: " + e.getMessage(), e);
        } finally {
            tLocalLazyList.remove();
        }
    }

    private static ThreadLocal<LazyListReflectionBean> tLocalLazyList = new ThreadLocal<>();

    private void pre(MethodInvocation inv) {
        if (tLocalLazyList.get() == null) {
            ProxyMethodInvocation pminv = (ProxyMethodInvocation) inv;
            LazyListReflectionBean llBean = new LazyListReflectionBean();
            Class<? extends Object> helperEJB = pminv.getThis().getClass();
            Method method = pminv.getMethod();
            // L'interceptor deve entrare in funzione solo con metodi che
            // ritornano un'istanza di AbstractLazyBaseTable; asSubclass() lancia un'eccezione
            method.getReturnType().asSubclass(AbstractBaseTable.class);
            Object[] parameterValue = pminv.getArguments();
            llBean.setClazz(helperEJB);
            llBean.setMethod(method);
            llBean.setParameters(parameterValue);
            // TO
            llBean.setMaxResult((int) parameterValue[parameterValue.length - 1]);
            tLocalLazyList.set(llBean);
        } else {
            ProxyMethodInvocation pminv = (ProxyMethodInvocation) inv;
            Object[] parameterValue = pminv.getArguments();
            // FROM
            parameterValue[parameterValue.length - 2] = tLocalLazyList.get().getFirstResult();

            pminv.setArguments(parameterValue);
        }

    }

    private Object invokeMethod(MethodInvocation inv) throws Throwable {
        return inv.proceed();
    }

    private void post(Object obj) {
        if (obj instanceof AbstractBaseTable) {
            // Setto il lazybean nel tablebean per le successive invocazioni
            AbstractBaseTable<?> tableBean = (AbstractBaseTable<?>) obj;
            if (tableBean.getLazyListReflectionBean() == null) {
                tableBean.setLazyListReflectionBean(tLocalLazyList.get());
            }
        }
    }

    public static LazyListReflectionBean getLazyListReflectionBean() {
        return tLocalLazyList.get();
    }

    public static void setLazyListReflectionBean(LazyListReflectionBean llBean) {
        tLocalLazyList.set(llBean);
    }
}
