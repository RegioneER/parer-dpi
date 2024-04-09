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

package it.eng.dpi.web.action;

import static org.quartz.TriggerBuilder.newTrigger;
import it.eng.dpi.bean.CStoreBean;
import it.eng.dpi.bean.CompareBean;
import it.eng.dpi.bean.DicomNode;
import it.eng.dpi.business.impl.ChiusWarnService;
import it.eng.dpi.business.impl.InvioOggettoService;
import it.eng.dpi.business.impl.PulNotifService;
import it.eng.dpi.business.impl.RicercaService;
import it.eng.dpi.business.impl.RichRestStudioService;
import it.eng.dpi.component.DPIContext;
import it.eng.dpi.component.Messages;
import it.eng.dpi.component.Util;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.WebGenericException;
import it.eng.dpi.job.QueryPacsPingJob;
import it.eng.dpi.service.DPIConstants;
import it.eng.dpi.service.EchoService;
import it.eng.dpi.service.SendService;
import it.eng.dpi.slite.gen.Application;
import it.eng.dpi.slite.gen.action.MonitoraggioAbstractAction;
import it.eng.dpi.slite.gen.form.MonitoraggioForm;
import it.eng.dpi.slite.gen.form.MonitoraggioForm.RicercaDiario;
import it.eng.dpi.slite.gen.form.MonitoraggioForm.RicercaRestituzioniStudi;
import it.eng.dpi.web.bean.ConfrontiPacsRowBean;
import it.eng.dpi.web.bean.ConfrontiPacsTableBean;
import it.eng.dpi.web.bean.PigVRicDiarioRowBean;
import it.eng.dpi.web.bean.PigVRicDiarioTableBean;
import it.eng.dpi.web.bean.PigVRicRecupTableBean;
import it.eng.dpi.web.util.Constants;
import it.eng.dpi.web.validator.TypeValidator;
import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.db.base.BaseRowInterface;
import it.eng.spagoLite.db.base.row.BaseRow;
import it.eng.spagoLite.db.base.table.BaseTable;
import it.eng.spagoLite.db.oracle.decode.DecodeMap;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

public class MonitoraggioAction extends MonitoraggioAbstractAction {
    private static final Logger log = LoggerFactory.getLogger(MonitoraggioAction.class);

    @Resource
    @Qualifier("pacsListForSearch")
    private List<String> pacsListForSearch;

    @Resource
    @Qualifier("transferPacsList")
    private List<String> transferPacsList;

    @Resource
    @Qualifier("transferDicomNodes")
    private List<DicomNode> transferDicomNodes;

    @Resource
    @Qualifier("dlMotivoWarningStandards")
    private List<String> dlMotivoWarningStandards;

    @Resource
    @Qualifier("storageSCUDir")
    private File storageSCUDir;

    @Autowired
    private RicercaService ricercaService;
    @Autowired
    private RichRestStudioService richRestStudioservice;
    @Autowired
    private ChiusWarnService chiusWarnService;
    @Autowired
    private InvioOggettoService pingSender;
    @Autowired
    private EchoService echoService;
    @Autowired
    private SendService pacsSender;
    @Autowired
    private PulNotifService puliziaNotifService;
    @Autowired
    private QueryPacsPingJob queryPacsPingJob;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private Messages messagesCtx;
    @Autowired
    private DPIContext ctx;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SchedulerFactoryBean scheduler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Date startDate;

