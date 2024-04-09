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

import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

import it.eng.dpi.dicom.scu.QRConstants.QueryRetrieveLevel;

public class QueryBean {

    private final QueryRetrieveLevel qrlevel;
    private DicomObject keys;
    private List<String> privateFind = new ArrayList<String>();

    private int cancelAfter = 3;
    private int completed;
    private int warning;
    private int failed;

    private int totalCompleted;
    private int totalWarning;
    private int totalFailed;

    public QueryBean(QueryRetrieveLevel qrlevel) {
        this.keys = new BasicDicomObject();
        this.qrlevel = qrlevel;
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, qrlevel.getCode());
        // setto i parametri di ritorno di default per il livello scelto
        // if(setDefaultRetKeys)
        setDefReturnKeys();
    }

    public DicomObject getKeys() {
        return keys;
    }

    public QueryRetrieveLevel getQueryLevel() {
        return qrlevel;
    }

    public List<String> getPrivateFind() {
        return privateFind;
    }

    public void setPrivateFind(List<String> privateFind) {
        this.privateFind = privateFind;
    }

    public int getCancelAfter() {
        return cancelAfter;
    }

    public void setCancelAfter(int cancelAfter) {
        this.cancelAfter = cancelAfter;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getWarning() {
        return warning;
    }

    public void setWarning(int warning) {
        this.warning = warning;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public void addMatchingKey(int[] tagPath, String value) {
        keys.putString(tagPath, null, value);
    }

    public void addMatchingKeys(String[] matchingKeys) {
        for (int i = 1; i < matchingKeys.length; i++) {
            int[] tagPath = Tag.toTagPath(matchingKeys[(i - 1)]);
            String value = matchingKeys[i];
            this.keys.putString(tagPath, null, value);
            i++;
        }
    }

    public void addReturnKey(int[] patientReturnKeys) {
        for (Integer i : patientReturnKeys)
            keys.putNull(i, null);
    }

    public void addReturnKey(String[] returnKeys) {
        for (int i = 1; i < returnKeys.length; i++) {
            int[] tagPath = Tag.toTagPath(returnKeys[i]);
            this.keys.putNull(tagPath, null);

        }
    }

    // public void addDefReturnKeys() {
    // for (int tag : qrlevel.getReturnKeys())
    // keys.putNull(tag, null);
    // }

    private void setDefReturnKeys() {
        for (int tag : this.getQueryLevel().getReturnKeys())
            keys.putNull(tag, null);
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(int totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public int getTotalWarning() {
        return totalWarning;
    }

    public void setTotalWarning(int totalWarning) {
        this.totalWarning = totalWarning;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

}
