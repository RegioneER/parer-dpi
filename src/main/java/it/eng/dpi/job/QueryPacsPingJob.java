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

package it.eng.dpi.job;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import it.eng.dpi.bean.CompareBean;
import it.eng.dpi.bean.DicomNode;
import it.eng.dpi.bean.QueryBean;
import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.business.impl.SacerPingObjectCreator;
import it.eng.dpi.component.JAXBSingleton;
import it.eng.dpi.dicom.scu.QRConstants.QueryRetrieveLevel;
import it.eng.dpi.exception.DPIException;
import it.eng.dpi.service.DPIConstants;
import it.eng.dpi.service.EchoService;
import it.eng.dpi.service.QueryMoveService;
import it.eng.sacerasi.ws.ListaOggRicDiarioType;
import it.eng.sacerasi.ws.OggettoRicDiarioType;
import it.eng.sacerasi.ws.RicercaDiario;
import it.eng.sacerasi.ws.RicercaDiarioRisposta;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType.FiltroUnValore;
import it.eng.sacerasi.ws.xml.diariofiltri.ListaFiltriType;
import it.eng.sacerasi.ws.xml.diarioout.ListaDatiSpecificiOutType;
import it.eng.sacerasi.ws.xml.diarioresult.ListaValoriDatiSpecificiType;
import it.eng.sacerasi.ws.xml.diarioresult.ValoreDatoSpecificoType;

