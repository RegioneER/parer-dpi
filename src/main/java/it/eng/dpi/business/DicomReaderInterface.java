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

package it.eng.dpi.business;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomElement;

import it.eng.dpi.exception.XAGenericException;
import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType;

public interface DicomReaderInterface {

    Map<String, DicomElement> readDicomValues(List<String> dcmHashDicomTag, List<File> dicomInstances)
            throws IOException;

    String generateDcmFile(Map<String, DicomElement> orderedMap, int numberOfImages);

    String calculateHash(String fileString) throws NoSuchAlgorithmException, UnsupportedEncodingException;

    String encodeFileString(String fileString) throws UnsupportedEncodingException;

    String generateGlobalFile(File dicomStudy, DatiSpecificiType datiSpecificiDICOM)
            throws XAGenericException, NoSuchAlgorithmException, IOException;

    String calculateHash(File file, boolean readAllFileInMemory) throws NoSuchAlgorithmException, IOException;

}
