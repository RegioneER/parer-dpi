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

import java.math.BigDecimal;
import java.util.Date;

public class RicDiarioObjToRowBean {
    private String cdKeyObject;
    private Integer idSessione;
    private String tiStatoObject;
    private OggettoRicDiarioType oggRicDiario;
    private String blDcmHashTxt;
    private String blGlobalHashTxt;
    private String cdAetNodoDicom;
    private String cdEncodingDcmHash;
    private String cdEncodingFileHash;
    private String cdEncodingGlobalHash;
    private String cdPatientId;
    private String cdPatientIdIssuer;
    // private String chiaveUnitaDoc;
    // private String dlErr;
    private String dlListaModalityInStudy;
    private String dlListaSopClass;
    // private String dlMotivoChiusoWarning;
    // private String dlMotivoForzaAccettazione;
    private String dlStudyDescription;
    private String dsAccessionNumber;
    private String dsDcmHash;
    private String dsFileHash;
    private String dsGlobalHash;
    private String dsInstitutionName;
    private String dsPatientName;
    private String dsRefPhysicianName;
    private String dsStudyId;
    private String dsStudyInstanceUid;
    // private Date dtAperturaSessione;
    // private Date dtAperturaSessioneRecup;
    // private Date dtChiusuraSessione;
    private Date dtPatientBirthDate;
    private Date dtPresaInCarico;
    private Date dtStudyDate;
    // private String flForzaAccettazione;
    // private String flForzaWarning;
    // private BigDecimal idObject;
    private BigDecimal niStudyRelatedImages;
    private BigDecimal niStudyRelatedSeries;
    private String tiAlgoDcmHash;
    private String tiAlgoFileHash;
    private String tiAlgoGlobalHash;
    private String tiPatientSex;

    // private String tiStatoSessione;
    // private String tiStatoSessioneRecup;

    public RicDiarioObjToRowBean() {
        this.oggRicDiario = new OggettoRicDiarioType();
    }

    /**
     * @param ogg
     *            {@link OggettoRicDiarioType}
     */
    public RicDiarioObjToRowBean(OggettoRicDiarioType ogg) {
        this.oggRicDiario = ogg;
    }

    /**
     * @return the cdKeyObject
     */
    public String getCdKeyObject() {
        return cdKeyObject;
    }

    /**
     * @param cdKeyObject
     *            the cdKeyObject to set
     */
    public void setCdKeyObject(String cdKeyObject) {
        this.cdKeyObject = cdKeyObject;
    }

    /**
     * @return the idSessione
     */
    public Integer getIdSessione() {
        return idSessione;
    }

    /**
     * @param idSessione
     *            the idSessione to set
     */
    public void setIdSessione(Integer idSessione) {
        this.idSessione = idSessione;
    }

    /**
     * @return the tiStatoObject
     */
    public String getTiStatoObject() {
        return tiStatoObject;
    }

    /**
     * @param tiStatoObject
     *            the tiStatoObject to set
     */
    public void setTiStatoObject(String tiStatoObject) {
        this.tiStatoObject = tiStatoObject;
    }

    /**
     * @return the ogg
     */
    public OggettoRicDiarioType getOgg() {
        return oggRicDiario;
    }

    /**
     * @param ogg
     *            the ogg to set
     */
    public void setOgg(OggettoRicDiarioType ogg) {
        this.oggRicDiario = ogg;
    }

    /**
     * @return the blDcmHashTxt
     */
    public String getBlDcmHashTxt() {
        return blDcmHashTxt;
    }

    /**
     * @param blDcmHashTxt
     *            the blDcmHashTxt to set
     */
    public void setBlDcmHashTxt(String blDcmHashTxt) {
        this.blDcmHashTxt = blDcmHashTxt;
    }

    /**
     * @return the blGlobalHashTxt
     */
    public String getBlGlobalHashTxt() {
        return blGlobalHashTxt;
    }

    /**
     * @param blGlobalHashTxt
     *            the blGlobalHashTxt to set
     */
    public void setBlGlobalHashTxt(String blGlobalHashTxt) {
        this.blGlobalHashTxt = blGlobalHashTxt;
    }

    /**
     * @return the cdAetNodoDicom
     */
    public String getCdAetNodoDicom() {
        return cdAetNodoDicom;
    }

    /**
     * @param cdAetNodoDicom
     *            the cdAetNodoDicom to set
     */
    public void setCdAetNodoDicom(String cdAetNodoDicom) {
        this.cdAetNodoDicom = cdAetNodoDicom;
    }

    /**
     * @return the cdEncodingDcmHash
     */
    public String getCdEncodingDcmHash() {
        return cdEncodingDcmHash;
    }

