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
import java.io.InputStream;
import java.io.OutputStream;

import org.xadisk.additional.XAFileInputStreamWrapper;
import org.xadisk.additional.XAFileOutputStreamWrapper;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileInputStream;
import org.xadisk.bridge.proxies.interfaces.XAFileOutputStream;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.DirectoryNotEmptyException;
import org.xadisk.filesystem.exceptions.FileAlreadyExistsException;
import org.xadisk.filesystem.exceptions.FileNotExistsException;
import org.xadisk.filesystem.exceptions.FileUnderUseException;
import org.xadisk.filesystem.exceptions.InsufficientPermissionOnFileException;
import org.xadisk.filesystem.exceptions.LockingFailedException;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.exception.XAGenericException;

public final class XAUtil {

    public static OutputStream createFileOS(Session session, File file, boolean createFile) throws XAGenericException {
        try {
            if (createFile) {
                session.createFile(file, false);
            }
            XAFileOutputStream xafos;
            xafos = session.createXAFileOutputStream(file, true);
            return new XAFileOutputStreamWrapper(xafos);
        } catch (FileNotExistsException | FileUnderUseException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException | InterruptedException
                | FileAlreadyExistsException e) {
            throw new XAGenericException(e);

        }
    }

    public static InputStream createFileIS(Session session, File file, boolean createFile) throws XAGenericException {
        try {
            if (createFile) {
                session.createFile(file, false);
            }
            XAFileInputStream xafis;
            xafis = session.createXAFileInputStream(file);
            return new XAFileInputStreamWrapper(xafis);
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException | InterruptedException e) {
            throw new XAGenericException(e);

        }
    }

    public static void createDirectory(Session session, File dir) throws XAGenericException {
        try {
            session.createFile(dir, true);
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException | InterruptedException e) {
            throw new XAGenericException(e);

        }
    }

    public static File[] listFiles(Session session, File root) throws XAGenericException {
        try {

            String[] filesName = session.listFiles(root);
            File[] files = new File[filesName.length];
            for (int i = 0; i < filesName.length; i++) {
                files[i] = new File(root, filesName[i]);
            }
            return files;
        } catch (FileNotExistsException | LockingFailedException | NoTransactionAssociatedException
                | InsufficientPermissionOnFileException | InterruptedException e) {
            throw new XAGenericException(e);
        }
    }

    public static File[] listFilesNewTX(XAFileSystem xaDiskNativeFS, File root) throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            String[] filesName = session.listFiles(root);

            File[] files = new File[filesName.length];
            for (int i = 0; i < filesName.length; i++) {
                files[i] = new File(root, filesName[i]);
            }
            return files;
        } catch (InterruptedException | FileNotExistsException | LockingFailedException
                | NoTransactionAssociatedException | InsufficientPermissionOnFileException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }
    }

    public static void moveFile(Session session, File file, File dest) throws XAGenericException {
        try {
            session.moveFile(file, dest);
        } catch (FileNotExistsException | LockingFailedException | NoTransactionAssociatedException
                | InsufficientPermissionOnFileException | InterruptedException | FileAlreadyExistsException
                | FileUnderUseException e) {
            throw new XAGenericException(e);
        }
    }

    public static void copyFile(Session session, File file, File dest) throws XAGenericException {
        try {
            session.copyFile(file, dest);
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException | InterruptedException e) {
            throw new XAGenericException(e);
        }
    }

    public static void deleteFile(Session session, File file) throws XAGenericException {

        try {
            session.deleteFile(file);
        } catch (DirectoryNotEmptyException | FileNotExistsException | FileUnderUseException
                | InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        }

    }

    public static void deleteFileNewTx(XAFileSystem xaDiskNativeFS, File file) throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            session.deleteFile(file);
        } catch (DirectoryNotEmptyException | FileNotExistsException | FileUnderUseException
                | InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }

    }

    public static void deleteFolderNewTx(XAFileSystem xaDiskNativeFS, File folder) throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            Util.rimuoviFileRicorsivamente(folder, session);
        } catch (DirectoryNotEmptyException | FileNotExistsException | FileUnderUseException
                | InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }

    }

    public static boolean fileExistsAndIsDirectory(Session session, File file) throws XAGenericException {
        try {
            return session.fileExistsAndIsDirectory(file);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        }

    }

    public static boolean fileExistsAndIsDirectoryLockExclusive(Session session, File file) throws XAGenericException {
        try {
            return session.fileExistsAndIsDirectory(file, true);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        }

    }

    public static boolean fileExistsAndIsDirectoryNewTx(XAFileSystem xaDiskNativeFS, File file)
            throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            return session.fileExistsAndIsDirectory(file);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }

    }

    public static boolean fileExists(Session session, File file) throws XAGenericException {
        try {
            return session.fileExists(file);
        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        }

    }

    public static boolean createDirIfNotExist(Session session, File dirToCreate) throws XAGenericException {
        if (!XAUtil.fileExistsAndIsDirectory(session, dirToCreate)) {
            XAUtil.createDirectory(session, dirToCreate);
            return true;
        } else {
            return false;
        }
    }

    public static boolean createDirIfNotExistNewTx(XAFileSystem xaDiskNativeFS, File dirToCreate)
            throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            if (!XAUtil.fileExistsAndIsDirectory(session, dirToCreate)) {
                session.createFile(dirToCreate, true);
                return true;
            } else {
                return false;
            }
        } catch (FileAlreadyExistsException | FileNotExistsException | InsufficientPermissionOnFileException
                | LockingFailedException | NoTransactionAssociatedException | InterruptedException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }
    }

    public static boolean fileExistsNewTx(XAFileSystem xaDiskNativeFS, File file) throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            return session.fileExists(file);

        } catch (InsufficientPermissionOnFileException | LockingFailedException | NoTransactionAssociatedException
                | InterruptedException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }

    }

    public static long getFileLength(Session session, File file) throws XAGenericException {
        try {
            return session.getFileLength(file);
        } catch (InterruptedException | FileNotExistsException | LockingFailedException
                | NoTransactionAssociatedException | InsufficientPermissionOnFileException e) {
            throw new XAGenericException(e);
        }

    }

    public static Long getFileLengthNewTX(XAFileSystem xaDiskNativeFS, File file) throws XAGenericException {
        Session session = null;
        try {
            session = xaDiskNativeFS.createSessionForLocalTransaction();
            return session.getFileLength(file);
        } catch (InterruptedException | FileNotExistsException | LockingFailedException
                | NoTransactionAssociatedException | InsufficientPermissionOnFileException e) {
            throw new XAGenericException(e);
        } finally {
            try {
                if (session != null)
                    session.commit();
            } catch (NoTransactionAssociatedException e) {
            }
        }
    }

}
