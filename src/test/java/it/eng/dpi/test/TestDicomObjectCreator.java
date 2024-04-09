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

package it.eng.dpi.test;

import java.io.File;
import java.io.IOException;

import it.eng.dpi.business.impl.SacerPingObjectCreator;
import it.eng.dpi.service.DPIConfigInitializer;
import it.eng.sacerasi.ws.xml.datispecdicom.DatiSpecificiType;

import org.junit.Test;

public class TestDicomObjectCreator {

    DPIConfigInitializer dpiConfig = new DPIConfigInitializer();

    // @Test
    // public void testReadDicomValue() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGenerateDcmFile() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGenerateGlobalFile() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testCalculateHash() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testEncodeFileString() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testDeleteStudyFolder() {
    // fail("Not yet implemented");
    // }

    @Test
    public void testMakeSacerPINGArchive() throws IOException {
        SacerPingObjectCreator dicomObjectCreator = new SacerPingObjectCreator();
        dicomObjectCreator.setXaDiskNativeFS(dpiConfig.xaDiskNativeFS());
        File pacsRoot = new File("C:/dpi/StoreSCP/DCMSND/cstore");
        for (File f : pacsRoot.listFiles()) {
            dicomObjectCreator.makeSacerPINGArchive(f, "Ghash", "C:/dpi/new/cstore", new DatiSpecificiType());
            break;
        }

    }

}
