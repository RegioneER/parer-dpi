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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import it.eng.spagoLite.db.base.BaseTableInterface;
import it.eng.spagoLite.db.base.paging.AbstractPaginator;
import it.eng.spagoLite.db.base.table.AbstractBaseTable;
import it.eng.spagoLite.db.base.table.LazyListInterface;
import it.eng.spagoLite.db.base.table.LazyListReflectionBean;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PaginatorImpl extends AbstractPaginator {

    @Autowired
    private ApplicationContext context;
    Logger log = LoggerFactory.getLogger(PaginatorImpl.class);

    public <T> AbstractBaseTable invokeEJBMethod(Class<T> helperEJB, Method method, Object[] parameterValue) {
        T helper = context.getBean(helperEJB);
        try {
            return (AbstractBaseTable) method.invoke(helper, parameterValue);
        } catch (IllegalAccessException ex) {
            log.error("Visibilità del metodo " + method + " dell'helper " + helperEJB.getName() + " non corretta ", ex);
        } catch (IllegalArgumentException ex) {
            log.error(
                    "Argomenti passati al metodo " + method + " dell'helper " + helperEJB.getName() + " non corretti ",
                    ex);
        } catch (InvocationTargetException ex) {
            log.error("L'invocazione al metodo " + method + " dell'helper " + helperEJB.getName()
                    + " ha prodotto un'eccezione. Segue stacktrace..", ex);
        } catch (SecurityException ex) {
            log.error("Security Exception", ex);
        }
        return null;
    }

    @Override
    protected BaseTableInterface<?> invoke(LazyListInterface lazyListInterface) {
        LazyListReflectionBean lazyList = (LazyListReflectionBean) lazyListInterface;
        PaginatorAspect.setLazyListReflectionBean(lazyList);
        return invokeEJBMethod(lazyList.getClazz(), lazyList.getMethod(), lazyList.getParameters());
    }
}
