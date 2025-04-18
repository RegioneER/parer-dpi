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

package it.eng.dpi.web.util;

import it.eng.util.SpringLiteTool;
import java.io.File;
import java.nio.charset.Charset;

public class ParerTool extends SpringLiteTool {

    private static final String GEN_PACKAGE = "it.eng.dpi.slite.gen";
    private static final String USER_PACKAGE = "it.eng.spagoLite.security";
    private static final String FORM_PACKAGE = GEN_PACKAGE + ".form";
    private static final String ACTION_PACKAGE = GEN_PACKAGE + ".action";
    private static final String ACTION_PATH = "./src/main/java/it/eng/dpi/web/action";

    public ParerTool(String actionPath, String actionRerPath, String genPackage, String actionPackage,
            String formPackage) {
        super(actionPath, actionRerPath, genPackage, actionPackage, formPackage);
    }

    public static void main(String[] args)
            throws IllegalArgumentException, SecurityException, NoSuchFieldException, IllegalAccessException {
        System.out.println(new File(".").getAbsolutePath());

        System.setProperty("file.encoding", "UTF-8");

        java.lang.reflect.Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);

        ParerTool myParerTool = new ParerTool(ACTION_PATH, null, GEN_PACKAGE, ACTION_PACKAGE, FORM_PACKAGE);
        myParerTool.setSrcPath("./target/generated-sources/slite");
        myParerTool.setFormPath("./src/main/resources/form");
        myParerTool.setJspPath("./src/main/webapp/jsp");
        myParerTool.setUserPackage(USER_PACKAGE);
        myParerTool.run();
    }

}
