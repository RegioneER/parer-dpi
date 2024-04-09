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
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.bean.ObjectType;
import it.eng.dpi.business.AbstractWSClient;
import it.eng.dpi.business.JobInterface;
import it.eng.dpi.component.Util;
import it.eng.dpi.component.XAUtil;
import it.eng.dpi.service.DPIConstants;

@Component
public class ObjectCopyJob extends AbstractWSClient implements JobInterface {

    private static final Logger audit = LoggerFactory.getLogger("COPIA_OGGETTO");
    private static final Logger log = LoggerFactory.getLogger(ObjectCopyJob.class);

    @Resource
    @Qualifier("objectTypes")
    private List<ObjectType> objectTypes;

    @Resource
    @Qualifier("delayTime")
    // MEV#27600
    private Optional<Integer> delayTime;
    // end MEV#27600

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Override
    public void doWork() {
        try {
            audit.info(DPIConstants.AUDIT_JOB_START);
            int numRaccProc = process();
            audit.info("Il job ha terminato l'esecusione. Sono state processate " + numRaccProc
                    + " cartelle raccoglitore");
            audit.info(DPIConstants.AUDIT_JOB_STOP);
        } catch (Exception e) {
            log.error("Errore durante l'esecuzione del job", e);
            audit.info(DPIConstants.AUDIT_JOB_ERROR + ": " + e.getMessage());
        }
    }

    public int process() throws Exception {
        int numRaccProc = 0;
        for (ObjectType objectType : objectTypes) {
            log.debug(objectType.toString());

            File inFolder = new File(objectType.getInputPath() + File.separatorChar + objectType.getObjType());
            File outFolder = new File(objectType.getCopiatoPath() + File.separatorChar + objectType.getObjType());
            File workFolder = new File(ctx.getWorkingPath() + File.separatorChar + objectType.getObjType());

            log.debug("Processo il tipo oggetto " + objectType.getObjType());
            log.debug("Directory input " + inFolder);
            log.debug("Directory output " + outFolder);
            // ricavo le cartelle del produttore
            File[] folders = XAUtil.listFilesNewTX(xaDiskNativeFS, inFolder);
            XAUtil.createDirIfNotExistNewTx(xaDiskNativeFS, outFolder);
            Calendar cal = Calendar.getInstance();
            log.debug("Data attuale " + cal.getTime());
            // MEV#27600
            log.debug("Ritardo in giorni nella lettura della cartella produttore: " + delayTime.orElse(null)
                    + " giorno/i");
            if (delayTime.isPresent()) {
                cal.add(Calendar.DATE, (-delayTime.get()));
            }
            // end MEV#27600
            log.debug("Data da cui processare le cartelle " + cal.getTime());
            long sysdate = cal.getTimeInMillis();
            // ciclo sulle cartelle produttore trovate nella cartella input
            for (File folder : folders) {
                // produttore da processare
                log.debug("processo il produttore: " + folder);
                File producerDpiDir = new File(workFolder, folder.getName());
                File producerInDir = new File(inFolder, folder.getName());
                File producerOutDir = new File(outFolder, folder.getName());
                // controllo se nel file system del DPI esiste la cartella <produttore>. Se non esiste la creo.
                XAUtil.createDirIfNotExistNewTx(xaDiskNativeFS, producerDpiDir);
                // controllo se nalla directory di output esiste la cartella <produttore>. Se non esiste la creo.
                XAUtil.createDirIfNotExistNewTx(xaDiskNativeFS, producerOutDir);
                // ricavo le cartelle dei raccoglitori dalla directory di input
                File[] raccoglitori = XAUtil.listFilesNewTX(xaDiskNativeFS, producerInDir);
                log.debug("Recupero le cartelle dei raccoglitori dalla directory di input " + producerInDir + ": "
                        + raccoglitori.length + " trovate");
                Session session = null;
                for (File raccoglitore : raccoglitori) {
                    long lastModified = raccoglitore.lastModified();
                    if (lastModified < sysdate) {
                        try {
                            session = xaDiskNativeFS.createSessionForLocalTransaction();
                            log.debug("Processo il raccoglitore " + raccoglitore.toString());
                            // controllo se nel file system del DPI esiste la cartella <raccoglitore> corrente. Se non
                            // esiste la creo.
                            File raccoglitoreDpiDir = new File(producerDpiDir, raccoglitore.getName());
                            if (!XAUtil.fileExistsAndIsDirectory(session, raccoglitoreDpiDir)) {
                                log.debug("la cartella " + raccoglitoreDpiDir + " non esiste... la creo");
                                XAUtil.createDirectory(session, raccoglitoreDpiDir);
                                // copio tutti i file presenti nella cartella raccoglitore input
                                // nell'omonima cartella presente nel file system del DPI
                                File raccoglitoreInDir = new File(producerInDir, raccoglitore.getName());
                                log.debug("Copio in " + raccoglitoreDpiDir + " tutti i file presenti in "
                                        + raccoglitoreInDir);
                                File[] elencoFile = XAUtil.listFiles(session, raccoglitoreInDir);
                                log.debug("trovati " + elencoFile.length + " da copiare");
                                int copied = 0;
                                for (File fileToCopy : elencoFile) {
                                    File fileDest = new File(raccoglitoreDpiDir, fileToCopy.getName());
                                    XAUtil.copyFile(session, fileToCopy, fileDest);
                                    copied++;
                                    log.debug("copiato file " + copied + " (" + fileDest + ") di " + elencoFile.length
                                            + " da copiare");
                                }
                                log.debug("copiati " + copied + " file.");
                                numRaccProc++;
                                // sposto la cartella raccoglitore appena elaborata dalla cartella di input a quella dei
                                // copiati
                                log.debug("sposto la cartella raccoglitore " + raccoglitoreDpiDir
                                        + " appena elaborata dalla cartella di input a quella dei copiati");
                                File raccoglitoreOutDir = new File(producerOutDir, raccoglitore.getName());
                                if (XAUtil.fileExistsAndIsDirectory(session, raccoglitoreOutDir)) {
                                    Util.rimuoviFileRicorsivamente(raccoglitoreOutDir, session);
                                }
                                XAUtil.moveFile(session, raccoglitoreInDir, raccoglitoreOutDir);
                            } else {
                                // la cartella raccoglitore esiste già. Segnalo l'accaduto e passo avanti.
                                // La cartella sarà elaborata alla prossima esecuzione del job.
                                audit.warn("Nel file system del DPI, per il tipo oggetto " + objectType.getObjType()
                                        + " e il produttore " + folder.getName() + " è già definita la cartella "
                                        + raccoglitoreDpiDir.getName() + ".");
                            }
                            session.commit();
                        } catch (Exception e) {
                            if (numRaccProc != 0) {
                                numRaccProc = numRaccProc - 1;
                            }
                            ;
                            audit.error("Il job ha terminato l'esecusione a causa di un errore. Sono state processate "
                                    + numRaccProc + " cartelle raccoglitore");
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
                            // si è deciso che in caso di eccezione si deve interrompere l'esecuzione
                            throw e;
                        }
                    } else {
                        log.debug("Trovata cartella: " + raccoglitore.getName()
                                + ". Non verrà processata perché data ultima modifica > sysdate)");
                    }
                }
            }
        }
        return numRaccProc;
    }

}