@Component
public class QueryPacsPingJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("CONFRONTO_PACS_PREINGEST");
    private static final Logger log = LoggerFactory.getLogger(QueryPacsPingJob.class);

    @Autowired
    private QueryMoveService qrService;

    @Autowired
    private EchoService echoService;

    @Resource
    @Qualifier("dcmHashDicomTag")
    private List<String> dcmHashDicomTag;

    @Resource
    @Qualifier("dicomNodes")
    private List<DicomNode> dicomNodes;

    @Autowired
    private SacerPingObjectCreator sacerObjectCreator;

    private int[] dcmDecodedValues;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private RicercaDiario client;

    private Date startDate;

    @Autowired
    private JAXBSingleton jaxbSingleton;

    @PostConstruct
    public void postConstruct() {
        dcmDecodedValues = new int[dcmHashDicomTag.size()];
        for (int i = 0; i < dcmDecodedValues.length; i++) {
            dcmDecodedValues[i] = Integer.decode(dcmHashDicomTag.get(i));
        }
        try {
            startDate = new SimpleDateFormat("ddMMyyyy").parse(ctx.getQueryPacsStartDate());
        } catch (ParseException e) {
            log.error("Stringa non in formato ggmmaaaa :" + ctx.getQueryPacsStartDate());
            throw new RuntimeException(e);
        }
    }

    public synchronized void doDailyWork() {
        // if (ctx.isDailyEnable()) {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START + " | JOB GIORNALIERO");
            if (client == null) {
                client = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                doWork(Integer.parseInt(ctx.getDailyDelay()), Integer.parseInt(ctx.getDailyHourBefore()));
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }
        // }
    }

    public synchronized void doWeeklyWork() {
        // if (ctx.isWeeklyEnable()) {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START + " | JOB SETTIMANALE");
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
                client = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class);
            } else {
                doWork(Integer.parseInt(ctx.getWeeklyDelay()), Integer.parseInt(ctx.getWeeklyHourBefore()));
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }
        // }
    }

    public synchronized void doMonthlyWork() {
        // if (ctx.isMonthlyEnable()) {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START + " | JOB MENSILE");
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
                client = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class);
            } else {
                doWork(Integer.parseInt(ctx.getMonthlyDelay()), Integer.parseInt(ctx.getMonthlyHourBefore()));
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }
        // }
    }

    private void doWork(Integer filtroGiorniDelay, Integer filtroOreBefore)
            throws IOException, ConfigurationException, InterruptedException, NoSuchAlgorithmException {
        Calendar to = Calendar.getInstance();
        to.add(Calendar.DATE, (-1) * (filtroGiorniDelay));
        Date toDate = to.getTime();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, (-1) * ((filtroGiorniDelay) + (filtroOreBefore / 24)));
        Date fromDate = from.getTime();
        String fromString = null;
        String toString = null;
        if (fromDate.before(startDate)) {
            log.info("La data di avvio DA calcolata: " + fromDate
                    + " è antecedente alla data di avvio impostata nella configurazione: " + startDate);
            fromDate = startDate;
        }
        if (toDate.before(startDate)) {
            log.info("La data di avvio A calcolata: " + toDate
                    + " è antecedente alla data di avvio impostata nella configurazione: " + startDate);
            toDate = startDate;
        }
        fromString = dateFormat.format(fromDate).toString();
        toString = dateFormat.format(toDate).toString();

        log.debug("Ricerca temporale su pacs dal: " + fromDate + " al: " + toDate);

        log.debug("StudyDate query TAG: " + Tag.StudyDate + " VALUE: " + fromString + "-" + toString);

        for (DicomNode pacs : dicomNodes) {
            try {
                echoService.doCEcho(pacs.getHostname(), pacs.getPort(), pacs.getAet());
            } catch (IOException e) {
                log.error("Impossibile contattare il nodo DICOM", e);
                audit.error("PACS: " + pacs.getAet() + " | ERRORE NEL COMANDO ECHO: " + e.getMessage());
                continue;
            }
            QueryBean query = new QueryBean(QueryRetrieveLevel.STUDY_STUDY_ROOT_FIRST);
            query.addReturnKey(dcmDecodedValues);
            query.addMatchingKey(new int[] { Tag.StudyDate }, fromString + "-" + toString);
            List<DicomObject> studies = null;
            try {
                studies = qrService.doCFind(query, pacs.getHostname(), pacs.getPort(), pacs.getAet());
            } catch (Exception e) {
                audit.error("PACS: " + pacs.getAet() + " | RANGE DI DATE: " + fromString + "-" + toString
                        + " | ERRORE INTERROGANDO IL PACS");
                log.error("Errore interrogando il Pacs " + pacs.getAet(), e);
                continue;
            }
            audit.info("PACS: " + pacs.getAet() + " | RANGE DI DATE: " + fromString + "-" + toString
                    + " | NUMERO DI STUDI SUL PACS: " + studies.size());
            Map<String, DicomObject> pacsStudyToMoveHashMap = null;
            try {
                pacsStudyToMoveHashMap = processStudies(studies);
            } catch (DPIException e) {
                audit.error("RANGE DI DATE: " + fromString + "-" + toString
                        + " | ERRORE INTERROGANDO IL WS RICERCA DIARIO SUL SACER PREINGEST");
                continue;
            }
            audit.info("PACS: " + pacs.getAet() + " | NUMERO DI STUDI DA RECUPERARE (CMOVE): "
                    + pacsStudyToMoveHashMap.size());
            try {
                if (pacsStudyToMoveHashMap.size() > 0) {
                    qrService.doCMove(new ArrayList<DicomObject>(pacsStudyToMoveHashMap.values()), query,
                            pacs.getHostname(), pacs.getPort(), pacs.getAet());
                    audit.info("PACS: " + pacs.getAet() + " | IMMAGINI RECUPERATE: " + query.getCompleted()
                            + " | IMMAGINI NON RECUPERATI: " + query.getFailed());
                }
            } catch (Exception e) {
                audit.error("PACS: " + pacs.getAet() + " | RANGE DI DATE: " + fromString + "-" + toString
                        + " | ERRORE CMOVE SUL PACS");
                log.error("Errore cmove sul Pacs " + pacs.getAet(), e);
                continue;
            }
        }

    }

    private Map<String, DicomObject> processStudies(List<DicomObject> studies)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, DPIException {
        Map<String, DicomObject> pacsStudyToMoveHashMap = new HashMap<String, DicomObject>();
        Iterator<DicomObject> studyIterator = studies.iterator();
        while (studyIterator.hasNext()) {
            StringBuilder dcmHashString = new StringBuilder();
            Set<String> sacerDCMHashSet = new HashSet<String>();
            Map<String, DicomObject> pacsStudyHashMap = new HashMap<String, DicomObject>();
            String sep = "";
            for (int i = 0; i < 500 && studyIterator.hasNext(); i++) {
                dcmHashString.append(sep);
                if (i == 0) {
                    sep = ",";
                }
                DicomObject study = studyIterator.next();
                Map<String, DicomElement> orderedMap = new LinkedHashMap<String, DicomElement>();
                sacerObjectCreator.getDicomValues(dcmHashDicomTag, study, orderedMap);
                String dcmFile = sacerObjectCreator.generateDcmFile(orderedMap, 0);
                log.debug("DCM_FILE:\n" + dcmFile);
                String dcmHash = sacerObjectCreator.calculateHash(dcmFile);
                log.debug("DCM_HASH:\n" + dcmHash);
                dcmHashString.append(dcmHash);
                pacsStudyHashMap.put(dcmHash, study);
            }
            try {
                sacerDCMHashSet.addAll(getSacerDCMHash(dcmHashString.toString()));
            } catch (Exception e) {
                log.error("Errore interrogando il WS Ricerca Diario", e);
                throw new DPIException("Errore interrogando il WS Ricerca Diario in SACER PREINGEST");
            }
            pacsStudyToMoveHashMap
                    .putAll(Maps.filterKeys(pacsStudyHashMap, Predicates.not(Predicates.in(sacerDCMHashSet))));

        }
        return pacsStudyToMoveHashMap;
    }

    private Set<String> getSacerDCMHash(String dcmHashString) throws JAXBException {
        ListaDatiSpecificiOutType xmlout = new ListaDatiSpecificiOutType();
        xmlout.getDatoSpecificoOut().add("DCM-hash");

        ListaFiltriType xmlfiltri = new ListaFiltriType();
        FiltroType filtro = new FiltroType();
        FiltroUnValore filtroUnValore = new FiltroUnValore();
        filtroUnValore.setDatoSpecifico("DCM-hash");
        filtroUnValore.setOperatore("IN");
        filtroUnValore.setValore(dcmHashString);
        filtro.setFiltroUnValore(filtroUnValore);
        xmlfiltri.getFiltro().add(filtro);

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

        // nmAmbiente, nmVersatore, cdPassword, nmTipoObject, cdKeyObject,
        // idSessione, tiStatoObject, flTutteSessioni, niRecordInizio,
        // niRecordResultSet, xmlDatiSpecOutput, xmlDatiSpecFiltri,
        // xmlDatiSpecOrder)
        RicercaDiarioRisposta risp = client.ricercaDiario(ctx.getNmAmbiente(), ctx.getNmVersatore(),
                DPIConstants.WS_NM_TIPO_OBJECT, null, null, null, false, 1, 9999, swXmlOut.toString(),
                swXmlFiltri.toString(), null);
        Set<String> sacerPingDCMHashSet = new HashSet<>();
        Unmarshaller um = jaxbSingleton.getContextListaValoriDatiSpecificiType().createUnmarshaller();
        ListaOggRicDiarioType listaOggetti = risp.getListaOggetti();
        for (OggettoRicDiarioType ogg : listaOggetti.getOggetto()) {
            if (!ogg.getTiStatoSessione().startsWith("CHIUSO_ERR")) {
                JAXBElement<ListaValoriDatiSpecificiType> datoSpecJAXB = (JAXBElement<ListaValoriDatiSpecificiType>) um
                        .unmarshal(new StreamSource(new StringReader(ogg.getXmlDatiSpecResult())),
                                ListaValoriDatiSpecificiType.class);
                ListaValoriDatiSpecificiType datoSpec = datoSpecJAXB.getValue();
                ValoreDatoSpecificoType dato = datoSpec.getValoreDatoSpecifico().get(0);
                sacerPingDCMHashSet.add(dato.getValore());
            }
        }
        log.debug("I DCM-Hash presenti in pre-ingest sono " + sacerPingDCMHashSet.size() + ": "
                + sacerPingDCMHashSet.toString());
        return sacerPingDCMHashSet;
    }

    @Override
    public synchronized void doWork() {
        // THIS METHOD DO NOTHING
    }

    public List<CompareBean> comparePacsPing(Integer filtroGiorniDelay, Integer filtroOreBefore)
            throws ConfigurationException, InterruptedException, NoSuchAlgorithmException,
            UnsupportedEncodingException {

        if (client == null) {
            client = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class);
        }
        if (client == null) {
            throw new IllegalStateException("Errore di comunicazione con il WS di Pre-Ingest");
        }

        List<CompareBean> compareList = new ArrayList<CompareBean>();
        Calendar to = Calendar.getInstance();
        to.add(Calendar.DATE, (-1) * (filtroGiorniDelay));
        Date toDate = to.getTime();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, (-1) * ((filtroGiorniDelay) + (filtroOreBefore / 24)));
        Date fromDate = from.getTime();
        String fromString = null;
        String toString = null;
        if (fromDate.before(startDate)) {
            log.info("La data di avvio DA calcolata: " + fromDate
                    + " è antecedente alla data di avvio impostata nella configurazione: " + startDate);
            fromDate = startDate;
        }
        if (toDate.before(startDate)) {
            log.info("La data di avvio A calcolata: " + toDate
                    + " è antecedente alla data di avvio impostata nella configurazione: " + startDate);
            toDate = startDate;
        }
        fromString = dateFormat.format(fromDate).toString();
        toString = dateFormat.format(toDate).toString();

        log.debug("Ricerca temporale su pacs dal: " + fromDate + " al: " + toDate);

        log.debug("StudyDate query TAG: " + Tag.StudyDate + " VALUE: " + fromString + "-" + toString);

        for (DicomNode pacs : dicomNodes) {
            CompareBean cb = new CompareBean();
            cb.setPacsAet(pacs.getAet());
            try {
                echoService.doCEcho(pacs.getHostname(), pacs.getPort(), pacs.getAet());
            } catch (IOException e) {
                log.error("Impossibile contattare il nodo DICOM", e);
                log.error("PACS: " + pacs.getAet() + " | ERRORE NEL COMANDO ECHO: " + e.getMessage());
                continue;
            }
            QueryBean query = new QueryBean(QueryRetrieveLevel.STUDY_STUDY_ROOT_FIRST);
            query.addReturnKey(dcmDecodedValues);
            query.addMatchingKey(new int[] { Tag.StudyDate }, fromString + "-" + toString);
            List<DicomObject> studies = null;
            try {
                studies = qrService.doCFind(query, pacs.getHostname(), pacs.getPort(), pacs.getAet());
            } catch (Exception e) {
                log.error("PACS: " + pacs.getAet() + " | RANGE DI DATE: " + fromString + "-" + toString
                        + " | ERRORE INTERROGANDO IL PACS");
                log.error("Errore interrogando il Pacs " + pacs.getAet(), e);
                continue;
            }
            cb.setNumStudyPacs(studies.size());
            log.info("PACS: " + pacs.getAet() + " | RANGE DI DATE: " + fromString + "-" + toString
                    + " | NUMERO DI STUDI SUL PACS: " + studies.size());
            Map<String, DicomObject> pacsStudyToMoveHashMap = null;
            try {
                pacsStudyToMoveHashMap = processStudies(studies);
            } catch (DPIException e) {
                log.error("RANGE DI DATE: " + fromString + "-" + toString
                        + " | ERRORE INTERROGANDO IL WS RICERCA DIARIO SUL SACER PREINGEST");
                continue;
            }
            cb.setNumStudyDaTrasferire(pacsStudyToMoveHashMap.size());
            cb.setNumStudyPing(studies.size() - pacsStudyToMoveHashMap.size());
            log.info("PACS: " + pacs.getAet() + " | NUMERO DI STUDI DA RECUPERARE (CMOVE): "
                    + pacsStudyToMoveHashMap.size());
            compareList.add(cb);
        }
        return compareList;

    }

    public synchronized void doInstantly(Integer filtroGiorniDelay, Integer filtroOreBefore) {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START + " | JOB ONLINE");
            if (client == null) {
                client = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                doWork(filtroGiorniDelay, filtroOreBefore);
            }
            audit.info(DPIConstants.AUDIT_JOB_STOP);
        } catch (NoSuchAlgorithmException | IOException | ConfigurationException | InterruptedException e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }
    }

}
