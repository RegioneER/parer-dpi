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

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType;
import it.eng.sacerasi.ws.xml.diariofiltri.ListaFiltriType;
import it.eng.sacerasi.ws.xml.diarioorder.ListaDatiSpecificiOrderType;
import it.eng.sacerasi.ws.xml.diarioout.ListaDatiSpecificiOutType;
import it.eng.sacerasi.ws.xml.diarioresult.ListaValoriDatiSpecificiType;
import it.eng.sacerasi.ws.xml.invioasync.ListaUnitaDocumentarieType;

@Component
public class JAXBSingleton {

    private static final Logger log = LoggerFactory.getLogger(JAXBSingleton.class);

    private JAXBContext contextListaUnitaDocumentarieType;
    private JAXBContext contextListaDatiSpecificiOutType;
    private JAXBContext contextListaFiltriType;
    private JAXBContext contextListaValoriDatiSpecificiType;
    private JAXBContext contextListaDatiSpecificiOrderType;

    @PostConstruct
    public void postConstruct() {
        try {
            contextListaUnitaDocumentarieType = JAXBContext.newInstance(ListaUnitaDocumentarieType.class,
                    DatiSpecificiType.class);
            contextListaDatiSpecificiOutType = JAXBContext.newInstance(ListaDatiSpecificiOutType.class);
            contextListaFiltriType = JAXBContext.newInstance(ListaFiltriType.class);
            contextListaValoriDatiSpecificiType = JAXBContext.newInstance(ListaValoriDatiSpecificiType.class);
            contextListaDatiSpecificiOrderType = JAXBContext.newInstance(ListaDatiSpecificiOrderType.class);
        } catch (JAXBException e) {
            log.error("Errore nella configurazione di JAXB", e);
        }
    }

    public JAXBContext getContextListaUnitaDocumentarieType() {
        return contextListaUnitaDocumentarieType;
    }

    public JAXBContext getContextListaDatiSpecificiOutType() {
        return contextListaDatiSpecificiOutType;
    }

    public JAXBContext getContextListaFiltriType() {
        return contextListaFiltriType;
    }

    public JAXBContext getContextListaValoriDatiSpecificiType() {
        return contextListaValoriDatiSpecificiType;
    }

    public JAXBContext getContextListaDatiSpecificiOrderType() {
        return contextListaDatiSpecificiOrderType;
    }
}
