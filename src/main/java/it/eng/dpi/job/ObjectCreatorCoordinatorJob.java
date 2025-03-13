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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.io.comparator.SizeFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;

import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.FileQueue;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;

@Component
public class ObjectCreatorCoordinatorJob implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("COORDINATORE_CREA_OGGETTO");
    private static final Logger log = LoggerFactory.getLogger(ObjectCreatorCoordinatorJob.class);

    @Autowired
    private FileQueue fileQueue;

    @Resource
    @Qualifier("pacsList")
    private List<String> pacsList;

    @Resource
    @Qualifier("storageSCPDir")
    private File storageSCPDir;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    public void doWork(String... consumerIds) {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            for (String pacs : pacsList) {
                // /foo/bar/SCPStore/PACS1/CMOVE
                processPacs(pacs, consumerIds);
            }
            audit.info(DPIConstants.AUDIT_JOB_STOP);
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void processPacs(String pacs, String[] consumerIds) throws NoSuchAlgorithmException, XAGenericException {
        File fileCmove = new File(storageSCPDir, pacs + File.separator + DPIConstants.CMOVE_DIR);
        File fileCstore = new File(storageSCPDir, pacs + File.separator + DPIConstants.CSTORE_DIR);
        int numStudi = 0;

        File[] studiesCmove = XAUtil.listFilesNewTX(xaDiskNativeFS, fileCmove);
        File[] studiesCstore = XAUtil.listFilesNewTX(xaDiskNativeFS, fileCstore);

        List<File> studiesCmoveList = new ArrayList<>();
        List<File> studiesCstoreList = new ArrayList<>();

        // Creiamo e riutilizziamo l'istanza di Random
        Random random = new Random();
        int idx = random.nextInt(consumerIds.length);

        for (File f : studiesCmove) {
            if (!fileQueue.contains(f)) {
                studiesCmoveList.add(f);
            }
        }
        for (File f : studiesCstore) {
            if (!fileQueue.contains(f)) {
                studiesCstoreList.add(f);
            }
        }
        studiesCmove = studiesCmoveList.toArray(new File[0]);
        studiesCstore = studiesCstoreList.toArray(new File[0]);

        // Ordiniamo le cartelle per dimensione per bilanciare il carico
        Arrays.sort(studiesCmove, SizeFileComparator.SIZE_SUMDIR_REVERSE);
        Arrays.sort(studiesCstore, SizeFileComparator.SIZE_SUMDIR_COMPARATOR);

        for (File studyFolder : studiesCmove) {
            if (fileQueue.put(consumerIds[idx], studyFolder, DPIConstants.CMOVE_DIR, pacs)) {
                numStudi++;
            }
            idx = (--idx < 0) ? (consumerIds.length - 1) : idx;
        }
        for (File studyFolder : studiesCstore) {
            if (fileQueue.put(consumerIds[idx], studyFolder, DPIConstants.CSTORE_DIR, pacs)) {
                numStudi++;
            }
            idx = (--idx < 0) ? (consumerIds.length - 1) : idx;
        }

        audit.info(
                "PACS: " + pacs + " | STUDI AGGIUNTI: " + numStudi + " | STUDI TOTALI IN LISTA: " + fileQueue.size());
    }

    @Override
    public void doWork() {
        // TODO Auto-generated method stub

    }

}
