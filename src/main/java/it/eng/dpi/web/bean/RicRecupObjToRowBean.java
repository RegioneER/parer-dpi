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

package it.eng.dpi.web.bean;

import it.eng.sacerasi.ws.OggettoRicDiarioType;
import it.eng.sacerasi.ws.OggettoRicRestOggType;

import java.util.Date;

public class RicRecupObjToRowBean {

    OggettoRicRestOggType ogg;
    String cdAetNodoDicom;
    Date dtStudyDate;
    String dsAccessionNumber;
    String dsPatientName;
    String tiPatientSex;
    Date dtPatientBirthDate;
    String cdPatientId;
    String cdPatientIdIssuer;
    String dsRefPhysicianName;
    String dlListaModalityInStudy;

    public RicRecupObjToRowBean() {
        this.ogg = new OggettoRicRestOggType();
    }

    /**
     * @param ogg
     *            {@link OggettoRicDiarioType}
     */
    public RicRecupObjToRowBean(OggettoRicRestOggType ogg) {
        this.ogg = ogg;
    }

    public OggettoRicRestOggType getOgg() {
        return ogg;
    }

    public void setOgg(OggettoRicRestOggType ogg) {
        this.ogg = ogg;
    }

    public String getCdAetNodoDicom() {
        return cdAetNodoDicom;
    }

    public void setCdAetNodoDicom(String cdAetNodoDicom) {
        this.cdAetNodoDicom = cdAetNodoDicom;
    }

    public Date getDtStudyDate() {
        return dtStudyDate;
    }

    public void setDtStudyDate(Date dtStudyDate) {
        this.dtStudyDate = dtStudyDate;
    }

    public String getDsAccessionNumber() {
        return dsAccessionNumber;
    }

    public void setDsAccessionNumber(String dsAccessionNumber) {
        this.dsAccessionNumber = dsAccessionNumber;
    }

    public String getDsPatientName() {
        return dsPatientName;
    }

    public void setDsPatientName(String dsPatientName) {
        this.dsPatientName = dsPatientName;
    }

    public String getTiPatientSex() {
        return tiPatientSex;
    }

    public void setTiPatientSex(String tiPatientSex) {
        this.tiPatientSex = tiPatientSex;
    }

    public Date getDtPatientBirthDate() {
        return dtPatientBirthDate;
    }

    public void setDtPatientBirthDate(Date dtPatientBirthDate) {
        this.dtPatientBirthDate = dtPatientBirthDate;
    }

    public String getCdPatientId() {
        return cdPatientId;
    }

    public void setCdPatientId(String cdPatientId) {
        this.cdPatientId = cdPatientId;
    }

    public String getCdPatientIdIssuer() {
        return cdPatientIdIssuer;
    }

    public void setCdPatientIdIssuer(String cdPatientIdIssuer) {
        this.cdPatientIdIssuer = cdPatientIdIssuer;
    }

    public String getDsRefPhysicianName() {
        return dsRefPhysicianName;
    }

    public void setDsRefPhysicianName(String dsRefPhysicianName) {
        this.dsRefPhysicianName = dsRefPhysicianName;
    }

    public String getDlListaModalityInStudy() {
        return dlListaModalityInStudy;
    }

    public void setDlListaModalityInStudy(String dlListaModalityInStudy) {
        this.dlListaModalityInStudy = dlListaModalityInStudy;
    }
}
