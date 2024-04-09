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

package it.eng.dpi.business.impl;

import it.eng.dpi.business.AbstractWSClientAOP;
import it.eng.dpi.component.JAXBSingleton;
import it.eng.dpi.component.Messages;
import it.eng.dpi.component.Util;
import it.eng.dpi.exception.WebGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.dpi.web.bean.PigVRicDiarioRowBean;
import it.eng.dpi.web.bean.PigVRicDiarioTableBean;
import it.eng.dpi.web.bean.PigVRicRecupRowBean;
import it.eng.dpi.web.bean.PigVRicRecupTableBean;
import it.eng.dpi.web.bean.RicDiarioObjToRowBean;
import it.eng.dpi.web.bean.RicRecupObjToRowBean;
import it.eng.dpi.web.util.Constants;
import it.eng.dpi.web.util.Constants.DatiSpecDicom;
import it.eng.sacerasi.ws.EsitoServizio;
import it.eng.sacerasi.ws.ListaOggRicDiarioType;
import it.eng.sacerasi.ws.ListaOggRicRestOggType;
import it.eng.sacerasi.ws.OggettoRicDiarioType;
import it.eng.sacerasi.ws.OggettoRicRestOggType;
import it.eng.sacerasi.ws.RicercaDiario;
import it.eng.sacerasi.ws.RicercaDiarioRisposta;
import it.eng.sacerasi.ws.RicercaRestituzioniOggetti;
import it.eng.sacerasi.ws.RicercaRestituzioniOggettiRisposta;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType.FiltroDueValori;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType.FiltroUnValore;
import it.eng.sacerasi.ws.xml.diariofiltri.ListaFiltriType;
import it.eng.sacerasi.ws.xml.diarioorder.ListaDatiSpecificiOrderType;
import it.eng.sacerasi.ws.xml.diarioorder.OrderType;
import it.eng.sacerasi.ws.xml.diarioout.ListaDatiSpecificiOutType;
import it.eng.sacerasi.ws.xml.diarioresult.ListaValoriDatiSpecificiType;
import it.eng.sacerasi.ws.xml.diarioresult.ValoreDatoSpecificoType;
import it.eng.spagoLite.db.base.BaseRowInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
//DEPRECATED import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RicercaService extends AbstractWSClientAOP {
    private static final Logger log = LoggerFactory.getLogger(RicercaService.class);

    private RicercaDiario ricDiarioClient;

    private RicercaRestituzioniOggetti ricRestClient;

    @Autowired
    private JAXBSingleton jaxbSingleton;

    @Autowired
    private Messages messagesCtx;

    private FiltroType creaFiltroUnValore(String datoSpec, String operatore, String valore) {
        FiltroType filtro = new FiltroType();
        FiltroUnValore filtroUnValore = new FiltroUnValore();
        filtroUnValore.setDatoSpecifico(datoSpec);
        filtroUnValore.setOperatore(operatore);
        filtroUnValore.setValore(valore);
        filtro.setFiltroUnValore(filtroUnValore);
        return filtro;
    }

    private FiltroType creaFiltroDueValori(String datoSpec, String operatore, String valore1, String valore2) {
        FiltroType filtro = new FiltroType();
        FiltroDueValori filtroDueValori = new FiltroDueValori();
        filtroDueValori.setDatoSpecifico(datoSpec);
        filtroDueValori.setOperatoreCompreso(operatore);
        filtroDueValori.setValore1(valore1);
        filtroDueValori.setValore2(valore2);
        filtro.setFiltroDueValori(filtroDueValori);
        return filtro;
    }

    private OrderType creaDatoSpecificoOrder(String datoSpec, String tipoOrder) {
        OrderType order = new OrderType();
        order.setDatoSpecifico(datoSpec);
        order.setTipoOrder(tipoOrder);
        return order;
    }

    /**
     * Esegue la chiamata a ricerca diario utilizzando i filtri assegnati
     * 
     * IMPORTANTE: gli ultimi due parametri devono rimanere i due interi FROM e TO per far funzionare la paginazione
     * 
     * @param filtri
     *            i filtri assegnati
     * @param dateStudy
     *            l'array di date già validate
     * @param datePresaInCarico
     *            l'array di date presa in carico
     * @param from
     *            l'indice di posizione per la paginazione
     * @param to
     *            il numero di record da visualizzare
     * 
     * @return il tablebean popolato
     * 
     * @throws JAXBException
     *             eccezione generica
     * @throws WebGenericException
     *             eccezione generica
     */
    public PigVRicDiarioTableBean callRicercaDiarioService(
            it.eng.dpi.slite.gen.form.MonitoraggioForm.RicercaDiario filtri, Date[] dateStudy, Date[] datePresaInCarico,
            int from, int to) throws JAXBException, WebGenericException {
        if (ricDiarioClient == null) {
            ricDiarioClient = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class, "wsRicercaDiarioAdvisor");
        }
        if (ricDiarioClient == null) {
            log.debug("Ricerca Diario - client non disponibile");
            throw new WebGenericException(messagesCtx.getServiceUnavailable(RicercaDiario.class.getSimpleName()));
        }
        log.debug("Ricerca Diario - client inizializzato");
        // Chiamo il servizio di ricerca diario in base ai parametri forniti
        // Creo la lista dei datispecifici in output
        ListaDatiSpecificiOutType xmlout = new ListaDatiSpecificiOutType();
        // Inserisco all'interno della lista i dati necessari all'output,
        // definito per semplicitÃ  in un enum delle costanti
        for (DatiSpecDicom datoSpec : Constants.DatiSpecDicom.getRicercaOutputEnums()) {
            xmlout.getDatoSpecificoOut().add(datoSpec.toString());
        }

        // Creo la lista dei filtri di richiesta in base ai parametri forniti
        ListaFiltriType xmlfiltri = new ListaFiltriType();

        // Aggiungo ogni filtro alla richiesta se e solo se è popolato

        // AETNodoDicom
        if (StringUtils.isNotBlank(filtri.getNodo_dicom().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.AETNodoDicom,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getNodo_dicom().getValue()));
        }

        // StudyDate
        if (dateStudy[0] != null || dateStudy[1] != null) {
            DateFormat timeStampDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            String operatore;
            FiltroType filtro = null;
            if (dateStudy[0] != null && dateStudy[1] != null) {
                operatore = Constants.ValoriOperatore.COMPRESO_FRA.name();
                filtro = creaFiltroDueValori(Constants.StudyDate, operatore, timeStampDf.format(dateStudy[0]),
                        timeStampDf.format(dateStudy[1]));
            } else if (dateStudy[0] != null) {
                operatore = Constants.ValoriOperatore.MAGGIORE_UGUALE.name();
                filtro = creaFiltroUnValore(Constants.StudyDate, operatore, timeStampDf.format(dateStudy[0]));
            } else if (dateStudy[1] != null) {
                operatore = Constants.ValoriOperatore.MINORE_UGUALE.name();
                filtro = creaFiltroUnValore(Constants.StudyDate, operatore, timeStampDf.format(dateStudy[1]));
            }
            xmlfiltri.getFiltro().add(filtro);
        }

        // DataPresaInCarico
        if (datePresaInCarico[0] != null || datePresaInCarico[1] != null) {
            DateFormat timeStampDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            String operatore;
            FiltroType filtro = null;
            if (datePresaInCarico[0] != null && datePresaInCarico[1] != null) {
                operatore = Constants.ValoriOperatore.COMPRESO_FRA.name();
                filtro = creaFiltroDueValori(Constants.DataPresaInCarico, operatore,
                        timeStampDf.format(datePresaInCarico[0]), timeStampDf.format(datePresaInCarico[1]));
            } else if (datePresaInCarico[0] != null) {
                operatore = Constants.ValoriOperatore.MAGGIORE_UGUALE.name();
                filtro = creaFiltroUnValore(Constants.DataPresaInCarico, operatore,
                        timeStampDf.format(datePresaInCarico[0]));
            } else if (datePresaInCarico[1] != null) {
                operatore = Constants.ValoriOperatore.MINORE_UGUALE.name();
                filtro = creaFiltroUnValore(Constants.DataPresaInCarico, operatore,
                        timeStampDf.format(datePresaInCarico[1]));
            }
            xmlfiltri.getFiltro().add(filtro);
        }

        // AccessionNumber
        if (StringUtils.isNotBlank(filtri.getAccession_number().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.AccessionNumber,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getAccession_number().getValue()));
        }

        // PatientName
        if (StringUtils.isNotBlank(filtri.getPaziente().getValue())) {
            if (StringUtils.isNotBlank(filtri.getFiltro_paziente().getValue())) {
                xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientName,
                        filtri.getFiltro_paziente().getValue(), filtri.getPaziente().getValue()));
            } else {
                log.debug("Errore - filtro paziente non disponibile");
                throw new WebGenericException(messagesCtx.getRicercaDiarioFiltroPazUnavailable());
            }
        }

        // PatientSex
        if (StringUtils.isNotBlank(filtri.getSesso_paziente().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientSex, Constants.ValoriOperatore.UGUALE.name(),
                    filtri.getSesso_paziente().getValue()));
        }

        // PatientBirthDate
        if (StringUtils.isNotBlank(filtri.getDt_nascita_paziente().getValue())) {
            DateFormat dateDf = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat webDf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                xmlfiltri.getFiltro()
                        .add(creaFiltroUnValore(Constants.PatientBirthDate, Constants.ValoriOperatore.UGUALE.name(),
                                dateDf.format(webDf.parse(filtri.getDt_nascita_paziente().getValue()))));
            } catch (ParseException e) {
                throw new WebGenericException("Errore di lettura della data di nascita");
            }
        }

        // PatientId
        if (StringUtils.isNotBlank(filtri.getId_paziente().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientId, Constants.ValoriOperatore.UGUALE.name(),
                    filtri.getId_paziente().getValue()));
        }

        // PatientIdIssuer
        if (StringUtils.isNotBlank(filtri.getIssuer_id_paziente().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientIdIssuer,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getIssuer_id_paziente().getValue()));
        }

        // // ReferringPhysicianName
        // if (StringUtils.isNotBlank(filtri.getMedico().getValue())) {
        // xmlfiltri.getFiltro().add(
        // creaFiltroUnValore(Constants.ReferringPhysicianName,
        // Constants.ValoriOperatore.CONTIENE.name(),
        // filtri.getMedico().getValue()));
        // }

        // ModalityStudyList
        if (StringUtils.isNotBlank(filtri.getModality_study().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.ModalityInStudyList,
                    Constants.ValoriOperatore.CONTIENE.name(), filtri.getModality_study().getValue()));
        }

        // StudyInstanceUID
        if (StringUtils.isNotBlank(filtri.getStudy_uid().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.StudyInstanceUID,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getStudy_uid().getValue()));
        }

        // DcmHash
        if (StringUtils.isNotBlank(filtri.getDcm_hash().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.DCM_hash, Constants.ValoriOperatore.UGUALE.name(),
                    filtri.getDcm_hash().getValue()));
        }
        // Creo la lista di ordine dei dati specifici
        ListaDatiSpecificiOrderType xmlorder = new ListaDatiSpecificiOrderType();
        xmlorder.getDatoSpecificoOrder().add(creaDatoSpecificoOrder(Constants.PatientName, Constants.ASCENDING));
        xmlorder.getDatoSpecificoOrder().add(creaDatoSpecificoOrder(Constants.StudyDate, Constants.ASCENDING));

        // Eseguo il marshalling dei tre xml predisposti sopra
        StringWriter swXmlOut = new StringWriter();
        Marshaller m = jaxbSingleton.getContextListaDatiSpecificiOutType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaDatiSpecificiOutType> xmlOutJAXB = new it.eng.sacerasi.ws.xml.diarioout.ObjectFactory()
                .createListaDatiSpecificiOut(xmlout);
        m.marshal(xmlOutJAXB, swXmlOut);

        StringWriter swXmlFiltri = new StringWriter();
        m = jaxbSingleton.getContextListaFiltriType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaFiltriType> xmlFiltriJAXB = new it.eng.sacerasi.ws.xml.diariofiltri.ObjectFactory()
                .createListaFiltri(xmlfiltri);
        m.marshal(xmlFiltriJAXB, swXmlFiltri);

        StringWriter swXmlOrder = new StringWriter();
        m = jaxbSingleton.getContextListaDatiSpecificiOrderType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaDatiSpecificiOrderType> xmlOrderJAXB = new it.eng.sacerasi.ws.xml.diarioorder.ObjectFactory()
                .createListaDatiSpecificiOrder(xmlorder);
        m.marshal(xmlOrderJAXB, swXmlOrder);

        // Eseguo la chiamata a ricerca diario con i parametri definiti
        // nmAmbiente, nmVersatore, cdPassword, nmTipoObject, cdKeyObject,
        // idSessione, tiStatoObject, flTutteSessioni, niRecordInizio,
        // niRecordResultSet, xmlDatiSpecOutput, xmlDatiSpecFiltri,
        // xmlDatiSpecOrder
        log.debug("Eseguo la chiamata ricerca diario");
        RicercaDiarioRisposta risp = ricDiarioClient.ricercaDiario(ctx.getNmAmbiente(), ctx.getNmVersatore(),
                DPIConstants.WS_NM_TIPO_OBJECT, null, null,
                StringUtils.isBlank(filtri.getTi_stato().getValue()) ? null : filtri.getTi_stato().getValue(),
                filtri.getRicerca_ogni_sessione().getValue().equals("1"), from + 1, to, swXmlOut.toString(),
                swXmlFiltri.toString(), swXmlOrder.toString());

        // Istanzio un tableBean vuoto
        PigVRicDiarioTableBean tb = new PigVRicDiarioTableBean();
        // Se la chiamata ha avuto esito OK e la lista di oggetti non è vuota
        if (risp != null && risp.getCdEsito().value().equals(EsitoServizio.OK.name())
                && risp.getListaOggetti() != null) {
            // Eseguo l'unmarshall dei dati specifici contenuti in ogni oggetto
            // Creo il bean di supporto RicDiarioObjToRowBean e popolo il
            // rowbean da aggiungere
            // al tablebean istanziato
            log.debug("Popolo la lista di risultati");
            ListaOggRicDiarioType listaOggetti = risp.getListaOggetti();
            Unmarshaller um = jaxbSingleton.getContextListaValoriDatiSpecificiType().createUnmarshaller();
            for (OggettoRicDiarioType ogg : listaOggetti.getOggetto()) {
                RicDiarioObjToRowBean obj = new RicDiarioObjToRowBean(ogg);
                JAXBElement<ListaValoriDatiSpecificiType> datoSpecJAXB = (JAXBElement<ListaValoriDatiSpecificiType>) um
                        .unmarshal(new StreamSource(new StringReader(ogg.getXmlDatiSpecResult())),
                                ListaValoriDatiSpecificiType.class);
                ListaValoriDatiSpecificiType datoSpec = datoSpecJAXB.getValue();
                try {
                    // Estraggo la classe di RicDiarioObjToRowBean, che
                    // dovrà  contenere i valori dei dati specifici
                    Class<?> c = Class.forName(RicDiarioObjToRowBean.class.getName());
                    // Istanzio il metodo definito come set + nome del campo
                    // da popolare del bean, capitalizzando il nome del
                    // metodo
                    // ES: cdAETnododicom -> setCdAetNodoDicom
                    Map<String, Class<?>[]> map = new HashMap<String, Class<?>[]>();
                    for (Method method : c.getDeclaredMethods()) {
                        map.put(method.getName(), method.getParameterTypes());
                    }
                    for (ValoreDatoSpecificoType dato : datoSpec.getValoreDatoSpecifico()) {

                        String nomeCampo = Constants.DatiSpecDicom.getEnum(dato.getDatoSpecifico()).getMethod();

                        Method method;
                        String methodName = "set" + WordUtils.capitalize(nomeCampo, null);
                        Class<?>[] params = map.get(methodName);
                        method = c.getDeclaredMethod(methodName, params);
                        // Invoca il metodo ottenuto sulla tabella
                        // RicDiarioObjToRowBean
                        // dell'oggetto per impostare il valore del dato
                        // specifico
                        Object value = null;
                        if (dato.getValore() != null) {
                            if (params[0].equals(String.class)) {
                                value = dato.getValore();
                            } else if (params[0].equals(Date.class)) {
                                DateFormat dateDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                value = dateDf.parse(dato.getValore());
                            } else if (params[0].equals(Timestamp.class)) {
                                DateFormat timeStampDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                value = timeStampDf.parse(dato.getValore());
                            } else if (params[0].equals(BigDecimal.class)) {
                                value = new BigDecimal(dato.getValore());
                            }
                        }
                        method.invoke(obj, value);

                    }
                } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException | ParseException e) {
                    log.error("Errore di popolamento del bean RicDiarioObjToRowBean", e);
                }
                PigVRicDiarioRowBean row = new PigVRicDiarioRowBean();
                row.entityToRowBean(obj);
                tb.add(row);
            }
        } else if (risp != null && risp.getCdEsito().value().equals(EsitoServizio.KO.name())) {
            log.debug("Errore nella chiamata di ricerca diario : " + risp.getCdErr() + " : " + risp.getDsErr());
            throw new WebGenericException(messagesCtx.getServiceError(risp.getCdErr(), risp.getDsErr()));
        }
        return tb;
    }

    /**
     * Esegue la chiamata per ottenere il dettaglio di uno studio
     * 
     * @param currentRow
     *            riga della lista da caricare in dettaglio
     * @param fromRicDiario
     *            boolean che indica se si carica il dettaglio da ricerca diario e non è il refresh del dettaglio
     * 
     * @return il rowBean del dettaglio
     * 
     * @throws WebGenericException
     *             eccezione generica
     * @throws JAXBException
     *             eccezione generica
     */
    public PigVRicDiarioRowBean callDettaglioRicercaDiarioService(BaseRowInterface currentRow, boolean fromRicDiario)
            throws WebGenericException, JAXBException {
        if (ricDiarioClient == null) {
            ricDiarioClient = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class, "wsRicercaDiarioAdvisor");
        }
        if (ricDiarioClient == null) {
            log.debug("Ricerca Diario - client non disponibile");
            throw new WebGenericException(messagesCtx.getServiceUnavailable(RicercaDiario.class.getSimpleName()));
        }
        log.debug("Ricerca Diario - client inizializzato");
        // Chiamo il servizio di ricerca diario in base ai parametri forniti
        // Creo la lista dei datispecifici in output
        ListaDatiSpecificiOutType xmlout = new ListaDatiSpecificiOutType();
        // Inserisco all'interno della lista i dati necessari all'output,
        // definito per semplicità  in un enum delle costanti
        for (DatiSpecDicom datoSpec : Constants.DatiSpecDicom.getDettaglioDiarioOutputEnums()) {
            xmlout.getDatoSpecificoOut().add(datoSpec.toString());
        }

        // Eseguo il marshalling dell' xml di output
        StringWriter swXmlOut = new StringWriter();
        Marshaller m = jaxbSingleton.getContextListaDatiSpecificiOutType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaDatiSpecificiOutType> xmlOutJAXB = new it.eng.sacerasi.ws.xml.diarioout.ObjectFactory()
                .createListaDatiSpecificiOut(xmlout);
        m.marshal(xmlOutJAXB, swXmlOut);

        String cdKeyObject = currentRow.getString("cd_key_object");
        BigDecimal idSessione = currentRow.getBigDecimal("id_sessione");

        PigVRicDiarioRowBean detRow = new PigVRicDiarioRowBean();

        // Eseguo la chiamata a ricerca diario con i parametri definiti
        // nmAmbiente, nmVersatore, cdPassword, nmTipoObject, cdKeyObject,
        // idSessione, tiStatoObject, flTutteSessioni, niRecordInizio,
        // niRecordResultSet, xmlDatiSpecOutput, xmlDatiSpecFiltri,
        // xmlDatiSpecOrder
        log.debug("Eseguo la chiamata ricercaDiario per visualizzare il dettaglio dello studio " + cdKeyObject);
        RicercaDiarioRisposta risp = ricDiarioClient.ricercaDiario(ctx.getNmAmbiente(), ctx.getNmVersatore(),
                DPIConstants.WS_NM_TIPO_OBJECT, cdKeyObject, fromRicDiario ? idSessione.longValue() : null, null,
                fromRicDiario, 1, 1, swXmlOut.toString(), null, null);
        // Se la chiamata ha avuto esito OK e la lista di oggetti non è vuota
        if (risp != null && risp.getCdEsito().value().equals(EsitoServizio.OK.name())
                && risp.getListaOggetti() != null) {
            log.debug("Popolo il dettaglio");
            // Eseguo l'unmarshall dei dati specifici contenuti in ogni oggetto
            // Creo il bean di supporto RicDiarioObjToRowBean e popolo il
            // rowbean da aggiungere
            // al tablebean istanziato
            ListaOggRicDiarioType listaOggetti = risp.getListaOggetti();
            Unmarshaller um = jaxbSingleton.getContextListaValoriDatiSpecificiType().createUnmarshaller();
            for (OggettoRicDiarioType ogg : listaOggetti.getOggetto()) {
                RicDiarioObjToRowBean obj = new RicDiarioObjToRowBean(ogg);
                JAXBElement<ListaValoriDatiSpecificiType> datoSpecJAXB = (JAXBElement<ListaValoriDatiSpecificiType>) um
                        .unmarshal(new StreamSource(new StringReader(ogg.getXmlDatiSpecResult())),
                                ListaValoriDatiSpecificiType.class);
                ListaValoriDatiSpecificiType datoSpec = datoSpecJAXB.getValue();
                try {
                    // Estraggo la classe di RicDiarioObjToRowBean, che
                    // dovrà  contenere i valori dei dati specifici
                    Class<?> c = Class.forName(RicDiarioObjToRowBean.class.getName());
                    // Istanzio il metodo definito come set + nome del campo
                    // da popolare del bean, capitalizzando il nome del
                    // metodo
                    // ES: cdAETnododicom -> setCdAetNodoDicom
                    Map<String, Class<?>[]> map = new HashMap<String, Class<?>[]>();
                    for (Method method : c.getDeclaredMethods()) {
                        map.put(method.getName(), method.getParameterTypes());
                    }
                    for (ValoreDatoSpecificoType dato : datoSpec.getValoreDatoSpecifico()) {

                        String nomeCampo = Constants.DatiSpecDicom.getEnum(dato.getDatoSpecifico()).getMethod();

                        Method method;
                        String methodName = "set" + WordUtils.capitalize(nomeCampo, null);
                        Class<?>[] params = map.get(methodName);
                        method = c.getDeclaredMethod(methodName, params);
                        // Invoca il metodo ottenuto sulla tabella
                        // RicDiarioObjToRowBean
                        // dell'oggetto per impostare il valore del dato
                        // specifico
                        Object value = null;
                        if (dato.getValore() != null) {
                            if (params[0].equals(String.class)) {
                                value = dato.getValore();
                            } else if (params[0].equals(Date.class)) {
                                DateFormat dateDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                value = dateDf.parse(dato.getValore());
                            } else if (params[0].equals(Timestamp.class)) {
                                DateFormat timeStampDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                value = timeStampDf.parse(dato.getValore());
                            } else if (params[0].equals(BigDecimal.class)) {
                                value = new BigDecimal(dato.getValore());
                            }
                        }
                        method.invoke(obj, value);
                    }
                } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException | ParseException e) {
                    log.error("Errore di popolamento del bean RicDiarioObjToRowBean", e);
                }
                detRow.entityToRowBean(obj);
                // Imposto a posteriori alcuni dati in quanto dipende dalla
                // presenza dello studio all'interno della directory notificati
                if (detRow.getTiStatoSessioneRecup() != null
                        && detRow.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_OK.name())) {
                    String dicomPath = ctx.getWorkingPath() + ctx.getStudioDicomPath();
                    File notifDir = new File(dicomPath.concat(DPIConstants.NOTIFICATI_FOLDER),
                            detRow.getDsGlobalHash());
                    if (notifDir.exists()) {
                        detRow.setFlTransferDicom(Constants.DB_FALSE);
                        String[] fileNames = notifDir.list();
                        String udFileName = Constants.UD_FILE_PREFIX_DPI.concat(detRow.getDsGlobalHash())
                                .concat(Constants.ZIP_EXTENSION);
                        String pcFileName = Constants.PC_FILE_PREFIX_DPI.concat(detRow.getDsGlobalHash())
                                .concat(Constants.ZIP_EXTENSION);
                        String zipPathUdFile = notifDir + File.separator + udFileName;
                        String zipPathPcFile = notifDir + File.separator + pcFileName;
                        try {
                            // Verifico che all'interno della directory esista
                            // il
                            // file UD-<GlobalHash>.zip
                            if (Arrays.asList(fileNames).contains(udFileName)) {
                                // verifico se esiste la cartella con nome
                                // registro-anno-numero
                                if (Util.verificaPresenzaDirInZip(zipPathUdFile, detRow.getChiaveUnitaDoc())) {
                                    detRow.setFlTransferDicom(Constants.DB_TRUE);
                                } else {
                                    detRow.setFlTransferDicom(Constants.DB_FALSE);
                                }
                                // Estraggo il file di indice
                                try {
                                    String xml = new String(
                                            Util.estraiFileDaZip(zipPathUdFile, Constants.UD_INDEX_FILE),
                                            Constants.UTF_ENCODING);
                                    detRow.setBlXmlIndiceUd(xml);
                                } catch (FileNotFoundException e) {
                                    throw new WebGenericException(messagesCtx.getNoUdIndexFile());
                                }
                            } else {
                                throw new WebGenericException(messagesCtx.getNoUdFile());
                            }
                            // Verifico che all'interno della directory esista
                            // il
                            // file PC_<GlobalHash>.zip
                            if (Arrays.asList(fileNames).contains(pcFileName)) {
                                // Estraggo il file di indice
                                try {
                                    String xml = new String(
                                            Util.estraiFileDaZip(zipPathPcFile, Constants.PC_INDEX_FILE),
                                            Constants.UTF_ENCODING);
                                    detRow.setBlXmlIndicePc(xml);
                                } catch (FileNotFoundException e) {
                                    throw new WebGenericException(messagesCtx.getNoPcIndexFile());
                                }
                            } else {
                                throw new WebGenericException(messagesCtx.getNoPcFile());
                            }
                        } catch (IOException e) {
                            throw new WebGenericException(
                                    messagesCtx.getGeneralError("Errore in fase di lettura del file zip"), e);
                        }
                    } else {
                        detRow.setDtAperturaSessioneRecup(null);
                        detRow.setTiStatoSessioneRecup(null);
                        // throw new
                        // WebGenericException(messagesCtx.getNoGlobalHashDir());
                    }
                }
            }
        } else if (risp != null && risp.getCdEsito().value().equals(EsitoServizio.KO.name())) {
            log.debug("Errore nella chiamata di ricerca diario : " + risp.getCdErr() + " : " + risp.getDsErr());
            throw new WebGenericException(messagesCtx.getServiceError(risp.getCdErr(), risp.getDsErr()));
        }
        return detRow;
    }

    /**
     * Esegue la chiamata a ricerca restituzioni studi utilizzando i filtri assegnati
     * 
     * IMPORTANTE: gli ultimi due parametri devono rimanere i due interi FROM e TO per far funzionare la paginazione
     * 
     * @param filtri
     *            i filtri assegnati
     * @param dateAperturaSessioni
     *            l'array di date apertura già validate
     * @param dateStudy
     *            l'array di date studi già validate
     * @param from
     *            l'indice di posizione per la paginazione
     * @param to
     *            il numero di record da visualizzare
     * 
     * @return il tablebean popolato
     * 
     * @throws JAXBException
     *             eccezione generica
     * @throws WebGenericException
     *             eccezione generica
     */
    public PigVRicRecupTableBean callRicercaRestituzioniStudiService(
            it.eng.dpi.slite.gen.form.MonitoraggioForm.RicercaRestituzioniStudi filtri, Date[] dateAperturaSessioni,
            Date[] dateStudy, int from, int to) throws JAXBException, WebGenericException {
        if (ricRestClient == null) {
            ricRestClient = init(ctx.getWsdlRicercaRestituzioniOggettiUrl(), RicercaRestituzioniOggetti.class,
                    "wsRicercaDiarioAdvisor");
        }
        if (ricRestClient == null) {
            log.debug("Ricerca Restituzioni - client non disponibile");
            throw new WebGenericException(messagesCtx.getServiceUnavailable(RicercaDiario.class.getSimpleName()));
        }
        log.debug("Ricerca Restituzioni - client inizializzato");

        // Chiamo il servizio di ricerca diario in base ai parametri forniti
        // Creo la lista dei datispecifici in output
        ListaDatiSpecificiOutType xmlout = new ListaDatiSpecificiOutType();
        // Inserisco all'interno della lista i dati necessari all'output,
        // definito per semplicità  in un enum delle costanti
        for (DatiSpecDicom datoSpec : Constants.DatiSpecDicom.getRecuperoOutputEnums()) {
            xmlout.getDatoSpecificoOut().add(datoSpec.toString());
        }

        // Creo la lista dei filtri di richiesta in base ai parametri forniti
        ListaFiltriType xmlfiltri = new ListaFiltriType();

        // Aggiungo ogni filtro alla richiesta se e solo se è popolato

        // AETNodoDicom
        if (StringUtils.isNotBlank(filtri.getNodo_dicom().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.AETNodoDicom,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getNodo_dicom().getValue()));
        }

        // StudyDate
        if (dateStudy[0] != null || dateStudy[1] != null) {
            DateFormat timeStampDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            String operatore;
            FiltroType filtro = null;
            if (dateStudy[0] != null && dateStudy[1] != null) {
                operatore = Constants.ValoriOperatore.COMPRESO_FRA.name();
                filtro = creaFiltroDueValori(Constants.StudyDate, operatore, timeStampDf.format(dateStudy[0]),
                        timeStampDf.format(dateStudy[1]));
            } else if (dateStudy[0] != null) {
                operatore = Constants.ValoriOperatore.MAGGIORE_UGUALE.name();
                filtro = creaFiltroUnValore(Constants.StudyDate, operatore, timeStampDf.format(dateStudy[0]));
            } else if (dateStudy[1] != null) {
                operatore = Constants.ValoriOperatore.MINORE_UGUALE.name();
                filtro = creaFiltroUnValore(Constants.StudyDate, operatore, timeStampDf.format(dateStudy[1]));
            }
            xmlfiltri.getFiltro().add(filtro);
        }

        // AccessionNumber
        if (StringUtils.isNotBlank(filtri.getAccession_number().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.AccessionNumber,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getAccession_number().getValue()));
        }

        // PatientName
        if (StringUtils.isNotBlank(filtri.getPaziente().getValue())) {
            if (StringUtils.isNotBlank(filtri.getFiltro_paziente().getValue())) {
                xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientName,
                        filtri.getFiltro_paziente().getValue(), filtri.getPaziente().getValue()));
            } else {
                throw new WebGenericException(messagesCtx.getRicercaDiarioFiltroPazUnavailable());
            }
        }

        // PatientSex
        if (StringUtils.isNotBlank(filtri.getSesso_paziente().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientSex, Constants.ValoriOperatore.UGUALE.name(),
                    filtri.getSesso_paziente().getValue()));
        }

        // PatientBirthDate
        if (StringUtils.isNotBlank(filtri.getDt_nascita_paziente().getValue())) {
            DateFormat dateDf = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat webDf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                xmlfiltri.getFiltro()
                        .add(creaFiltroUnValore(Constants.PatientBirthDate, Constants.ValoriOperatore.UGUALE.name(),
                                dateDf.format(webDf.parse(filtri.getDt_nascita_paziente().getValue()))));
            } catch (ParseException e) {
                throw new WebGenericException("Errore di lettura della data di nascita");
            }
        }

        // PatientId
        if (StringUtils.isNotBlank(filtri.getId_paziente().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientId, Constants.ValoriOperatore.UGUALE.name(),
                    filtri.getId_paziente().getValue()));
        }

        // PatientIdIssuer
        if (StringUtils.isNotBlank(filtri.getIssuer_id_paziente().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.PatientIdIssuer,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getIssuer_id_paziente().getValue()));
        }

        // ReferringPhysicianName
        if (StringUtils.isNotBlank(filtri.getMedico().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.ReferringPhysicianName,
                    Constants.ValoriOperatore.CONTIENE.name(), filtri.getMedico().getValue()));
        }

        // ModalityStudyList
        if (StringUtils.isNotBlank(filtri.getModality_study().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.ModalityInStudyList,
                    Constants.ValoriOperatore.CONTIENE.name(), filtri.getModality_study().getValue()));
        }

        // StudyInstanceUID
        if (StringUtils.isNotBlank(filtri.getStudy_uid().getValue())) {
            xmlfiltri.getFiltro().add(creaFiltroUnValore(Constants.StudyInstanceUID,
                    Constants.ValoriOperatore.UGUALE.name(), filtri.getStudy_uid().getValue()));
        }

        // Creo la lista di ordine dei dati specifici
        ListaDatiSpecificiOrderType xmlorder = new ListaDatiSpecificiOrderType();
        xmlorder.getDatoSpecificoOrder().add(creaDatoSpecificoOrder(Constants.PatientName, Constants.ASCENDING));
        xmlorder.getDatoSpecificoOrder().add(creaDatoSpecificoOrder(Constants.StudyDate, Constants.ASCENDING));

        // Eseguo il marshalling dei tre xml predisposti sopra
        StringWriter swXmlOut = new StringWriter();
        Marshaller m = jaxbSingleton.getContextListaDatiSpecificiOutType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaDatiSpecificiOutType> xmlOutJAXB = new it.eng.sacerasi.ws.xml.diarioout.ObjectFactory()
                .createListaDatiSpecificiOut(xmlout);
        m.marshal(xmlOutJAXB, swXmlOut);

        StringWriter swXmlFiltri = new StringWriter();
        m = jaxbSingleton.getContextListaFiltriType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaFiltriType> xmlFiltriJAXB = new it.eng.sacerasi.ws.xml.diariofiltri.ObjectFactory()
                .createListaFiltri(xmlfiltri);
        m.marshal(xmlFiltriJAXB, swXmlFiltri);

        StringWriter swXmlOrder = new StringWriter();
        m = jaxbSingleton.getContextListaDatiSpecificiOrderType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaDatiSpecificiOrderType> xmlOrderJAXB = new it.eng.sacerasi.ws.xml.diarioorder.ObjectFactory()
                .createListaDatiSpecificiOrder(xmlorder);
        m.marshal(xmlOrderJAXB, swXmlOrder);

        XMLGregorianCalendar xmlCalDa = null;
        XMLGregorianCalendar xmlCalA = null;

        DatatypeFactory factory = null;
        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
        }

        if (dateAperturaSessioni[0] != null) {
            GregorianCalendar calDa = new GregorianCalendar();
            calDa.setTime(dateAperturaSessioni[0]);
            xmlCalDa = factory.newXMLGregorianCalendar(calDa);
        }

        if (dateAperturaSessioni[1] != null) {
            GregorianCalendar calA = new GregorianCalendar();
            calA.setTime(dateAperturaSessioni[1]);
            xmlCalA = factory.newXMLGregorianCalendar(calA);
        }

        // Eseguo la chiamata a ricerca restituzioni oggetti con i parametri
        // definiti
        // nmAmbiente, nmVersatore, cdPassword, nmTipoObject, cdKeyObject,
        // tiStatoSessione, dtAperturaSessioneDa, dtAperturaSessioneA,
        // niRecordInizio, niRecordResultSet, xmlDatiSpecOutput,
        // xmlDatiSpecFiltri, xmlDatiSpecOrder
        RicercaRestituzioniOggettiRisposta risp = ricRestClient.ricercaRestituzioniOggetti(ctx.getNmAmbiente(),
                ctx.getNmVersatore(), DPIConstants.WS_NM_TIPO_OBJECT, null,
                StringUtils.isBlank(filtri.getTi_stato().getValue()) ? null : filtri.getTi_stato().getValue(), xmlCalDa,
                xmlCalA, from + 1, to, swXmlOut.toString(), swXmlFiltri.toString(), swXmlOrder.toString());

        // Istanzio un tableBean vuoto
        PigVRicRecupTableBean tb = new PigVRicRecupTableBean();
        // Se la chiamata ha avuto esito OK e la lista di oggetti non è vuota
        if (risp != null && risp.getCdEsito().value().equals(EsitoServizio.OK.name())
                && risp.getListaOggetti() != null) {
            // Eseguo l'unmarshall dei dati specifici contenuti in ogni oggetto
            // Creo il bean di supporto RicRecupObjToRowBean e popolo il
            // rowbean da aggiungere al tablebean istanziato
            ListaOggRicRestOggType listaOggetti = risp.getListaOggetti();
            Unmarshaller um = jaxbSingleton.getContextListaValoriDatiSpecificiType().createUnmarshaller();
            for (OggettoRicRestOggType ogg : listaOggetti.getOggetto()) {
                RicRecupObjToRowBean obj = new RicRecupObjToRowBean(ogg);
                JAXBElement<ListaValoriDatiSpecificiType> datoSpecJAXB = (JAXBElement<ListaValoriDatiSpecificiType>) um
                        .unmarshal(new StreamSource(new StringReader(ogg.getXmlDatiSpecResult())),
                                ListaValoriDatiSpecificiType.class);
                ListaValoriDatiSpecificiType datoSpec = datoSpecJAXB.getValue();
                try {
                    // Estraggo la classe di RicDiarioObjToRowBean, che
                    // dovrà  contenere i valori dei dati specifici
                    Class<?> c = Class.forName(RicRecupObjToRowBean.class.getName());
                    // Istanzio il metodo definito come set + nome del campo
                    // da popolare del bean, capitalizzando il nome del
                    // metodo
                    // ES: cdAETnododicom -> setCdAetNodoDicom
                    Map<String, Class<?>[]> map = new HashMap<String, Class<?>[]>();
                    for (Method method : c.getDeclaredMethods()) {
                        map.put(method.getName(), method.getParameterTypes());
                    }
                    for (ValoreDatoSpecificoType dato : datoSpec.getValoreDatoSpecifico()) {

                        String nomeCampo = Constants.DatiSpecDicom.getEnum(dato.getDatoSpecifico()).getMethod();

                        Method method;
                        String methodName = "set" + WordUtils.capitalize(nomeCampo, null);
                        Class<?>[] params = map.get(methodName);
                        method = c.getDeclaredMethod(methodName, params);
                        // Invoca il metodo ottenuto sulla tabella
                        // RicRecupObjToRowBean
                        // dell'oggetto per impostare il valore del dato
                        // specifico
                        Object value = null;
                        if (dato.getValore() != null) {
                            if (params[0].equals(String.class)) {
                                value = dato.getValore();
                            } else if (params[0].equals(Date.class)) {
                                DateFormat dateDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                value = dateDf.parse(dato.getValore());
                            } else if (params[0].equals(Timestamp.class)) {
                                DateFormat timeStampDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                value = timeStampDf.parse(dato.getValore());
                            } else if (params[0].equals(BigDecimal.class)) {
                                value = new BigDecimal(dato.getValore());
                            }
                        }
                        method.invoke(obj, value);

                    }
                } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException | ParseException e) {
                    log.error("Errore di popolamento del bean RicRecupObjToRowBean", e);
                }
                PigVRicRecupRowBean row = new PigVRicRecupRowBean();
                row.entityToRowBean(obj);
                tb.add(row);
            }
        } else if (risp != null && risp.getCdEsito().value().equals(EsitoServizio.KO.name())) {
            throw new WebGenericException(messagesCtx.getServiceError(risp.getCdErr(), risp.getDsErr()));
        }
        return tb;
    }
}
