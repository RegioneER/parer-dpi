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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.SOPClassSingleton;
import it.eng.dpi.dicom.scp.TxStoreSCP;
import it.eng.dpi.service.DPIConstants;
import it.eng.dpi.service.DicomServer;
import it.eng.sacerasi.ws.RichiestaSopClassList;
import it.eng.sacerasi.ws.RichiestaSopClassListRisposta;
import it.eng.sacerasi.ws.SopClassRespType;

@Component
public class UpdateSOPClassJob extends AbstractWSClient implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("UPDATE_SOPCLASS");
    private static final Logger log = LoggerFactory.getLogger(UpdateSOPClassJob.class);

    @Autowired
    private SOPClassSingleton sopClasses;
    @Autowired
    private TxStoreSCP txStoreSCP;
    @Autowired
    private DicomServer dicomServer;

    private RichiestaSopClassList client;

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            if (client == null) {
                client = init(ctx.getWsdlAllineaSopClassUrl(), RichiestaSopClassList.class);
            }
            if (client == null) {
                audit.info(DPIConstants.AUDIT_JOB_ERROR + " WEB-SERVICE NON INIZIALIZZATO");
            } else {
                updateSOPClass();
                audit.info(DPIConstants.AUDIT_JOB_STOP);
            }
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void updateSOPClass() {

        RichiestaSopClassListRisposta risposta = client.richiestaSopClassList(ctx.getNmAmbiente(),
                ctx.getNmVersatore());
        log.info("RISPOSTA DEL WS: " + risposta.getCdEsito() + " "
                + ((risposta.getCdErr() != null) ? risposta.getCdErr() + " " + risposta.getDsErr() : ""));
        List<String> sopClassList = new ArrayList<String>();
        for (SopClassRespType resp : risposta.getListaSOPClass().getSopClass()) {
            sopClassList.add(resp.getCdSopClass());
        }
        sopClasses.setSopClassList(sopClassList.toArray(new String[0]));
        dicomServer.refreshSopClass(sopClasses.getSopClassList());
        txStoreSCP.refreshSopClass(dicomServer.getAeStoreSCP(), sopClasses.getSopClassList());

    }

}