    /**
     * @param cdEncodingDcmHash
     *            the cdEncodingDcmHash to set
     */
    public void setCdEncodingDcmHash(String cdEncodingDcmHash) {
        this.cdEncodingDcmHash = cdEncodingDcmHash;
    }

    /**
     * @return the cdEncodingFileHash
     */
    public String getCdEncodingFileHash() {
        return cdEncodingFileHash;
    }

    /**
     * @param cdEncodingFileHash
     *            the cdEncodingFileHash to set
     */
    public void setCdEncodingFileHash(String cdEncodingFileHash) {
        this.cdEncodingFileHash = cdEncodingFileHash;
    }

    /**
     * @return the cdEncodingGlobalHash
     */
    public String getCdEncodingGlobalHash() {
        return cdEncodingGlobalHash;
    }

    /**
     * @param cdEncodingGlobalHash
     *            the cdEncodingGlobalHash to set
     */
    public void setCdEncodingGlobalHash(String cdEncodingGlobalHash) {
        this.cdEncodingGlobalHash = cdEncodingGlobalHash;
    }

    /**
     * @return the cdPatientId
     */
    public String getCdPatientId() {
        return cdPatientId;
    }

    /**
     * @param cdPatientId
     *            the cdPatientId to set
     */
    public void setCdPatientId(String cdPatientId) {
        this.cdPatientId = cdPatientId;
    }

    /**
     * @return the cdPatientIdIssuer
     */
    public String getCdPatientIdIssuer() {
        return cdPatientIdIssuer;
    }

    /**
     * @param cdPatientIdIssuer
     *            the cdPatientIdIssuer to set
     */
    public void setCdPatientIdIssuer(String cdPatientIdIssuer) {
        this.cdPatientIdIssuer = cdPatientIdIssuer;
    }

    /**
     * @return the dlListaModalityInStudy
     */
    public String getDlListaModalityInStudy() {
        return dlListaModalityInStudy;
    }

    /**
     * @param dlListaModalityInStudy
     *            the dlListaModalityInStudy to set
     */
    public void setDlListaModalityInStudy(String dlListaModalityInStudy) {
        this.dlListaModalityInStudy = dlListaModalityInStudy;
    }

    /**
     * @return the dlListaSopClass
     */
    public String getDlListaSopClass() {
        return dlListaSopClass;
    }

    /**
     * @param dlListaSopClass
     *            the dlListaSopClass to set
     */
    public void setDlListaSopClass(String dlListaSopClass) {
        this.dlListaSopClass = dlListaSopClass;
    }

    /**
     * @return the dlStudyDescription
     */
    public String getDlStudyDescription() {
        return dlStudyDescription;
    }

    /**
     * @param dlStudyDescription
     *            the dlStudyDescription to set
     */
    public void setDlStudyDescription(String dlStudyDescription) {
        this.dlStudyDescription = dlStudyDescription;
    }

    /**
     * @return the dsAccessionNumber
     */
    public String getDsAccessionNumber() {
        return dsAccessionNumber;
    }

    /**
     * @param dsAccessionNumber
     *            the dsAccessionNumber to set
     */
    public void setDsAccessionNumber(String dsAccessionNumber) {
        this.dsAccessionNumber = dsAccessionNumber;
    }

    /**
     * @return the dsDcmHash
     */
    public String getDsDcmHash() {
        return dsDcmHash;
    }

    /**
     * @param dsDcmHash
     *            the dsDcmHash to set
     */
    public void setDsDcmHash(String dsDcmHash) {
        this.dsDcmHash = dsDcmHash;
    }

    /**
     * @return the dsFileHash
     */
    public String getDsFileHash() {
        return dsFileHash;
    }

    /**
     * @param dsFileHash
     *            the dsFileHash to set
     */
    public void setDsFileHash(String dsFileHash) {
        this.dsFileHash = dsFileHash;
    }

    /**
     * @return the dsGlobalHash
     */
    public String getDsGlobalHash() {
        return dsGlobalHash;
    }

    /**
     * @param dsGlobalHash
     *            the dsGlobalHash to set
     */
    public void setDsGlobalHash(String dsGlobalHash) {
        this.dsGlobalHash = dsGlobalHash;
    }

    /**
     * @return the dsInstitutionName
     */
    public String getDsInstitutionName() {
        return dsInstitutionName;
    }

    /**
     * @param dsInstitutionName
     *            the dsInstitutionName to set
     */
    public void setDsInstitutionName(String dsInstitutionName) {
        this.dsInstitutionName = dsInstitutionName;
    }

    /**
     * @return the dsPatientName
     */
    public String getDsPatientName() {
        return dsPatientName;
    }

