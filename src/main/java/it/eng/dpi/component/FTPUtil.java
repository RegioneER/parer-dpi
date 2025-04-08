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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTPUtil {
    private static final Logger log = LoggerFactory.getLogger(FTPUtil.class);

    /**
     *
     * @param fileToSend
     *            File da inviare
     * @param filename
     *            Nome da assegnare al file sul server FTP
     * @param path
     *            Percorso in cui copiare il file sul server FTP (il metodo provvede anche a creare le directory)
     * @param IP
     *            Indirizzo IP del server
     * @param port
     *            Porta del server
     * @param user
     *            Utente FTP
     * @param pwd
     *            Password utente
     * @param isSSL
     *            Connessione SSL/TSL
     *
     * @return Esito operazione
     */
    public static boolean sendFile(File fileToSend, String filename, String path, String IP, String port, String user,
            String pwd, boolean isSSL) {
        FTPClient ftp = createClient(isSSL);
        try {
            connectFTP(ftp, IP, port, user, pwd, isSSL);
            // String filename = globalHash + ".zip";
            // ftp.makeDirectory(ctx.getFtpInputFolder() + "/" + globalHash);
            ftp.makeDirectory(path);
            log.debug(ftp.getReplyString());
            // ftp.changeWorkingDirectory(ctx.getFtpInputFolder() + "/" +
            // globalHash);
            ftp.changeWorkingDirectory(path);
            log.debug(ftp.getReplyString());
            FTPFile[] file = ftp.listFiles(filename);
            log.debug(ftp.getReplyString());
            if (file != null && file.length > 0) {
                log.debug("File " + filename + " già presente nella cartella FTP");
                if (file[0].getSize() == fileToSend.length()) {
                    return true;
                } else {
                    log.debug("Elimino il file " + filename + " su FTP perchè di dimensione differente");
                    ftp.deleteFile(filename);
                    log.info(ftp.getReplyString());
                }
            } else {
                log.debug("File " + filename + " non presente lo copio");
            }
            log.debug(ftp.getReplyString());
            log.info("Invio il file " + filename + " ...");
            long startTime = System.currentTimeMillis();
            int copiedBytes = -1;
            try (InputStream is = new FileInputStream(fileToSend); OutputStream fout = ftp.storeFileStream(filename)) {
                copiedBytes = IOUtils.copy(is, fout);
            } catch (IOException e) {
                log.error("Errore durante il trasferimento del file " + filename, e);
                return false;
            }
            ftp.completePendingCommand();
            long endTime = System.currentTimeMillis();
            log.debug(ftp.getReplyString());
            ftp.logout();
            log.debug(ftp.getReplyString());
            log.info("Invio il file " + filename + " ... inviato");
            float trRate = ((float) copiedBytes / (endTime - startTime));
            log.info((copiedBytes / (1024 * 1024)) + " MB copiati in " + (float) (endTime - startTime) / 1000 + " s. "
                    + trRate / 1000 + " MB/s - ~" + ((long) (trRate * 8) / 1000) + " Mbit/s");
            return true;
        } catch (Exception ex) {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                    log.debug("Disconnected from server: " + IP);
                } catch (IOException e) {
                    // do nothing
                }
            }
            log.error("Couldn't connect to server: " + IP, ex);
            return false;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                    log.debug("Disconnected from server: " + IP);
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    /**
     *
     * @param filename
     *            Nome del file da recuperare
     * @param path
     *            Percorso sul server FTP in cui è presente il file
     * @param localStream
     *            OutputStream locale in cui copiare il file
     * @param IP
     *            Indirizzo IP del server
     * @param port
     *            Porta del server
     * @param user
     *            Utente FTP
     * @param pwd
     *            Password utente
     * @param isSSL
     *            Connessione SSL/TSL
     *
     * @return Esito operazione
     */
    public static boolean retrieveFile(String[] filename, OutputStream[] localStream, String path, String IP,
            String port, String user, String pwd, boolean isSSL) {
        if (filename.length != localStream.length)
            throw new IllegalArgumentException("Il numero di filename deve coincidere con il numero di localStream");
        FTPClient ftp = createClient(isSSL);
        try {
            connectFTP(ftp, IP, port, user, pwd, isSSL);
            // String filename = globalHash + ".zip";
            OutputStream fout = null;
            // ftp.changeWorkingDirectory(ctx.getFtpInputFolder() + "/" +
            // globalHash);
            ftp.changeWorkingDirectory(path);
            ftp.setBufferSize(100 * 1024);
            log.debug(ftp.getReplyString());

            for (int i = 0; i < filename.length; i++) {
                FTPFile[] file = ftp.listFiles(filename[i]);
                log.debug(ftp.getReplyString());
                if (file == null || file.length == 0) {
                    log.info("Il file " + filename[i] + " non è presente sul server nel percorso " + path);
                    return false;
                }
                log.info("Recupero il file " + filename[i]);
                boolean completed = ftp.retrieveFile(filename[i], localStream[i]);
                log.debug(ftp.getReplyString());
                if (!completed)
                    return false;
                log.info("Recuperato il file " + filename[i]);

            }
            ftp.logout();
            log.debug(ftp.getReplyString());
            return true;
        } catch (Exception ex) {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                    log.debug("Disconnected from server: " + IP);
                } catch (IOException e) {
                    // do nothing
                }
            }
            log.error("Couldn't connect to server: " + IP, ex);
            return false;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                    log.debug("Disconnected from server: " + IP);
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    private static FTPClient createClient(boolean isSSL) {
        FTPClient ftp;
        if (isSSL) {
            ftp = new FTPSClient();
        } else {
            ftp = new FTPClient();
        }
        ftp.setConnectTimeout(60 * 1000);
        ftp.setControlKeepAliveTimeout(10);
        return ftp;
    }

    private static void connectFTP(FTPClient ftp, String IP, String port, String user, String pwd, boolean isSSL)
            throws IOException {
        InetAddress addr = InetAddress.getByName(IP);
        int portnum = Integer.parseInt(port);
        ftp.connect(addr, portnum);
        ftp.setSoTimeout(900 * 1000);
        log.debug("Connected to " + addr);
        log.debug(ftp.getReplyString());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            log.error("FTP server refused connection.");
        } else {
            log.debug("Login user " + user);
            if (!ftp.login(user, pwd)) {
                log.info(ftp.getReplyString());
                ftp.logout();
            } else {
                log.debug(ftp.getReplyString());
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                log.debug("Binary mode on");
                log.debug(ftp.getReplyString());

                ftp.enterLocalPassiveMode();
                log.debug("Enter local passive mode.\n");
                if (isSSL) {
                    // Set protection buffer size
                    ((FTPSClient) ftp).execPBSZ(0);
                    // Set data channel protection to private
                    ((FTPSClient) ftp).execPROT("P");
                }
            }
        }
    }

}
