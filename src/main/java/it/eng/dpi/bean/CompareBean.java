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

public class CompareBean {

    private int numStudyPing;
    private int numStudyPacs;
    private String pacsAet;
    private int numStudyDaTrasferire;

    public int getNumStudyPing() {
        return numStudyPing;
    }

    public void setNumStudyPing(int numStudyPing) {
        this.numStudyPing = numStudyPing;
    }

    public int getNumStudyPacs() {
        return numStudyPacs;
    }

    public void setNumStudyPacs(int numStudyPacs) {
        this.numStudyPacs = numStudyPacs;
    }

    public String getPacsAet() {
        return pacsAet;
    }

    public void setPacsAet(String pacsAet) {
        this.pacsAet = pacsAet;
    }

    public int getNumStudyDaTrasferire() {
        return numStudyDaTrasferire;
    }

    public void setNumStudyDaTrasferire(int numStudyDaTrasferire) {
        this.numStudyDaTrasferire = numStudyDaTrasferire;
    }

}