    /**
     * @param dsPatientName
     *            the dsPatientName to set
     */
    public void setDsPatientName(String dsPatientName) {
        this.dsPatientName = dsPatientName;
    }

    /**
     * @return the dsRefPhysicianName
     */
    public String getDsRefPhysicianName() {
        return dsRefPhysicianName;
    }

    /**
     * @param dsRefPhysicianName
     *            the dsRefPhysicianName to set
     */
    public void setDsRefPhysicianName(String dsRefPhysicianName) {
        this.dsRefPhysicianName = dsRefPhysicianName;
    }

    /**
     * @return the dsStudyId
     */
    public String getDsStudyId() {
        return dsStudyId;
    }

    /**
     * @param dsStudyId
     *            the dsStudyId to set
     */
    public void setDsStudyId(String dsStudyId) {
        this.dsStudyId = dsStudyId;
    }

    /**
     * @return the dsStudyInstanceUid
     */
    public String getDsStudyInstanceUid() {
        return dsStudyInstanceUid;
    }

    /**
     * @param dsStudyInstanceUid
     *            the dsStudyInstanceUid to set
     */
    public void setDsStudyInstanceUid(String dsStudyInstanceUid) {
        this.dsStudyInstanceUid = dsStudyInstanceUid;
    }

    /**
     * @return the dtPatientBirthDate
     */
    public Date getDtPatientBirthDate() {
        return dtPatientBirthDate;
    }

    /**
     * @param dtPatientBirthDate
     *            the dtPatientBirthDate to set
     */
    public void setDtPatientBirthDate(Date dtPatientBirthDate) {
        this.dtPatientBirthDate = dtPatientBirthDate;
    }

    /**
     * @return the dtPresaInCarico
     */
    public Date getDtPresaInCarico() {
        return dtPresaInCarico;
    }

    /**
     * @param dtPresaInCarico
     *            the dtPresaInCarico to set
     */
    public void setDtPresaInCarico(Date dtPresaInCarico) {
        this.dtPresaInCarico = dtPresaInCarico;
    }

    /**
     * @return the dtStudyDate
     */
    public Date getDtStudyDate() {
        return dtStudyDate;
    }

    /**
     * @param dtStudyDate
     *            the dtStudyDate to set
     */
    public void setDtStudyDate(Date dtStudyDate) {
        this.dtStudyDate = dtStudyDate;
    }

    /**
     * @return the niStudyRelatedImages
     */
    public BigDecimal getNiStudyRelatedImages() {
        return niStudyRelatedImages;
    }

    /**
     * @param niStudyRelatedImages
     *            the niStudyRelatedImages to set
     */
    public void setNiStudyRelatedImages(BigDecimal niStudyRelatedImages) {
        this.niStudyRelatedImages = niStudyRelatedImages;
    }

    /**
     * @return the niStudyRelatedSeries
     */
    public BigDecimal getNiStudyRelatedSeries() {
        return niStudyRelatedSeries;
    }

    /**
     * @param niStudyRelatedSeries
     *            the niStudyRelatedSeries to set
     */
    public void setNiStudyRelatedSeries(BigDecimal niStudyRelatedSeries) {
        this.niStudyRelatedSeries = niStudyRelatedSeries;
    }

    /**
     * @return the tiAlgoDcmHash
     */
    public String getTiAlgoDcmHash() {
        return tiAlgoDcmHash;
    }

    /**
     * @param tiAlgoDcmHash
     *            the tiAlgoDcmHash to set
     */
    public void setTiAlgoDcmHash(String tiAlgoDcmHash) {
        this.tiAlgoDcmHash = tiAlgoDcmHash;
    }

    /**
     * @return the tiAlgoFileHash
     */
    public String getTiAlgoFileHash() {
        return tiAlgoFileHash;
    }

    /**
     * @param tiAlgoFileHash
     *            the tiAlgoFileHash to set
     */
    public void setTiAlgoFileHash(String tiAlgoFileHash) {
        this.tiAlgoFileHash = tiAlgoFileHash;
    }

    /**
     * @return the tiAlgoGlobalHash
     */
    public String getTiAlgoGlobalHash() {
        return tiAlgoGlobalHash;
    }

    /**
     * @param tiAlgoGlobalHash
     *            the tiAlgoGlobalHash to set
     */
    public void setTiAlgoGlobalHash(String tiAlgoGlobalHash) {
        this.tiAlgoGlobalHash = tiAlgoGlobalHash;
    }

    /**
     * @return the tiPatientSex
     */
    public String getTiPatientSex() {
        return tiPatientSex;
    }

    /**
     * @param tiPatientSex
     *            the tiPatientSex to set
     */
    public void setTiPatientSex(String tiPatientSex) {
        this.tiPatientSex = tiPatientSex;
    }
}
