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

package it.eng.dpi.bean;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ObjectType {

    // Davide
    String objType;
    String inputPath;
    String copiatoPath;
    boolean flCreaZip;
    String tiCalcRegistroUd;
    String tiCalcAnnoUd;
    String tiCalcKeyUd;
    String tipoFile;
    String tiCalcProfiloUd;

    public ObjectType(String objType, String inputPath, String copiatoPath, boolean flCreaZip, String tiCalcRegistroUd,
            String tiCalcAnnoUd, String tiCalcKeyUd, String tipoFile, String tiCalcProfiloUd) {
        super();
        this.objType = objType;
        this.inputPath = inputPath;
        this.copiatoPath = copiatoPath;
        this.flCreaZip = flCreaZip;
        this.tiCalcRegistroUd = tiCalcRegistroUd;
        this.tiCalcAnnoUd = tiCalcAnnoUd;
        this.tiCalcKeyUd = tiCalcKeyUd;
        this.tipoFile = tipoFile;
        this.tiCalcProfiloUd = tiCalcProfiloUd;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getCopiatoPath() {
        return copiatoPath;
    }

    public void setCopiatoPath(String copiatoPath) {
        this.copiatoPath = copiatoPath;
    }

    public boolean getFlCreaZip() {
        return flCreaZip;
    }

    public void setFlCreaZip(boolean flCreaZip) {
        this.flCreaZip = flCreaZip;
    }

    public String getTiCalcRegistroUd() {
        return tiCalcRegistroUd;
    }

    public void setTiCalcRegistroUd(String tiCalcRegistroUd) {
        this.tiCalcRegistroUd = tiCalcRegistroUd;
    }

    public String getTiCalcAnnoUd() {
        return tiCalcAnnoUd;
    }

    public void setTiCalcAnnoUd(String tiCalcAnnoUd) {
        this.tiCalcAnnoUd = tiCalcAnnoUd;
    }

    public String getTiCalcKeyUd() {
        return tiCalcKeyUd;
    }

    public void setTiCalcKeyUd(String tiCalcKeyUd) {
        this.tiCalcKeyUd = tiCalcKeyUd;
    }

    public String getTipoFile() {
        return tipoFile;
    }

    public void setTipoFile(String tipoFile) {
        this.tipoFile = tipoFile;
    }

    public String getTiCalcProfiloUd() {
        return tiCalcProfiloUd;
    }

    public void setTiCalcProfiloUd(String tiCalcProfiloUd) {
        this.tiCalcProfiloUd = tiCalcProfiloUd;
    }

    @Override
    public String toString() {
        return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

}
