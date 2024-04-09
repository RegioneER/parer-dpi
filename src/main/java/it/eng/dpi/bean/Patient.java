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

import java.util.Date;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;

public class Patient {

    public Patient() {
    }

    public Patient(DicomObject dicom) {

        this.patientName = getDicomValue(dicom, Tag.PatientName);
        this.patientID = getDicomValue(dicom, Tag.PatientID);
        this.patientBirthDate = dicom.getDate(Tag.PatientBirthDate);
        this.patientSex = getDicomValue(dicom, Tag.PatientSex);
        this.numberOfPatientRelatedStudies = dicom.getInt(Tag.NumberOfPatientRelatedSeries);
        this.numberOfPatientRelatedSeries = dicom.getInt(Tag.NumberOfPatientRelatedSeries);
        this.numberOfPatientRelatedInstances = dicom.getInt(Tag.NumberOfPatientRelatedInstances);
    }

    private String patientName;
    private String patientID;
    private Date patientBirthDate;
    private String patientSex;
    private long numberOfPatientRelatedStudies;
    private long numberOfPatientRelatedSeries;
    private long numberOfPatientRelatedInstances;

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public long getNumberOfPatientRelatedStudies() {
        return numberOfPatientRelatedStudies;
    }

    public void setNumberOfPatientRelatedStudies(long numberOfPatientRelatedStudies) {
        this.numberOfPatientRelatedStudies = numberOfPatientRelatedStudies;
    }

    public long getNumberOfPatientRelatedSeries() {
        return numberOfPatientRelatedSeries;
    }

    public void setNumberOfPatientRelatedSeries(long numberOfPatientRelatedSeries) {
        this.numberOfPatientRelatedSeries = numberOfPatientRelatedSeries;
    }

    public long getNumberOfPatientRelatedInstances() {
        return numberOfPatientRelatedInstances;
    }

    public void setNumberOfPatientRelatedInstances(long numberOfPatientRelatedInstances) {
        this.numberOfPatientRelatedInstances = numberOfPatientRelatedInstances;
    }

    public static String getDicomValue(DicomObject dcmObj, int tag) {
        String value = null;
        DicomElement elem = dcmObj.get(tag);
        if (elem != null) {
            value = elem.getValueAsString(new SpecificCharacterSet("UTF-8"), Integer.MAX_VALUE);
        }
        return value;
    }

}
