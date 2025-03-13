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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.filesystem.exceptions.DirectoryNotEmptyException;
import org.xadisk.filesystem.exceptions.FileAlreadyExistsException;
import org.xadisk.filesystem.exceptions.FileNotExistsException;
import org.xadisk.filesystem.exceptions.FileUnderUseException;
import org.xadisk.filesystem.exceptions.InsufficientPermissionOnFileException;
import org.xadisk.filesystem.exceptions.LockingFailedException;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.DPIConstants;

public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static final String FILE_PREFIX = "tmp_dpi_";

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Metodo statico per ordinare un enum tramite il valore
     *
     * @param <T>
     *            generics enum
     * @param enumValues
     *            l'array di valori dell'enum
     *
     * @return la collezione ordinata
     */
    public static <T extends Enum<?>> Collection<T> sortEnum(T[] enumValues) {
        SortedMap<String, T> map = new TreeMap<String, T>();
        for (T l : enumValues) {
            map.put(l.name(), l);
        }
        return map.values();
    }

    /**
     * Metodo statico che verifica la presenza di un file all'interno di un archivio zip
     *
     * @param zipPathName
     *            il path del file zip
     * @param pathFileInZip
     *            il path del file all'interno dello zip
     *
     * @return true se il file è presente
     *
     * @throws IOException
     *             eccezione generica
     */
    public static boolean verificaPresenzaFileInZip(String zipPathName, String pathFileInZip) throws IOException {
        boolean tmpResult = false;
        ZipArchiveEntry tmpZipArchiveEntry;
        ZipFile tmpFile = null;
        try {
            tmpFile = new ZipFile(zipPathName);
            tmpZipArchiveEntry = tmpFile.getEntry(pathFileInZip);
            if (tmpZipArchiveEntry != null) {
                tmpResult = true;
            }
        } finally {
            ZipFile.closeQuietly(tmpFile);
        }
        return tmpResult;
    }

    /**
     * Metodo statico che verifica la presenza di una directory all'interno di un archivio zip
     *
     * @param zipPathName
     *            il path del file zip
     * @param pathDirInZip
     *            il path della directory all'interno dello zip
     *
     * @return true se la directory è presente
     *
     * @throws IOException
     *             eccezione generica
     */
    public static boolean verificaPresenzaDirInZip(String zipPathName, String pathDirInZip) throws IOException {
        boolean tmpResult = false;
        ZipFile tmpFile = null;
        try {
            if (!pathDirInZip.endsWith("/")) {
                pathDirInZip += '/';
            }
            tmpFile = new ZipFile(zipPathName);
            Enumeration<ZipArchiveEntry> entries = tmpFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry tmpZipArchiveEntry = entries.nextElement();
                String fileName = tmpZipArchiveEntry.getName();
                if (fileName.startsWith(pathDirInZip)) {
                    tmpResult = true;
                    break;
                }
            }
        } finally {
            ZipFile.closeQuietly(tmpFile);
        }
        return tmpResult;
    }

    /**
     * Metodo che ritorna il file richiesto come parametro contenuto all'interno di un archivio zip
     *
     * @param zipPathName
     *            il path del file zip
     * @param fileInZip
     *            il path del file all'interno dello zip
     * @param pathContenutoZip
     *            il path su cui scrivere il file
     *
     * @return il file salvato
     *
     * @throws IOException
     *             eccezione generica
     */
    public static File estraFileDaZip(String zipPathName, String fileInZip, File pathContenutoZip) throws IOException {
        File tmpRetFile = null;
        if (verificaPresenzaFileInZip(zipPathName, fileInZip)) {
            tmpRetFile = File.createTempFile("out_", ".tmp", pathContenutoZip);
            FileOutputStream tmpFileOutStream = new FileOutputStream(tmpRetFile);
            estraFileDaZip(zipPathName, fileInZip, tmpFileOutStream);
        } else {
            throw new FileNotFoundException();
        }
        return tmpRetFile;
    }

    /**
     * Metodo che ritorna il file richiesto come parametro contenuto all'interno di un archivio zip come byte array
     *
     * @param zipPathName
     *            il path del file zip
     * @param fileInZip
     *            il path del file all'interno dello zip
     *
     * @return il byte[]
     *
     * @throws IOException
     *             eccezione generica
     */
    public static byte[] estraiFileDaZip(String zipPathName, String fileInZip) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (verificaPresenzaFileInZip(zipPathName, fileInZip)) {
            estraFileDaZip(zipPathName, fileInZip, out);
        } else {
            throw new FileNotFoundException();
        }
        return out.toByteArray();
    }

    /**
     * Metodo che ritorna un outputStream contenente il file richiesto come parametro all'interno di un archivio zip
     *
     * @param zipPathName
     *            il path del file zip
     * @param fileInZip
     *            il path del file all'interno dello zip
     * @param out
     *            l'outputStream su cui scrivere
     *
     * @return out oggetto {@link OutputStream}
     *
     * @throws IOException
     *             eccezione generica
     */
    public static OutputStream estraFileDaZip(String zipPathName, String fileInZip, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[512 * 1024]; // 1/2 megabyte di buffer
        ZipArchiveEntry tmpZipArchiveEntry;
        ZipFile tmpZipFile = null;
        InputStream tmpInputStream = null;
        try {
            tmpZipFile = new ZipFile(zipPathName);
            tmpZipArchiveEntry = tmpZipFile.getEntry(fileInZip);
            if (tmpZipArchiveEntry != null) {
                tmpInputStream = tmpZipFile.getInputStream(tmpZipArchiveEntry);
                int numBytes;
                while ((numBytes = tmpInputStream.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, numBytes);
                }
            }
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(tmpInputStream);
            ZipFile.closeQuietly(tmpZipFile);
        }

        return out;
    }

    /**
     * Metodo statico che va a scrivere nella directory outFolder i file contenuti nella directory pathDirInZip in
     * transazione
     *
     * @param zipFile
     *            il file zip
     * @param pathDirInZip
     *            il path della directory da cui estrarre i file nello zip
     * @param outFolder
     *            la directory su cui scrivere i file
     * @param session
     *            la sessione in transazione
     * @param mkdirs
     *            true per ricreare lo stesso path del file nella directory di destinazione
     *
     * @return array di oggetti tipo {@link File}
     *
     * @throws XAGenericException
     *             eccezione generica
     * @throws IOException
     *             eccezione generica
     */
    public static File[] estraiFileDaZipTx(File zipFile, String pathDirInZip, String outFolder, Session session,
            boolean mkdirs) throws XAGenericException, IOException {

        byte[] buffer = new byte[512 * 1024]; // 1/2 megabyte di buffer
        ZipArchiveEntry tmpZipArchiveEntry = null;
        CloseShieldInputStream tmpInputStream = null;
        BufferedOutputStream out = null;
        File outFile = null;
        ZipArchiveInputStream is = new ZipArchiveInputStream(XAUtil.createFileIS(session, zipFile, false));
        List<File> fileList = new ArrayList<File>();
        boolean extractFromRoot = false;
        if (pathDirInZip.equals("/")) {
            extractFromRoot = true;
        } else if (!pathDirInZip.endsWith("/")) {
            pathDirInZip += '/';
        }
        try {
            while ((tmpZipArchiveEntry = is.getNextZipEntry()) != null) {
                String fileName = tmpZipArchiveEntry.getName();
                if (extractFromRoot || fileName.startsWith(pathDirInZip)) {
                    if (tmpZipArchiveEntry.isDirectory()) {
                        if (mkdirs) {
                            File dir = new File(outFolder, fileName);
                            XAUtil.createDirectory(session, dir);
                            fileList.add(dir);
                        } else {
                            continue;
                        }
                    } else {
                        try {
                            int index = fileName.lastIndexOf('/') + 1;
                            String tmpFileName = fileName.substring(index);
                            String tmpFilePath = fileName.substring(0, index);
                            if (mkdirs) {
                                if (!XAUtil.fileExistsAndIsDirectory(session, new File(outFolder, tmpFilePath))) {
                                    File dir = new File(outFolder, tmpFilePath);
                                    XAUtil.createDirectory(session, dir);
                                    fileList.add(dir);
                                }
                                outFile = new File(outFolder, fileName);
                            } else {
                                outFile = new File(outFolder, tmpFileName);
                            }
                            out = new BufferedOutputStream(XAUtil.createFileOS(session, outFile, true));
                            tmpInputStream = new CloseShieldInputStream(is);
                            int numBytes;
                            while ((numBytes = tmpInputStream.read(buffer, 0, buffer.length)) != -1) {
                                out.write(buffer, 0, numBytes);
                            }
                        } finally {
                            IOUtils.closeQuietly(out);
                            tmpInputStream.close();
                            fileList.add(outFile);
                        }
                    }
                }
            }
        } finally {
            is.close();
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
     * Metodo statico che rimuove ricorsivamente i dati contenuti in una directory in transazione
     *
     * @param dirPath
     *            la directory
     * @param xaSession
     *            la sessione in transazione
     *
     * @throws XAGenericException
     *             eccezione generica
     * @throws DirectoryNotEmptyException
     *             eccezione generica
     * @throws FileNotExistsException
     *             eccezione generica
     * @throws FileUnderUseException
     *             eccezione generica
     * @throws InsufficientPermissionOnFileException
     *             eccezione generica
     * @throws LockingFailedException
     *             eccezione generica
     * @throws NoTransactionAssociatedException
     *             eccezione generica
     */
    public static void rimuoviFileRicorsivamente(File dirPath, Session xaSession)
            throws XAGenericException, DirectoryNotEmptyException, FileNotExistsException, FileUnderUseException,
            InsufficientPermissionOnFileException, LockingFailedException, NoTransactionAssociatedException {
        File[] elencoFile = XAUtil.listFiles(xaSession, dirPath);
        if (elencoFile != null && elencoFile.length > 0) {
            for (File tmpFile : elencoFile) {
                if (XAUtil.fileExistsAndIsDirectory(xaSession, tmpFile)) {
                    rimuoviFileRicorsivamente(tmpFile, xaSession);
                } else {
                    XAUtil.deleteFile(xaSession, tmpFile);
                }
            }
            XAUtil.deleteFile(xaSession, dirPath);
        }
    }

    /**
     * Metodo statico che elimina un'intera gerarchia di directory da un file zip
     *
     * @param zipFile
     *            lo zip
     * @param dirPath
     *            la directory da rimuovere dallo zip
     * @param session
     *            la sessione in transazione
     *
     * @throws IOException
     *             eccezione generica
     * @throws XAGenericException
     *             eccezione generica
     * @throws InterruptedException
     *             eccezione generica
     * @throws NoTransactionAssociatedException
     *             eccezione generica
     * @throws LockingFailedException
     *             eccezione generica
     * @throws InsufficientPermissionOnFileException
     *             eccezione generica
     * @throws FileNotExistsException
     *             eccezione generica
     * @throws FileAlreadyExistsException
     *             eccezione generica
     */
    public static void deleteZipEntryDirTx(File zipFile, String dirPath, Session session)
            throws IOException, XAGenericException, FileAlreadyExistsException, FileNotExistsException,
            InsufficientPermissionOnFileException, LockingFailedException, NoTransactionAssociatedException,
            InterruptedException {
        // get a temp file
        File tempFile = new File(FileUtils.getTempDirectory(), FILE_PREFIX + UUID.randomUUID().toString() + ".zip");// File.createTempFile("dpi_tmp_",
                                                                                                                    // ".zip");

        ZipArchiveEntry tmpZipArchiveEntry = null;

        byte[] buf = new byte[1024];

        ZipArchiveInputStream zin = new ZipArchiveInputStream(XAUtil.createFileIS(session, zipFile, false));
        ZipOutputStream zout = new ZipOutputStream(XAUtil.createFileOS(session, tempFile, true));

        try {
            while ((tmpZipArchiveEntry = zin.getNextZipEntry()) != null) {
                String name = tmpZipArchiveEntry.getName();
                boolean toBeDeleted = false;

                if (name.startsWith(dirPath)) {
                    toBeDeleted = true;
                }

                if (!toBeDeleted) {
                    // Add ZIP entry to output stream.
                    zout.putNextEntry(new ZipEntry(name));
                    // Transfer bytes from the ZIP file to the output file
                    int len;
                    while ((len = zin.read(buf)) > 0) {
                        zout.write(buf, 0, len);
                    }
                }
            }
        } finally {
            // Close the streams
            zin.close();
            // Complete the ZIP file
            zout.close();
        }
        XAUtil.deleteFile(session, zipFile);
        XAUtil.moveFile(session, tempFile, zipFile);
    }

    /**
     * Get a diff between two dates
     *
     * @param date1
     *            the oldest date
     * @param date2
     *            the newest date
     * @param timeUnit
     *            the unit in which you want the diff
     *
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static String encodePassword(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(DPIConstants.HASH_ALGO);
            md.update(password.getBytes(StandardCharsets.UTF_8), 0, password.length());
        } catch (NoSuchAlgorithmException ex) {
            log.error("Algoritmo " + DPIConstants.HASH_ALGO + "non supportato");
            return StringUtils.EMPTY;
        }
        byte[] pwdHash = md.digest();
        return new String(Base64.encodeBase64(pwdHash));

    }

}
