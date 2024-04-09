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
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.DPIContext;
import it.eng.dpi.component.FTPUtil;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;

@Component
public class PrelievoFTPJob implements JobInterface {
    private static final Logger audit = LoggerFactory.getLogger("PRELIEVO_FTP");
    private static final Logger log = LoggerFactory.getLogger(PrelievoFTPJob.class);

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Autowired
    private DPIContext ctx;

    private File disponibiliDir;
    private File prelevatiDir;

    @PostConstruct
    public void init() {
        // disponibiliDir = new File(ctx.getWorkingPath() + DPIConstants.DISPONIBILI_FOLDER);
        // prelevatiDir = new File(ctx.getWorkingPath() + DPIConstants.PRELEVATI_FOLDER);
        disponibiliDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.DISPONIBILI_FOLDER);
        prelevatiDir = new File(ctx.getWorkingPath() + ctx.getStudioDicomPath() + DPIConstants.PRELEVATI_FOLDER);
    }

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            processFolder();
            audit.info(DPIConstants.AUDIT_JOB_STOP);
        } catch (Exception e) {
            audit.info(DPIConstants.AUDIT_JOB_ERROR + " " + e.getMessage());
            log.error("Errore durante l'esecuzione del job", e);
        }

    }

    private void processFolder() throws MalformedURLException, XAGenericException {
        File[] folder = XAUtil.listFilesNewTX(xaDiskNativeFS, disponibiliDir);
        for (File globalHashFolder : folder) {
            log.debug("Elaboro la cartella " + globalHashFolder.getAbsolutePath());
            Session session = xaDiskNativeFS.createSessionForLocalTransaction();
            try {
                File dir = new File(prelevatiDir, globalHashFolder.getName());
                XAUtil.createDirectory(session, dir);
                String udFileName = "UD_" + globalHashFolder.getName() + ".zip";
                String pcFileName = "PC_" + globalHashFolder.getName() + ".zip";
                String[] filename = { udFileName, pcFileName };
                OutputStream[] os = { XAUtil.createFileOS(session, new File(dir, udFileName), true),
                        XAUtil.createFileOS(session, new File(dir, pcFileName), true) };
                boolean ok = FTPUtil.retrieveFile(filename, os, ctx.getFtpOutputFolder(), ctx.getFtpIP(),
                        ctx.getFtpPort(), ctx.getFtpUser(), ctx.getFtpPassword(), ctx.getSecureFtp());
                if (!ok) {
                    session.rollback();
                } else {
                    XAUtil.deleteFile(session, globalHashFolder);
                    session.commit();
                }
            } catch (Exception e) {
                try {
                    if (!(e instanceof NoTransactionAssociatedException)) {
                        log.error("Si è verificato un errore durante la transazione.", e);
                        session.rollback();
                    }
                } catch (NoTransactionAssociatedException e1) {
                    log.error(
                            "Si è verificato un errore durante il rollback della transazione. Transazione non esistente.",
                            e1);
                }
            }

        }

    }

}
