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

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.JAXBSingleton;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;
import it.eng.dpi.web.util.Constants;
import it.eng.sacerasi.ws.OggettoRicDiarioType;
import it.eng.sacerasi.ws.RicercaDiario;
import it.eng.sacerasi.ws.RicercaDiarioRisposta;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType;
import it.eng.sacerasi.ws.xml.diariofiltri.FiltroType.FiltroUnValore;
import it.eng.sacerasi.ws.xml.diariofiltri.ListaFiltriType;

@Component
public class PuliziaInAttesaFileJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("PULIZIA_IN_ATTESA_FILE");
    private static final Logger log = LoggerFactory.getLogger(PuliziaInAttesaFileJob.class);

    private RicercaDiario client;

    private File warnFolder;
    private File outFolder;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private JAXBSingleton jaxbSingleton;

    @PostConstruct
    public void postConstruct() {
        // warnFolder = new File(ctx.getWorkingPath() + DPIConstants.WARN_FOLDER);
        // outFolder = new File(ctx.getWorkingPath() + DPIConstants.OUT_FOLDER);
        warnFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.WARN_FOLDER);
        outFolder = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.OUT_FOLDER);
    }

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            if (client == null) {
                client = init(ctx.getWsdlRicercaDiarioUrl(), RicercaDiario.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                processWarnFolder();
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }
    }

    private void processWarnFolder() throws MalformedURLException, XAGenericException, JAXBException {
        File[] folder = XAUtil.listFilesNewTX(xaDiskNativeFS, warnFolder);
        Set<String> globalHashWarnSet = new HashSet<String>();
        for (File study : folder) {
            String globalHash = study.getName().substring(0, study.getName().length() - 4);
            globalHashWarnSet.add(globalHash);
        }

        log.debug("Sono presenti nella cartella warn " + globalHashWarnSet.size() + " globalHash");

        Iterator<String> globalHashWarnIterator = globalHashWarnSet.iterator();
        while (globalHashWarnIterator.hasNext()) {

            StringBuilder stringaIN = new StringBuilder();
            String sep = "";
            for (int i = 0; i < DPIConstants.JOB_CLEAN_WARN_LIMIT && globalHashWarnIterator.hasNext(); i++) {
                stringaIN.append(sep);
                if (i == 0) {
                    sep = ",";
                }
                stringaIN.append(globalHashWarnIterator.next());
            }

            log.debug("Richiesta a ricerca diario per i globalHash " + stringaIN.toString());
            RicercaDiarioRisposta risposta = callWSRicercaDiario(stringaIN.toString());

            // Se la chiamata ha avuto esito OK e la lista di oggetti non è
            // vuota
            if (risposta != null) {
                switch (risposta.getCdEsito()) {
                case OK:
                    if (risposta.getListaOggetti() != null) {
                        audit.info("Numero di oggetti da spostare in OUT "
                                + risposta.getListaOggetti().getOggetto().size());
                        for (OggettoRicDiarioType ogg : risposta.getListaOggetti().getOggetto()) {
                            Session xaSession = xaDiskNativeFS.createSessionForLocalTransaction();
                            try {
                                String cdKeyObject = ogg.getCdKeyObject();
                                File zipStudyInWarn = new File(warnFolder, cdKeyObject + ".zip");
                                File zipStudyInOut = new File(outFolder, cdKeyObject + ".zip");
                                if (!XAUtil.fileExists(xaSession, zipStudyInOut)) {
                                    XAUtil.moveFile(xaSession, zipStudyInWarn, zipStudyInOut);
                                    XAUtil.deleteFile(xaSession, new File(warnFolder, cdKeyObject + ".xml"));
                                    audit.info("Oggetto spostato in OUT " + cdKeyObject);
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
                            }
                        }
                    }
                    break;
                case KO:
                    audit.info("Risposta KO del WS RicercaDiario ricercando i global hash " + stringaIN + "\nCODICE:"
                            + risposta.getCdErr() + "\nDESC:" + risposta.getDsErr());
                    break;
                }
            }

        }

    }

    private RicercaDiarioRisposta callWSRicercaDiario(String inClause) throws JAXBException {
        log.debug("Invoco il WS: RicercaDiario - URL: " + ctx.getWsdlRicercaDiarioUrl());

        // Creo la lista dei filtri di richiesta in base ai parametri forniti
        ListaFiltriType xmlfiltri = new ListaFiltriType();
        FiltroType filtro = new FiltroType();
        FiltroUnValore filtroUnValore = new FiltroUnValore();
        filtroUnValore.setDatoSpecifico(Constants.GLOBAL_hash);
        filtroUnValore.setOperatore(Constants.ValoriOperatore.IN.name());
        filtroUnValore.setValore(inClause);
        filtro.setFiltroUnValore(filtroUnValore);
        xmlfiltri.getFiltro().add(filtro);

        StringWriter swXmlFiltri = new StringWriter();
        Marshaller m = jaxbSingleton.getContextListaFiltriType().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        JAXBElement<ListaFiltriType> xmlFiltriJAXB = new it.eng.sacerasi.ws.xml.diariofiltri.ObjectFactory()
                .createListaFiltri(xmlfiltri);
        m.marshal(xmlFiltriJAXB, swXmlFiltri);

        return client.ricercaDiario(ctx.getNmAmbiente(), ctx.getNmVersatore(), DPIConstants.WS_NM_TIPO_OBJECT, null,
                null, Constants.StatoStudio.IN_ATTESA_FILE.name(), false, 1, 500, null, swXmlFiltri.toString(), null);
    }
}
