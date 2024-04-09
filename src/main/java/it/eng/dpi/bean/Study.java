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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class Study extends Patient {

    public Study() {
    }

    public Study(DicomObject dicom) {
        super(dicom);
        this.studyInstanceUID = getDicomValue(dicom, Tag.StudyInstanceUID);
        this.studyID = getDicomValue(dicom, Tag.StudyID);
        this.accessionNumber = getDicomValue(dicom, Tag.AccessionNumber);
        this.studyDateTime = dicom.getDate(Tag.StudyDate, Tag.StudyTime);
        this.numberOfStudyRelatedSeries = getDicomValue(dicom, Tag.NumberOfStudyRelatedSeries);
        this.numberOfStudyRelatedInstances = getDicomValue(dicom, Tag.NumberOfStudyRelatedInstances);
    }

    private Date studyDateTime;
    private String accessionNumber;
    private String studyID;
    private String studyInstanceUID;
    private String numberOfStudyRelatedSeries;
    private String numberOfStudyRelatedInstances;

    public Date getStudyDateTime() {
        return studyDateTime;
    }

    public void setStudyDateTime(Date studyDateTime) {
        this.studyDateTime = studyDateTime;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getNumberOfStudyRelatedSeries() {
        return numberOfStudyRelatedSeries;
    }

    public void setNumberOfStudyRelatedSeries(String numberOfStudyRelatedSeries) {
        this.numberOfStudyRelatedSeries = numberOfStudyRelatedSeries;
    }

    public String getNumberOfStudyRelatedInstances() {
        return numberOfStudyRelatedInstances;
    }

    public void setNumberOfStudyRelatedInstances(String numberOfStudyRelatedInstances) {
        this.numberOfStudyRelatedInstances = numberOfStudyRelatedInstances;
    }
}