    @PostConstruct
    public void postConstruct() {
        try {
            startDate = new SimpleDateFormat("ddMMyyyy").parse(ctx.getQueryPacsStartDate());
        } catch (ParseException e) {
            log.error("Stringa non in formato ggmmaaaa :" + ctx.getQueryPacsStartDate());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initOnClick() throws EMFError {
    }

    /**
     * Carica la pagina di ricerca diario inizializzando i campi
     * 
     * @throws EMFError
     *             eccezione generica
     */
    // @Secure(action = "Menu.Monitoraggio.RicercaDiario")
    public void loadRicercaDiario() throws EMFError {
        getUser().getMenu().reset();
        getUser().getMenu().select("Menu.Monitoraggio.RicercaDiario");

        getForm().getRicercaDiario().clear();
        // Inizializzo il campo ricercaOgniSessione con la decodeMap
        // indicando il valore di default
        getForm().getRicercaDiario().getRicerca_ogni_sessione().setDecodeMap(getIndicatoreDecodeMap());
        getForm().getRicercaDiario().getRicerca_ogni_sessione().setValue("0");
        // Inizializzo il campo ti_stato con la decodeMap degli stati
        getForm().getRicercaDiario().getTi_stato().setDecodeMap(getStatiStudioDecodeMap());
        // Inizializzo il campo nodiDicom con la decodeMap dei nodi dicom
        // indicando il valore di default nel caso esista solo un nodo dicom
        getForm().getRicercaDiario().getNodo_dicom().setDecodeMap(getDicomNodesForResearchDecodeMap());
        if (pacsListForSearch.size() == 1) {
            getForm().getRicercaDiario().getNodo_dicom().setValue(pacsListForSearch.get(0));
        }
        // Inizializzo il campo filtro paziente indicando il valore di
        // default
        getForm().getRicercaDiario().getFiltro_paziente().setDecodeMap(getFiltroPazienteDecodeMap());
        getForm().getRicercaDiario().getFiltro_paziente().setValue(Constants.ValoriOperatore.INIZIA_PER.name());
        // Inizializzo il campo sesso paziente
        getForm().getRicercaDiario().getSesso_paziente().setDecodeMap(getSexDecodeMap());
        // Inizializzo il campo modality in study
        getForm().getRicercaDiario().getModality_study().setDecodeMap(getModalityInStudyDecodeMap());
        // // Inizializzo il campo di numero record visualizzati
        // getForm().getRicercaDiario().getPage_size().setDecodeMap(getPageSizeDecodeMap());
        // getForm().getRicercaDiario().getPage_size()
        // .setValue(String.valueOf(Constants.NumPages.NUM_RECORDS_10.getValue()));
        // Imposto i filtri in edit mode
        getForm().getRicercaDiario().setEditMode();
        getForm().getRicercaDiario().getRicercaDiario().setEditMode();
        getForm().getStudiList().setTable(new PigVRicDiarioTableBean());

        getForm().getListaGestioneWarning().setTable(new BaseTable());
        getForm().getStudiList().getSelect_warnings().setHidden(true);
        getForm().getRicercaDiario().getChiusuraMultipleWarnings().setHidden(true);
        getForm().getRicercaDiario().getVersamentoMultipleWarnings().setHidden(true);

        // Carico la pagina di ricerca
        forwardToPublisher(Application.Publisher.RICERCA_DIARIO);
    }

    @Override
    public void ricercaDiario() throws EMFError {
        // Eseguo la ricerca del diario: valido i valori e creo l'xml di
        // richiesta
        if (getLastPublisher().equals(Application.Publisher.RICERCA_DIARIO)) {
            MonitoraggioForm.RicercaDiario filtri = getForm().getRicercaDiario();
            // Esegue la post dei filtri compilati
            filtri.post(getRequest());
            // Valida i filtri per verificare quelli obbligatori
            if (filtri.validate(getMessageBox())) {
                caricaListaRicercaDiario(filtri);
            }
            String stato = filtri.getTi_stato().parse();

            if (StringUtils.isNotBlank(stato) && stato.equals(Constants.StatoStudio.WARNING.name())) {
                getForm().getStudiList().getSelect_warnings().setHidden(false);
                getForm().getRicercaDiario().getChiusuraMultipleWarnings().setHidden(false);
                getForm().getRicercaDiario().getVersamentoMultipleWarnings().setHidden(false);
            } else {
                getForm().getStudiList().getSelect_warnings().setHidden(true);
                getForm().getRicercaDiario().getChiusuraMultipleWarnings().setHidden(true);
                getForm().getRicercaDiario().getVersamentoMultipleWarnings().setHidden(true);
            }

            forwardToPublisher(getLastPublisher());
        }
    }

    public void caricaListaRicercaDiario(RicercaDiario filtri) throws EMFError {
        // Valida i campi di ricerca
        TypeValidator validator = new TypeValidator(getMessageBox());
        // Valido i filtri data acquisizione da - a restituendo le date
        // comprensive di orario
        Date[] dateStudi = validator.validaDate(filtri.getDt_study_da().parse(), filtri.getOre_dt_study_da().parse(),
                filtri.getMinuti_dt_study_da().parse(), filtri.getDt_study_a().parse(),
                filtri.getOre_dt_study_a().parse(), filtri.getMinuti_dt_study_a().parse(),
                filtri.getDt_study_da().getHtmlDescription(), filtri.getDt_study_a().getHtmlDescription());

        Date[] datePresaInCarico = validator.validaDate(filtri.getDt_presa_in_carico_da().parse(),
                filtri.getOre_dt_presa_in_carico_da().parse(), filtri.getMinuti_dt_presa_in_carico_da().parse(),
                filtri.getDt_presa_in_carico_a().parse(), filtri.getOre_dt_presa_in_carico_a().parse(),
                filtri.getMinuti_dt_presa_in_carico_a().parse(), filtri.getDt_presa_in_carico_da().getHtmlDescription(),
                filtri.getDt_presa_in_carico_a().getHtmlDescription());

        PigVRicDiarioTableBean tableBean = new PigVRicDiarioTableBean();
        if (!getMessageBox().hasError()) {
            // Non ho ricevuto errori nella validazione delle date,
            // perciò genero l'xml di richiesta
            try {
                tableBean = ricercaService.callRicercaDiarioService(filtri, dateStudi, datePresaInCarico, 0,
                        Integer.parseInt(ctx.getWebNumRecords()));
            } catch (JAXBException e) {
                log.error("Errore di marshalling della richiesta", e);
                getMessageBox()
                        .addError(messagesCtx.getGeneralError("Errore di marshalling della richiesta", e.getMessage()));
            } catch (WebGenericException e) {
                log.error("Errore dei filtri di ricerca diario : " + e.getMessage());
                getMessageBox().addError(e.getMessage());
            }
        }
        getForm().getStudiList().setTable(tableBean);
        // BigDecimal pageSize = filtri.getPage_size().parse();
        getForm().getStudiList().getTable().setPageSize(Constants.NumPages.NUM_RECORDS_10.getValue());
        getForm().getStudiList().getTable().first();
    }

    @Override
    public void dettaglioOnClick() throws EMFError {
        // Inizializzo anche i campi di dettaglio
        getForm().getDetailButtonList().setEditMode();
        forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
    }

    @Override
    public void elencoOnClick() throws EMFError {
        goBack();
    }

    @Override
    public void insertDettaglio() throws EMFError {
    }

    @Override
    public void loadDettaglio() throws EMFError {
        String lista = getTableName();
        String navigationEvent = getNavigationEvent();
        if (lista != null) {
            BaseRowInterface currentRow = lista.equals(getForm().getStudiList().getName())
                    ? getForm().getStudiList().getTable().getCurrentRow()
                    : getForm().getRestituzioniStudiList().getTable().getCurrentRow();
            boolean refresh;
            if (navigationEvent != null) {
                refresh = false;
            } else {
                refresh = getLastPublisher().equals(Application.Publisher.DETTAGLIO_STUDIO) ? true : false;
            }

            /*
             * Da analisi: - FlTutteSessioni = Si se attivato da "Ricerca diario"; = No se attivato da
             * "Ricerca restituzioni studi"; = No se attivato di seguito ad uno dei punti successivi di “Dettaglio
             * studio” Determino perciò il valore del campo flTutteSessioni con questa logica:
             * 
             * NB. refresh : Indica se è un refresh del dettaglio (ad esempio in seguito alla pressione di un bottone
             * del dettaglio)
             * 
             * Se la riga corrente è un'istanza della lista di ricercaDiario e refresh è true = false Se la riga
             * corrente è un'istanza della lista di ricercaDiario e refresh è false = true Se la riga corrente NON è
             * un'istanza della lista di ricercaDiario e refresh è true = false Se la riga corrente NON è un'istanza
             * della lista di ricercaDiario e refresh è false = false
             */
            boolean fromRicDiario = (currentRow instanceof PigVRicDiarioRowBean) && !refresh;

            PigVRicDiarioRowBean row = new PigVRicDiarioRowBean();
            try {
                row = ricercaService.callDettaglioRicercaDiarioService(currentRow, fromRicDiario);
            } catch (JAXBException e) {
                log.error("Errore di marshalling della richiesta", e);
                getMessageBox()
                        .addError(messagesCtx.getGeneralError("Errore di marshalling della richiesta", e.getMessage()));
            } catch (WebGenericException e) {
                log.error("Errore dei filtri di ricerca diario", e);
                getMessageBox().addError(e.getMessage());
            }
            getForm().getRicercaDiarioDettaglio().copyFromBean(row);
            // In base ai dati del dettaglio mostro le sezioni popolate e i
            // bottoni utilizzabili
            if (StringUtils.isNotBlank(row.getBlXmlIndiceUd()) || StringUtils.isNotBlank(row.getBlXmlIndicePc())) {
                getForm().getXMLPCSection().setHidden(false);
                getForm().getXMLUDSection().setHidden(false);
            } else {
                getForm().getXMLPCSection().setHidden(true);
                getForm().getXMLUDSection().setHidden(true);
            }
            // Disabilito i bottoni non utilizzabili in base allo stato
            // dello studio
            // DEFAULT
            getForm().getDetailButtonList().getChiusuraWarning().setHidden(true);
            getForm().getDetailButtonList().getDownloadProveConservazione().setHidden(true);
            getForm().getDetailButtonList().getEliminaRecuperoStudio().setHidden(true);
            getForm().getDetailButtonList().getRestituzioneStudio().setHidden(true);
            getForm().getDetailButtonList().getTrasferimentoStudio().setHidden(true);
            getForm().getDetailButtonList().getVersamentoWarning().setHidden(true);
            getForm().getRicercaDiarioDettaglio().getTi_patient_sex().setDecodeMap(getSexDecodeMap());
            getForm().getRicercaDiarioDettaglio().setViewMode();

            boolean mostraBottoni;

            if (currentRow instanceof PigVRicDiarioRowBean) {
                if (getForm().getRicercaDiario().getRicerca_ogni_sessione().getValue().equals(Constants.DB_TRUE)) {
                    mostraBottoni = false;
                } else {
                    mostraBottoni = true;
                }
            } else {
                mostraBottoni = true;
            }

            if (mostraBottoni) {
                // CASISTICHE
                // STATO SESSIONE RECUPERO
                if (row.getTiStatoSessioneRecup() != null
                        && row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_OK.name())) {
                    getForm().getDetailButtonList().getDownloadProveConservazione().setHidden(false);
                    getForm().getDetailButtonList().getEliminaRecuperoStudio().setHidden(false);
                } else if ((row.getTiStatoSessioneRecup() == null
                        || row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_ERR.name())
                        || row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_ERR_NOTIFICATO.name())
                        || row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_ERR_RECUPERATO.name())
                        || row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_ERR_PRELEVATO.name())
                        || row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.CHIUSO_ERR_ELIMINATO.name())
                        || row.getTiStatoSessioneRecup().equals(Constants.StatoStudio.ELIMINATO.name()))
                        && row.getTiStatoObject() != null
                        && row.getTiStatoObject().equals(Constants.StatoStudio.CHIUSO_OK.name())) {
                    getForm().getDetailButtonList().getRestituzioneStudio().setHidden(false);
                }
                // STATO SESSIONE
                if (row.getTiStatoObject() != null
                        && row.getTiStatoObject().equals(Constants.StatoStudio.WARNING.name())) {
                    getForm().getDetailButtonList().getChiusuraWarning().setHidden(false);
                    getForm().getDetailButtonList().getVersamentoWarning().setHidden(false);
                }
                // FL TRASFER DICOM
                if (row.getFlTransferDicom() != null && row.getFlTransferDicom().equals(Constants.DB_TRUE)) {
                    getForm().getDetailButtonList().getTrasferimentoStudio().setHidden(false);
                }
            }
        }
    }

    @Override
    public void saveDettaglio() throws EMFError {

    }

    @Override
    public void undoDettaglio() throws EMFError {

    }

    @Override
    protected String getDefaultPublsherName() {
        return Application.Publisher.RICERCA_DIARIO;
    }

    @Override
    public void reloadAfterGoBack(String publisherName) {
        if (publisherName.equals(Application.Publisher.RICERCA_DIARIO)) {
            try {
                int paginaCorrente = getForm().getStudiList().getTable().getCurrentPageIndex();
                int primoRecordPagina = getForm().getStudiList().getTable().getFirstRowPageIndex();

                caricaListaRicercaDiario(getForm().getRicercaDiario());

                // Rieseguo la query se necessario
                this.lazyLoadGoPage(getForm().getStudiList(), paginaCorrente);
                // Ritorno alla pagina
                getForm().getStudiList().getTable().setCurrentRowIndex(primoRecordPagina);
            } catch (EMFError e) {
                getMessageBox().addError("Impossibile caricare la lista");
            }
        } else if (publisherName.equals(Application.Publisher.RICERCA_RESTITUZIONI_STUDI)) {
            try {
                int paginaCorrente = getForm().getRestituzioniStudiList().getTable().getCurrentPageIndex();
                int primoRecordPagina = getForm().getRestituzioniStudiList().getTable().getFirstRowPageIndex();

                caricaListaRicercaRestituzioniStudi(getForm().getRicercaRestituzioniStudi());

                // Rieseguo la query se necessario
                this.lazyLoadGoPage(getForm().getRestituzioniStudiList(), paginaCorrente);
                // Ritorno alla pagina
                getForm().getRestituzioniStudiList().getTable().setCurrentRowIndex(primoRecordPagina);
            } catch (EMFError e) {
                getMessageBox().addError("Impossibile caricare la lista");
            }
        }
    }

    @Override
    public String getControllerName() {
        return Application.Actions.MONITORAGGIO;
    }

    private BaseRow createKeyValueBaseRow(String key, String value) {
        BaseRow br = new BaseRow();
        br.setString(key, value);
        return br;
    }

    private DecodeMap getIndicatoreDecodeMap() {
        BaseTable bt = new BaseTable();
        BaseRow br = new BaseRow();
        BaseRow br1 = new BaseRow();
        // Imposto i valori della combo INDICATORE
        DecodeMap mappaIndicatore = new DecodeMap();
        br.setString("flag", "SI");
        br.setString("valore", Constants.DB_TRUE);
        bt.add(br);
        br1.setString("flag", "NO");
        br1.setString("valore", Constants.DB_FALSE);
        bt.add(br1);
        mappaIndicatore.populatedMap(bt, "valore", "flag");
        return mappaIndicatore;
    }

    private DecodeMap getStatiStudioDecodeMap() {
        // Inizializzo le combo di supporto
        BaseTable bt = new BaseTable();

        // Imposto i valori della combo STATO ordinati
        DecodeMap mappaStato = new DecodeMap();
        String key = "stato";

        for (Constants.StatoStudio stato : Util.sortEnum(Constants.StatoStudio.getRicercaDiarioEnums())) {
            bt.add(createKeyValueBaseRow(key, stato.name()));
        }
        mappaStato.populatedMap(bt, key, key);
        return mappaStato;
    }

    private DecodeMap getStatiRecuperoStudioDecodeMap() {
        // Inizializzo le combo di supporto
        BaseTable bt = new BaseTable();

        // Imposto i valori della combo STATO ordinati
        DecodeMap mappaStato = new DecodeMap();
        String key = "stato";

        for (Constants.StatoStudio stato : Util.sortEnum(Constants.StatoStudio.getRicercaRestituzioniEnums())) {
            bt.add(createKeyValueBaseRow(key, stato.name()));
        }
        mappaStato.populatedMap(bt, key, key);
        return mappaStato;
    }

    private DecodeMap getDicomNodesForResearchDecodeMap() {
        BaseTable bt = new BaseTable();
        // Imposto i valori della combo nodi dicom ordinati
        DecodeMap mappaNodi = new DecodeMap();
        String key = "nodo";
        Collections.sort(pacsListForSearch);
        for (String nodo : pacsListForSearch) {
            bt.add(createKeyValueBaseRow(key, nodo));
        }
        mappaNodi.populatedMap(bt, key, key);
        return mappaNodi;
    }

    private DecodeMap getTransferDicomNodesDecodeMap() {
        BaseTable bt = new BaseTable();
        // Imposto i valori della combo nodi dicom ordinati
        DecodeMap mappaNodi = new DecodeMap();
        String key = "nodoTrasf";
        Collections.sort(transferPacsList);
        for (String nodo : transferPacsList) {
            bt.add(createKeyValueBaseRow(key, nodo));
        }
        mappaNodi.populatedMap(bt, key, key);
        return mappaNodi;
    }

    // private DecodeMap getPageSizeDecodeMap() {
    // BaseTable bt = new BaseTable();
    // DecodeMap mappaNumRecords = new DecodeMap();
    // String key = "numRecords";
    // String value = "value";
    // for (Constants.NumPages numPage : Constants.NumPages.values()) {
    // BaseRow br = new BaseRow();
    // br.setBigDecimal(key, new BigDecimal(numPage.getValue()));
    // br.setString(value, String.valueOf(numPage.getValue()));
    // bt.add(br);
    // }
    // mappaNumRecords.populatedMap(bt, key, value);
    // return mappaNumRecords;
    // }

    private DecodeMap getFiltroPazienteDecodeMap() {
        BaseTable bt = new BaseTable();
        // Imposto i valori della combo filtro paziente ordinati
        DecodeMap mappaFiltri = new DecodeMap();
        String key = "filtroPaziente";
        Collection<Constants.ValoriOperatore> ops = Util.sortEnum(Constants.ValoriOperatore
                .getEnums(Constants.ValoriOperatore.CONTIENE, Constants.ValoriOperatore.INIZIA_PER));
        for (Constants.ValoriOperatore op : ops) {
            bt.add(createKeyValueBaseRow(key, op.name()));
        }
        mappaFiltri.populatedMap(bt, key, key);
        return mappaFiltri;
    }

    private DecodeMap getSexDecodeMap() {
        BaseTable bt = new BaseTable();
        // Imposto i valori della combo sesso paziente
        DecodeMap mappaSexPatient = new DecodeMap();
        String key = "sessoPaziente";
        for (Constants.Sex sesso : Constants.Sex.values()) {
            bt.add(createKeyValueBaseRow(key, sesso.name()));
        }
        mappaSexPatient.populatedMap(bt, key, key);
        return mappaSexPatient;
    }

    private DecodeMap getMotivoWarningStandardsDecodeMap() {
        BaseTable bt = new BaseTable();
        // Imposto i valori della combo motivazioni standard versamento warning
        DecodeMap mappaMotivazioni = new DecodeMap();
        String key = "motivoWarning";
        for (String motivo : dlMotivoWarningStandards) {
            bt.add(createKeyValueBaseRow(key, motivo));
        }
        mappaMotivazioni.populatedMap(bt, key, key);
        return mappaMotivazioni;
    }

    private DecodeMap getModalityInStudyDecodeMap() {
        BaseTable bt = new BaseTable();
        // Imposto i valori della combo motivazioni standard versamento warning
        DecodeMap mappaModalities = new DecodeMap();
        String keyCode = "modality";
        String keyDesc = "modalityDesc";
        for (Constants.Modalities modality : Util.sortEnum(Constants.Modalities.values())) {
            BaseRow row = createKeyValueBaseRow(keyCode, modality.name());
            row.setString(keyDesc, modality.name() + " = " + modality.getDesc());
            bt.add(row);
        }
        mappaModalities.populatedMap(bt, keyCode, keyDesc);
        return mappaModalities;
    }

    @Override
    public void chiusuraWarning() throws EMFError {
        loadChiusuraWarning();
    }

    @Override
    public void chiusuraMultipleWarnings() throws EMFError {
        // Ottengo la lista di globalHash selezionati
        String[] selezionati = getRequest().getParameterValues(getForm().getStudiList().getSelect_warnings().getName());
        int totSelezionati = selezionati != null ? selezionati.length : 0;

        if (totSelezionati > 0) {
            PigVRicDiarioTableBean tb = new PigVRicDiarioTableBean();
            // getForm().getStudiList().post(getRequest());
            if (selezionati != null) {

                for (String comp : selezionati) {
                    if (StringUtils.isNotBlank(comp) && isNumeric(comp)) {
                        PigVRicDiarioRowBean rowBean = (PigVRicDiarioRowBean) getForm().getStudiList().getTable()
                                .getRow(Integer.parseInt(comp));
                        tb.add(rowBean);
                    }
                }
                getForm().getListaGestioneWarning().setTable(tb);
                getForm().getListaGestioneWarning().getTable()
                        .setPageSize(Constants.NumPages.NUM_RECORDS_100.getValue());
                getForm().getListaGestioneWarning().getTable().first();
            }

            loadChiusuraWarning();
        } else {
            getMessageBox().addError("Seleziona almeno uno studio in warning");
        }
    }

    private void loadChiusuraWarning() {
        log.debug("Carico pagina chiusura warning");
        if (getLastPublisher().equals(Application.Publisher.DETTAGLIO_STUDIO)) {
            log.debug("DA DETTAGLIO - Singolo studio");
            getForm().getStudySummarySection().setHidden(false);
            getForm().getListaGestioneWarning().setHidden(true);
        } else {
            log.debug("DA RICERCA DIARIO - Studi multipli");
            getForm().getStudySummarySection().setHidden(true);
            getForm().getListaGestioneWarning().setHidden(false);
        }
        getForm().getGestioneWarning().clear();
        getForm().getGestioneWarning().getDl_motivo_chiuso_warning_standard()
                .setDecodeMap(getMotivoWarningStandardsDecodeMap());
        getForm().getGestioneWarning().setEditMode();
        forwardToPublisher(Application.Publisher.CHIUSURA_WARNING);
    }

    private void callWSChiusuraWarning(String globalHash, String motivazione) throws WebGenericException {
        chiusWarnService.callWS(globalHash, motivazione);
    }

    @Override
    public void versamentoMultipleWarnings() throws EMFError {
        // Ottengo la lista di globalHash selezionati
        String[] selezionati = getRequest().getParameterValues(getForm().getStudiList().getSelect_warnings().getName());
        int totSelezionati = selezionati != null ? selezionati.length : 0;

        if (totSelezionati > 0) {
            PigVRicDiarioTableBean tb = new PigVRicDiarioTableBean();
            // getForm().getStudiList().post(getRequest());
            if (selezionati != null) {
                for (String comp : selezionati) {
                    if (StringUtils.isNotBlank(comp) && isNumeric(comp)) {
                        PigVRicDiarioRowBean rowBean = (PigVRicDiarioRowBean) getForm().getStudiList().getTable()
                                .getRow(Integer.parseInt(comp));
                        tb.add(rowBean);
                    }
                }
                getForm().getListaGestioneWarning().setTable(tb);
                getForm().getListaGestioneWarning().getTable()
                        .setPageSize(Constants.NumPages.NUM_RECORDS_100.getValue());
                getForm().getListaGestioneWarning().getTable().first();
            }

            loadVersamentoWarning();
        } else {
            getMessageBox().addError("Seleziona almeno uno studio in warning");
        }
    }

    /**
     * Eseguo la chiamata per il versamento in pre-ingest dello studio in warning. Verifico la presenza della
     * motivazione e eseguo l'invio.
     * 
     * @throws EMFError
     *             eccezione generica
     */
    @Override
    public void versamentoWarning() throws EMFError {
        loadVersamentoWarning();
    }

    private void loadVersamentoWarning() {
        log.debug("Carico pagina versamento warning");
        if (getLastPublisher().equals(Application.Publisher.DETTAGLIO_STUDIO)) {
            log.debug("DA DETTAGLIO - Singolo studio");
            getForm().getStudySummarySection().setHidden(false);
            getForm().getListaGestioneWarning().setHidden(true);
        } else {
            log.debug("DA RICERCA DIARIO - Studi multipli");
            getForm().getStudySummarySection().setHidden(true);
            getForm().getListaGestioneWarning().setHidden(false);
        }
        getForm().getGestioneWarning().clear();
        getForm().getGestioneWarning().setEditMode();
        forwardToPublisher(Application.Publisher.FORZA_ACCETTAZIONE_WARNING);
    }

    private void callWSVersamentoWarning(String globalHash, String motivazione) throws WebGenericException {
        try {
            pingSender.setMotivazione(motivazione);
            pingSender.sendWarnStudy(globalHash);
        } catch (IOException e) {
            getMessageBox().addError(messagesCtx.getFatalError());
        }
    }

    @Override
    public void confermaChiusuraWarning() throws EMFError {
        if (!getForm().getStudySummarySection().isHidden()) {
            getForm().getGestioneWarning().getDl_motivo_chiuso_warning().post(getRequest());
            String motivazione = getForm().getGestioneWarning().getDl_motivo_chiuso_warning().getValue();
            String globalHash = getForm().getRicercaDiarioDettaglio().getDs_global_hash().getValue();
            // Verifico la presenza della motivazione
            // Altrimenti ritorno errore
            if (StringUtils.isNotBlank(motivazione)) {
                log.debug("Eseguo la chiamata di chiusura warning per il global hash " + globalHash + " e motivazione "
                        + motivazione);
                try {
                    callWSChiusuraWarning(globalHash, motivazione);
                } catch (WebGenericException e) {
                    String descError = e.getCode() != null
                            ? messagesCtx.getChiusWarnError(e.getCode(), e.getDescription(), 0, 1) : e.getMessage();
                    log.debug(descError);
                    getMessageBox().addError(descError);
                }
            } else {
                log.debug("Motivazione chiusura assente");
                getMessageBox().addError(messagesCtx.getNoMotivChiusWarning());
            }
            if (!getMessageBox().hasError()) {
                log.debug("Chiusura in warning eseguito con successo");
                getMessageBox().addInfo(messagesCtx.getChiusWarnOk());
                setTableName(getRequest().getParameter("table"));
                loadDettaglio();
                goBack();
            } else {
                setTableName(getRequest().getParameter("table"));
                forwardToPublisher(Application.Publisher.CHIUSURA_WARNING);
            }
        } else {
            // RICERCA DIARIO
            PigVRicDiarioTableBean table = (PigVRicDiarioTableBean) getForm().getListaGestioneWarning().getTable();
            getForm().getGestioneWarning().getDl_motivo_chiuso_warning().post(getRequest());
            String motivazione = getForm().getGestioneWarning().getDl_motivo_chiuso_warning().getValue();
            // Verifico la presenza della motivazione
            // Altrimenti ritorno errore
            if (StringUtils.isNotBlank(motivazione)) {
                int currentRow = 0;
                log.debug("Eseguo la chiamata di chiusura warning per " + table.size() + " studi");
                for (PigVRicDiarioRowBean row : table) {
                    String globalHash = row.getString("cd_key_object");
                    try {
                        callWSChiusuraWarning(globalHash, motivazione);
                        currentRow++;
                    } catch (WebGenericException e) {
                        String descError = e.getCode() != null ? messagesCtx.getChiusWarnError(e.getCode(),
                                e.getDescription(), currentRow, table.size()) : e.getMessage();
                        log.debug(descError);
                        getMessageBox().addError(descError);
                    }
                    if (getMessageBox().hasError()) {
                        break;
                    }
                }
            } else {
                log.debug("Motivazione chiusura assente");
                getMessageBox().addError(messagesCtx.getNoMotivChiusWarning());
            }
            if (!getMessageBox().hasError()) {
                log.debug("Chiusure in warning eseguite con successo");
                getMessageBox().addInfo(messagesCtx.getChiusWarnMultipleOk(table.size(), table.size()));
                // setTableName(getForm().getStudiList().getName());
                // loadDettaglio();
                goBack();
            } else {
                forwardToPublisher(Application.Publisher.CHIUSURA_WARNING);
            }
        }
    }

    @Override
    public void annullaChiusuraWarning() throws EMFError {
        goBack();
    }

    @Override
    public void confermaVersamentoWarning() throws EMFError {
        if (!getForm().getStudySummarySection().isHidden()) {
            getForm().getGestioneWarning().getDl_motivo_forza_accettazione().post(getRequest());
            String motivazione = getForm().getGestioneWarning().getDl_motivo_forza_accettazione().getValue();
            String globalHash = getForm().getRicercaDiarioDettaglio().getDs_global_hash().getValue();
            // Verifico la presenza della motivazione
            // Altrimenti ritorno errore
            if (StringUtils.isNotBlank(motivazione)) {
                log.debug("Eseguo la chiamata di versamento per il global hash " + globalHash + " e motivazione "
                        + motivazione);
                try {
                    callWSVersamentoWarning(globalHash, motivazione);
                } catch (WebGenericException e) {
                    String descError = e.getCode() != null
                            ? messagesCtx.getVersWarnError(e.getCode(), e.getDescription(), 0, 1) : e.getMessage();
                    log.debug(descError);
                    getMessageBox().addError(descError);
                }
            } else {
                log.debug("Motivazione versamento assente");
                getMessageBox().addError(messagesCtx.getNoMotivVersWarning());
            }
            if (!getMessageBox().hasError()) {
                log.debug("Versamento in warning eseguito con successo");
                getMessageBox().addInfo(messagesCtx.getVersWarnOk());
                setTableName(getRequest().getParameter("table"));
                loadDettaglio();
                goBack();
            } else {
                setTableName(getRequest().getParameter("table"));
                forwardToPublisher(Application.Publisher.FORZA_ACCETTAZIONE_WARNING);
            }
        } else {
            // RICERCA DIARIO
            PigVRicDiarioTableBean table = (PigVRicDiarioTableBean) getForm().getListaGestioneWarning().getTable();
            getForm().getGestioneWarning().getDl_motivo_forza_accettazione().post(getRequest());
            String motivazione = getForm().getGestioneWarning().getDl_motivo_forza_accettazione().getValue();
            // Verifico la presenza della motivazione
            // Altrimenti ritorno errore
            if (StringUtils.isNotBlank(motivazione)) {
                int currentRow = 0;
                log.debug("Eseguo la chiamata di versamento per " + table.size() + " studi");
                for (PigVRicDiarioRowBean row : table) {
                    String globalHash = row.getString("cd_key_object");
                    try {
                        callWSVersamentoWarning(globalHash, motivazione);
                        currentRow++;
                    } catch (WebGenericException e) {
                        String descError = e.getCode() != null ? messagesCtx.getVersWarnError(e.getCode(),
                                e.getDescription(), currentRow, table.size()) : e.getMessage();
                        log.debug(descError);
                        getMessageBox().addError(descError);
                    }
                    if (getMessageBox().hasError()) {
                        break;
                    }
                }
            } else {
                log.debug("Motivazione versamento assente");
                getMessageBox().addError(messagesCtx.getNoMotivVersWarning());
            }
            if (!getMessageBox().hasError()) {
                log.debug("Versamenti in warning eseguiti con successo");
                getMessageBox().addInfo(messagesCtx.getVersWarnMultipleOk(table.size(), table.size()));
                // setTableName(getForm().getStudiList().getName());
                // loadDettaglio();
                goBack();
            } else {
                forwardToPublisher(Application.Publisher.FORZA_ACCETTAZIONE_WARNING);
            }
        }
    }

    @Override
    public void annullaVersamentoWarning() throws EMFError {
        goBack();
    }

    /**
     * Eseguo la chiamata di restituzione dello studio dal Pre-Ingest
     * 
     * @throws EMFError
     *             eccezione generica
     */
    @Override
    public void restituzioneStudio() throws EMFError {
        String globalHash = getForm().getRicercaDiarioDettaglio().getDs_global_hash().getValue();
        log.debug("Eseguo la chiamata di restituzione dello studio " + globalHash);
        try {
            richRestStudioservice.callWS(globalHash);
        } catch (WebGenericException e) {
            log.error(e.getMessage());
            getMessageBox().addError(e.getMessage());
        }
        if (!getMessageBox().hasError()) {
            log.debug("Chiamata eseguita con successo");
            getMessageBox().addInfo(messagesCtx.getServiceOk());
            setTableName(getRequest().getParameter("table"));
            loadDettaglio();
        }
        forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
    }

    /**
     * Eseguo il trasferimento dello studio al PACS
     * 
     * @throws EMFError
     *             eccezione generica
     */
    @Override
    public void trasferimentoStudio() throws EMFError {
        getForm().getStudySummarySection().setHidden(false);
        getForm().getTrasferimentoPacs().clear();

        getForm().getTrasferimentoPacs().getTransfer_node().setDecodeMap(getTransferDicomNodesDecodeMap());
        String nodoDicom = getForm().getRicercaDiarioDettaglio().getCd_aet_nodo_dicom().getValue();
        getForm().getTrasferimentoPacs().getTransfer_node().setValue(nodoDicom);

        getForm().getTrasferimentoPacs().setEditMode();
        forwardToPublisher(Application.Publisher.TRASFERIMENTO_PACS);
    }

    @Override
    public void confermaTrasferimento() throws EMFError {
        if (getForm().getTrasferimentoPacs().postAndValidate(getRequest(), getMessageBox())) {
            String nodoDicom = getForm().getTrasferimentoPacs().getTransfer_node().parse();
            log.debug("Conferma trasferimento pacs al nodo " + nodoDicom);
            // Estraggo tutti i dati per eseguire il trasferimento
            String globalHash = getForm().getRicercaDiarioDettaglio().getDs_global_hash().getValue();
            // String nodoDicom =
            // getForm().getRicercaDiarioDettaglio().getCd_aet_nodo_dicom().getValue();
            String chiaveUnitaDoc = getForm().getRicercaDiarioDettaglio().getChiave_unita_doc().getValue();
            String udFileName = Constants.UD_FILE_PREFIX_DPI.concat(globalHash).concat(Constants.ZIP_EXTENSION);
            String studyUid = getForm().getRicercaDiarioDettaglio().getDs_study_instance_uid().getValue();
            File notifDir = new File(
                    ctx.getWorkingPath().concat(ctx.getStudioDicomPath()).concat(DPIConstants.NOTIFICATI_FOLDER),
                    globalHash);
            File fileWork = new File(storageSCUDir, nodoDicom + File.separator + DPIConstants.WORK_FOLDER);
            File chiaveUnitaDocDir = new File(fileWork, chiaveUnitaDoc);
            File[] filesInUdZip = null;
            File[] dicomFiles = null;
            File dicomZip = null;
            CStoreBean result = null;
            String zipPathUdFile = notifDir + File.separator + udFileName;
            // Ottengo il nodoDicom corrispondente al valore della combo
            // da cui potrò prendere hostname e porta
            DicomNode node = null;
            for (DicomNode d : transferDicomNodes) {
                if (d.getAet().equalsIgnoreCase(nodoDicom)) {
                    node = d;
                    break;
                }
            }
            // Creo la transazione
            Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                // Verifico l'esistenza del percorso e del file
                log.debug("Verifico l'esistenza del percorso e del file");
                if (XAUtil.fileExistsAndIsDirectory(xaSession, notifDir)) {
                    if (XAUtil.fileExists(xaSession, new File(zipPathUdFile))) {
                        if (Util.verificaPresenzaDirInZip(zipPathUdFile, chiaveUnitaDoc)) {
                            if (!XAUtil.fileExistsAndIsDirectoryLockExclusive(xaSession, chiaveUnitaDocDir)) {
                                log.debug("Creo la directory " + chiaveUnitaDoc + "in work");
                                // Creo all'interno della directory work una
                                // directory chiamata come la chiave
                                XAUtil.createDirectory(xaSession, chiaveUnitaDocDir);

                                // Creo all'interno della directory chiave una
                                // directory chiamata come lo study UID
                                log.debug("Creo la directory " + studyUid + "in " + chiaveUnitaDoc);
                                File studyUidDir = new File(chiaveUnitaDocDir, studyUid);
                                XAUtil.createDirectory(xaSession, studyUidDir);

                                /*
                                 * Estraggo dallo zip la directory chiaveUnitaDoc e copio il suo contenuto nella
                                 * directory work nel filesystem
                                 */
                                log.debug("Estraggo dallo zip la directory " + chiaveUnitaDoc
                                        + " nella directory creata");
                                filesInUdZip = Util.estraiFileDaZipTx(new File(zipPathUdFile), chiaveUnitaDoc,
                                        chiaveUnitaDocDir.getPath(), xaSession, false);
                                // L'estrazione dovrebbe aver salvato nella
                                // directory chiave un file. Lo verifico
                                if (filesInUdZip.length == 1) {
                                    for (File file : filesInUdZip) {
                                        // Se esiste, estraggo il file salvato
                                        // per ricavarne il contenuto
                                        dicomZip = file;
                                        dicomFiles = Util.estraiFileDaZipTx(file, "/", studyUidDir.getPath(), xaSession,
                                                true);
                                    }
                                } else {
                                    log.error(messagesCtx.getZipFileLengthError());
                                    getMessageBox().addError(messagesCtx.getZipFileLengthError());
                                }
                            } else {
                                log.info(
                                        "Nella cartella work è già presente uno studio con la stessa chiave, procedo con la send");
                            }
                        } else {
                            log.error(messagesCtx.getPacsStudyDirNotPresent());
                            getMessageBox().addError(messagesCtx.getPacsStudyDirNotPresent());
                        }
                    } else {
                        log.error(messagesCtx.getNoUdFile());
                        getMessageBox().addError(messagesCtx.getNoUdFile());
                    }
                } else {
                    log.error(messagesCtx.getNoGlobalHashDir());
                    getMessageBox().addError(messagesCtx.getNoGlobalHashDir());
                }
                // elimino il file zip nella cartella chiave
                log.debug("elimino lo zip nella cartella chiave");
                if (dicomZip != null) {
                    XAUtil.deleteFile(xaSession, dicomZip);
                }
                // Eseguo il commit dei dati prima dell'invio
                xaSession.commit();

            } catch (Exception e) {
                try {
                    log.error("Si è verificato un errore durante la transazione.", e);
                    xaSession.rollback();
                } catch (NoTransactionAssociatedException e1) {
                    log.error(
                            "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                            e1);
                }
                getMessageBox().addError(messagesCtx.getFatalError());
            }
            try {
                xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
                if (XAUtil.fileExistsAndIsDirectoryLockExclusive(xaSession, chiaveUnitaDocDir)) {
                    if (!getMessageBox().hasError() && dicomFiles != null) {
                        // Verifico la raggiungibilità del nodo dicom
                        log.debug("Verifico la raggiungibilità del nodo dicom");
                        echoService.doCEcho(node.getHostname(), node.getPort(), nodoDicom);
                        // Il primo elemento dell'array dovrebbe essere la
                        // directory
                        // principale
                        File studyUidDir = new File(chiaveUnitaDocDir, studyUid);
                        if (studyUidDir.exists() && studyUidDir.isDirectory()) {
                            // Eseguo la chiamata di trasferimento dei dati
                            log.debug("Eseguo la chiamata di trasferimento dei dati");
                            result = pacsSender.doCStore(node.getHostname(), node.getPort(), nodoDicom, studyUidDir);
                            log.debug(messagesCtx.getDicomNodeOk(result.getNumImagesInStudy(),
                                    result.getTransferedImagesInStudy()));
                            getMessageBox().addInfo(messagesCtx.getDicomNodeOk(result.getNumImagesInStudy(),
                                    result.getTransferedImagesInStudy()));
                            // Verifico che siano stati trasferiti tutti i
                            // dati
                            if (!result.isStudyComplete()) {
                                log.debug(messagesCtx.getDicomNodeError());
                                getMessageBox().addError(messagesCtx.getDicomNodeError());
                            }
                            // Sia che io abbia trasferito tutte le immagini
                            // e serie o no, elimino i dati dalla directory work
                            log.debug("Rimuovo i dati dalla directory work");
                            Util.rimuoviFileRicorsivamente(chiaveUnitaDocDir, xaSession);
                        }
                    }
                }
                xaSession.commit();

            } catch (Exception e) {
                try {
                    log.error("Si è verificato un errore durante la transazione.", e);
                    xaSession.rollback();
                } catch (NoTransactionAssociatedException e1) {
                    log.error(
                            "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                            e1);
                }
                getMessageBox().addError(messagesCtx.getFatalError());
            }
        }
        if (getMessageBox().hasError()) {
            forwardToPublisher(Application.Publisher.TRASFERIMENTO_PACS);
        } else {
            goBack();
        }
    }

    @Override
    public void annullaTrasferimento() throws EMFError {
        goBack();
    }

    @Override
    public void eliminaRecuperoStudio() throws EMFError {
        String globalHash = getForm().getRicercaDiarioDettaglio().getDs_global_hash().getValue();
        File notifDir = new File(
                ctx.getWorkingPath().concat(ctx.getStudioDicomPath()).concat(DPIConstants.NOTIFICATI_FOLDER),
                globalHash);
        // Verifico che sia presente la cartella del globalHash all'interno
        // della directory "notificati"
        // Se non esiste, errore
        Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
        try {
            if (XAUtil.fileExistsAndIsDirectory(xaSession, notifDir)) {
                Util.rimuoviFileRicorsivamente(notifDir, xaSession);
            } else {
                getMessageBox().addWarning(messagesCtx.getNoGlobalHashDir());
            }
            xaSession.commit();
        } catch (Exception e) {
            try {
                log.error("Si è verificato un errore durante la transazione.", e);
                xaSession.rollback();
            } catch (NoTransactionAssociatedException e1) {
                log.error("Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                        e1);
            }
            getMessageBox().addError(messagesCtx.getFatalError());
        }
        /*
         * Chiamata al servizio di Pulizia Notificato del Pre Ingest
         */
        try {
            puliziaNotifService.callWS(globalHash);
        } catch (WebGenericException e) {
            getMessageBox().addError(e.getMessage());
        }

        if (!getMessageBox().hasError()) {
            getMessageBox().addInfo(messagesCtx.getRicDiarioOpOk());
            setTableName(getRequest().getParameter("table"));
            loadDettaglio();
        }
        forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
    }

    /**
     * Ricerca il file delle prove di conservazione su filesystem e ne permette il download
     * 
     * @throws Throwable
     *             eccezione generica
     */
    @Override
    public void downloadProveConservazione() throws Throwable {
        String globalHash = getForm().getRicercaDiarioDettaglio().getDs_global_hash().getValue();
        File notifDir = new File(
                ctx.getWorkingPath().concat(ctx.getStudioDicomPath()).concat(DPIConstants.NOTIFICATI_FOLDER),
                globalHash);
        // Verifico che sia presente la cartella del globalHash all'interno
        // della directory "notificati"
        // Se non esiste, errore
        if (notifDir.exists()) {
            String[] fileNames = notifDir.list();
            String pcFileName = Constants.PC_FILE_PREFIX_DPI.concat(globalHash).concat(Constants.ZIP_EXTENSION);
            String zipPathPcFile = notifDir + File.separator + pcFileName;
            // Verifico la presenza dello zip delle prove di conservazione nella
            // directory
            // Se non esiste, errore
            if (Arrays.asList(fileNames).contains(pcFileName)) {
                // Eseguo la copia dell'outputStream per il download
                getResponse().setContentType("application/zip");
                getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + pcFileName + "\"");
                OutputStream out = new DataOutputStream(getServletOutputStream());
                FileUtils.copyFile(new File(zipPathPcFile), out);
                out.flush();
                out.close();
                freeze();
            } else {
                getMessageBox().addError(messagesCtx.getNoPcFile());
            }
        } else {
            getMessageBox().addError(messagesCtx.getNoGlobalHashDir());
        }
        if (getMessageBox().hasError()) {
            // Eseguo la ricarica della pagina se e solo se ci sono stati errori
            forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
        }
    }

    /**
     * Trigger sull'utilizzo del campo Dl_motivo_chiuso_warning_standard copia il valore contenuto nel campo del trigger
     * sul campo Dl_motivo_chiuso_warning
     * 
     * @throws EMFError
     *             eccezione generica
     */
    @Override
    public JSONObject triggerGestioneWarningDl_motivo_chiuso_warning_standardOnTrigger() throws EMFError {
        getForm().getGestioneWarning().getDl_motivo_chiuso_warning_standard().post(getRequest());
        String value = getForm().getGestioneWarning().getDl_motivo_chiuso_warning_standard().getValue();
        getForm().getGestioneWarning().getDl_motivo_chiuso_warning().setValue(value);
        return getForm().getGestioneWarning().asJSON();
    }

    /**
     * Carica la pagina di ricerca restituzioni studi inizializzando i campi
     * 
     * @throws EMFError
     *             eccezione generica
     */
    // @Secure(action = "Menu.Monitoraggio.RicercaRestituzioneStudi")
    public void loadRicercaRestituzioniStudi() throws EMFError {
        getUser().getMenu().reset();
        getUser().getMenu().select("Menu.Monitoraggio.RicercaRestituzioneStudi");

        getForm().getRicercaRestituzioniStudi().clear();
        // Inizializzo il campo ti_stato con la decodeMap degli stati
        getForm().getRicercaRestituzioniStudi().getTi_stato().setDecodeMap(getStatiRecuperoStudioDecodeMap());
        // Inizializzo il campo nodiDicom con la decodeMap dei nodi dicom
        // indicando il valore di default nel caso esista solo un nodo dicom
        getForm().getRicercaRestituzioniStudi().getNodo_dicom().setDecodeMap(getDicomNodesForResearchDecodeMap());
        if (pacsListForSearch.size() == 1) {
            getForm().getRicercaRestituzioniStudi().getNodo_dicom().setValue(pacsListForSearch.get(0));
        }
        // Inizializzo il campo filtro paziente indicando il valore di
        // default
        getForm().getRicercaRestituzioniStudi().getFiltro_paziente().setDecodeMap(getFiltroPazienteDecodeMap());
        getForm().getRicercaRestituzioniStudi().getFiltro_paziente()
                .setValue(Constants.ValoriOperatore.INIZIA_PER.name());
        // Inizializzo il campo sesso paziente
        getForm().getRicercaRestituzioniStudi().getSesso_paziente().setDecodeMap(getSexDecodeMap());
        // Inizializzo il campo data apertura sessione da alla data corrente
        DateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        getForm().getRicercaRestituzioniStudi().getDt_apertura_sessione_da()
                .setValue(formato.format(Calendar.getInstance().getTime()));
        // Imposto i filtri in edit mode
        getForm().getRicercaRestituzioniStudi().setEditMode();
        getForm().getRicercaRestituzioniStudi().getRicercaRestituzioniStudi().setEditMode();
        getForm().getRestituzioniStudiList().setTable(new PigVRicDiarioTableBean());

        // Carico la pagina di ricerca
        forwardToPublisher(Application.Publisher.RICERCA_RESTITUZIONI_STUDI);
    }

    @Override
    public void ricercaRestituzioniStudi() throws EMFError {
        // Eseguo la ricerca restituzioni studi: valido i valori e creo l'xml di
        // richiesta
        if (getLastPublisher().equals(Application.Publisher.RICERCA_RESTITUZIONI_STUDI)) {
            MonitoraggioForm.RicercaRestituzioniStudi filtri = getForm().getRicercaRestituzioniStudi();
            // Esegue la post dei filtri compilati
            filtri.post(getRequest());
            // Valida i filtri per verificare quelli obbligatori
            if (filtri.validate(getMessageBox())) {
                caricaListaRicercaRestituzioniStudi(filtri);
            }
            forwardToPublisher(getLastPublisher());
        }
    }

    private void caricaListaRicercaRestituzioniStudi(RicercaRestituzioniStudi filtri) throws EMFError {
        // Valida i campi di ricerca
        TypeValidator validator = new TypeValidator(getMessageBox());
        // Valido i filtri data acquisizione da - a restituendo le date
        // comprensive di orario
        Date[] dateStudi = validator.validaDate(filtri.getDt_study_da().parse(), filtri.getOre_dt_study_da().parse(),
                filtri.getMinuti_dt_study_da().parse(), filtri.getDt_study_a().parse(),
                filtri.getOre_dt_study_a().parse(), filtri.getMinuti_dt_study_a().parse(),
                filtri.getDt_study_da().getHtmlDescription(), filtri.getDt_study_a().getHtmlDescription());
        Date[] dateAperturaSessioni = validator.validaDate(filtri.getDt_apertura_sessione_da().parse(),
                filtri.getOre_dt_apertura_sessione_da().parse(), filtri.getMinuti_dt_apertura_sessione_da().parse(),
                filtri.getDt_apertura_sessione_a().parse(), filtri.getOre_dt_apertura_sessione_a().parse(),
                filtri.getMinuti_dt_apertura_sessione_a().parse(),
                filtri.getDt_apertura_sessione_da().getHtmlDescription(),
                filtri.getDt_apertura_sessione_a().getHtmlDescription());
        PigVRicRecupTableBean tableBean = new PigVRicRecupTableBean();
        if (!getMessageBox().hasError()) {
            // Non ho ricevuto errori nella validazione delle date,
            // perciò genero l'xml di richiesta
            try {
                tableBean = ricercaService.callRicercaRestituzioniStudiService(filtri, dateAperturaSessioni, dateStudi,
                        0, Integer.parseInt(ctx.getWebNumRecords()));
            } catch (JAXBException e) {
                log.error("Errore di marshalling della richiesta", e);
                getMessageBox()
                        .addError(messagesCtx.getGeneralError("Errore di marshalling della richiesta", e.getMessage()));
            } catch (WebGenericException e) {
                log.error("Errore dei filtri di ricerca diario : " + e.getMessage());
                getMessageBox().addError(e.getMessage());
            }
        }
        getForm().getRestituzioniStudiList().setTable(tableBean);
        getForm().getRestituzioniStudiList().getTable().setPageSize(10);
        getForm().getRestituzioniStudiList().getTable().first();
    }

    @Override
    public void startConfronto() throws EMFError {
        if (getForm().getConfrontoPacsPreIngest().postAndValidate(getRequest(), getMessageBox())) {
            Date from = getForm().getConfrontoPacsPreIngest().getDt_confronto_da().parse();
            Date to = getForm().getConfrontoPacsPreIngest().getDt_confronto_a().parse();

            Calendar toCal = Calendar.getInstance();
            toCal.setTime(to);

            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_MONTH, -ctx.getDayBeforeNow());

            long days = Util.getDateDiff(from, to, TimeUnit.DAYS);
            long daysFromNow = Util.getDateDiff(to, Calendar.getInstance().getTime(), TimeUnit.DAYS);

            if (toCal.after(now) || days > ctx.getDayLimit() || from.after(to) || from.before(startDate)) {
                getMessageBox().addError(
                        messagesCtx.getQueryPacsDateError(new SimpleDateFormat("dd/MM/yyyy").format(startDate)));
            } else {

                int filtroGiorniDelay = (int) daysFromNow;
                int filtroOreBefore = (int) days * 24;

                getForm().getConfrontoPacsPreIngest().getFiltro_giorni_delay()
                        .setValue(String.valueOf(filtroGiorniDelay));
                getForm().getConfrontoPacsPreIngest().getFiltro_ore_before().setValue(String.valueOf(filtroOreBefore));

                List<CompareBean> compareList = null;
                try {
                    compareList = queryPacsPingJob.comparePacsPing(filtroGiorniDelay, filtroOreBefore);
                } catch (Exception e) {
                    getMessageBox().addError(messagesCtx.getGeneralError(e.getMessage()));
                }
                ConfrontiPacsTableBean tb = new ConfrontiPacsTableBean();
                int studiDaTrasferire = 0;
                if (compareList != null && !compareList.isEmpty()) {
                    // Creazione TableBean
                    for (CompareBean bean : compareList) {
                        ConfrontiPacsRowBean rb = new ConfrontiPacsRowBean();
                        rb.entityToRowBean(bean);
                        studiDaTrasferire += rb.getNumStudyDaTrasferire().intValue();
                        tb.add(rb);
                    }
                }

                if (studiDaTrasferire == 0) {
                    getForm().getConfrontoPacsPreIngest().getConfermaConfronto().setReadonly(true);
                }

                getForm().getConfrontiList().setTable(tb);
                getForm().getConfrontiList().getTable().setPageSize(Constants.NumPages.NUM_RECORDS_100.getValue());
                getForm().getConfrontiList().getTable().first();
            }
        }
        if (!getMessageBox().hasError()) {
            getForm().getConfrontoPacsPreIngest().getDt_confronto_da().setViewMode();
            getForm().getConfrontoPacsPreIngest().getDt_confronto_a().setViewMode();
            forwardToPublisher(Application.Publisher.CONFERMA_CONFRONTO_PACS_PRE_INGEST);
        } else {
            getRequest().setAttribute("queryPacsStartDate", dateFormat.format(startDate));
            forwardToPublisher(getLastPublisher());
        }
    }

    public void loadConfrontoPacs() throws EMFError {
        getUser().getMenu().reset();
        getUser().getMenu().select("Menu.Monitoraggio.ConfrontoPacs");

        getForm().getConfrontoPacsPreIngest().clear();
        getForm().getConfrontoPacsPreIngest().setEditMode();
        getRequest().setAttribute("queryPacsStartDate", dateFormat.format(startDate));

        // Carico la pagina di ricerca
        forwardToPublisher(Application.Publisher.CONFRONTO_PACS_PRE_INGEST);
    }

    private boolean checkJobCurrentlyExecuting(String name) throws SchedulerException {
        for (JobExecutionContext jobCtx : scheduler.getScheduler().getCurrentlyExecutingJobs()) {
            if (jobCtx.getJobDetail().getKey().getName().equals(name)) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    /*
     * TABS DETTAGLIO
     */
    @Override
    public void tabInfoStudioOnClick() throws EMFError {
        getForm().getDettaglioTabs().setCurrentTab(getForm().getDettaglioTabs().getInfoStudio());
        forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
    }

    @Override
    public void tabInfoRecuperoOnClick() throws EMFError {
        getForm().getDettaglioTabs().setCurrentTab(getForm().getDettaglioTabs().getInfoRecupero());
        forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
    }

    @Override
    public void tabInfoSessioneOnClick() throws EMFError {
        getForm().getDettaglioTabs().setCurrentTab(getForm().getDettaglioTabs().getInfoSessione());
        forwardToPublisher(Application.Publisher.DETTAGLIO_STUDIO);
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public void confermaConfronto() throws EMFError {
        try {
            // MethodInvokingJobDetailFactoryBean jobDetail = new
            // MethodInvokingJobDetailFactoryBean();
            // jobDetail.setName("queryPacsPingQJobInstantly");
            // jobDetail.setGroup(JobKey.DEFAULT_GROUP);
            // jobDetail.setTargetObject(queryPacsService);
            // jobDetail.setTargetMethod("doInstantly");
            // jobDetail.setConcurrent(false);
            // jobDetail.setArguments(new Object[] {
            // filtroGiorniDelay,
            // filtroOreBefore });
            // jobDetail.afterPropertiesSet();
            int filtroGiorniDelay = getForm().getConfrontoPacsPreIngest().getFiltro_giorni_delay().parse().intValue();
            int filtroOreBefore = getForm().getConfrontoPacsPreIngest().getFiltro_ore_before().parse().intValue();

            JobDetail job = (JobDetail) context.getBean("queryPacsPingQJobInstantly");
            MethodInvokingJobDetailFactoryBean jobDetailFactory = (MethodInvokingJobDetailFactoryBean) job
                    .getJobDataMap().get("methodInvoker");
            jobDetailFactory.setArguments(new Object[] { filtroGiorniDelay, filtroOreBefore });
            jobDetailFactory.afterPropertiesSet();
            // job.getJobDataMap().put("methodInvoker",
            // jobDetailFactory);

            SimpleTrigger trigger = (SimpleTrigger) newTrigger()
                    .withIdentity("simpleTrigger1", TriggerKey.DEFAULT_GROUP).forJob(job)
                    .startAt(DateBuilder.futureDate(5, IntervalUnit.SECOND)).build();

            if (!checkJobCurrentlyExecuting(job.getKey().getName())) {
                if (scheduler.getScheduler().getJobDetail(job.getKey()) == null) {
                    scheduler.getScheduler().scheduleJob(job, trigger);
                } else {
                    scheduler.getScheduler().scheduleJob(trigger);
                }
            } else {
                getMessageBox().addError(messagesCtx.getQueryPacsAlreadyExecuting());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (!getMessageBox().hasError()) {
            getMessageBox().addInfo(messagesCtx.getServiceOk());
            goBack();
        } else {
            forwardToPublisher(getLastPublisher());
        }
    }

    @Override
    public void annullaConfronto() throws EMFError {
        getForm().getConfrontoPacsPreIngest().clear();
        getForm().getConfrontoPacsPreIngest().setEditMode();
        getForm().getConfrontoPacsPreIngest().getConfermaConfronto().setReadonly(false);
        goBack();
    }
}
