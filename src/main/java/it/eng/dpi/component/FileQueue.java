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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;

import it.eng.dpi.bean.ProcessableFile;
import it.eng.dpi.exception.XAGenericException;

/**
 * Implementazione di una coda per processare file.
 * 
 * La coda è un Set di ProcessableFile, non è possibile inserire due file con lo stesso percorso assoluto. In
 * ProcessableFile sono stati ridefiniti, infatti, i metodi hashcode e equals per basare l'uguaglianza sul percorso.
 * 
 * 
 * 
 * @author Quaranta_M
 *
 */
@Component
public class FileQueue {

    private static final Logger log = LoggerFactory.getLogger(FileQueue.class);
    Set<ProcessableFile> queue = new HashSet<ProcessableFile>();
    @Autowired
    private XAFileSystem xaDiskNativeFS;

    public boolean contains(File filePath) {
        return queue.contains(new ProcessableFile(null, filePath, null, null));
    }

    public synchronized boolean put(String consumerId, File filePath, String destinationSubDir, String pacsName)
            throws XAGenericException {

        if (!XAUtil.fileExistsNewTx(xaDiskNativeFS, filePath)) {
            log.debug("File non più esistente su filesystem. Path: " + filePath);
            return false;
        }
        if (queue.add(new ProcessableFile(consumerId, filePath, destinationSubDir, pacsName))) {
            log.debug("Nuovo file aggiunto in coda. Path: " + filePath + " - Consumer: " + consumerId);
            return true;
        } else {
            log.debug("File già presente in coda. Path: " + filePath + " - Consumer: " + consumerId);
            return false;
        }
    }

    public synchronized boolean remove(String consumerId, File filePath, String destinationSubDir, String pacsName) {
        if (queue.remove(new ProcessableFile(consumerId, filePath, destinationSubDir, pacsName))) {
            log.debug("File rimosso dalla coda. Path: " + filePath + " - Consumer: " + consumerId);
            return true;
        } else {
            log.debug("Rimozione fallita, file non presente in coda. Path: " + filePath + " - Consumer: " + consumerId);
            return false;
        }
    }

    public List<ProcessableFile> getProcessableFiles(String consumerId) {
        List<ProcessableFile> list = new ArrayList<ProcessableFile>();
        for (ProcessableFile pFile : queue) {
            if (pFile.filter(consumerId))
                list.add(pFile);
        }
        return list;
    }

    public int size() {
        return queue.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ProcessableFile pfile : queue) {
            sb.append("CONSUMER: ");
            sb.append(pfile.getConsumerName());
            sb.append("FILE: ");
            sb.append(pfile.getDestinationSubDir());
            sb.append("\n");
        }
        return sb.toString();
    }

}
