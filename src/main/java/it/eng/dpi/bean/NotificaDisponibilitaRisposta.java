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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
public class NotificaDisponibilitaRisposta {
    protected String cdErr;
    protected EsitoServizio cdEsito;
    protected String globalHash;
    protected String dlErr;
    protected String nmAmbiente;
    protected String nmVersatore;

    public NotificaDisponibilitaRisposta() {

    }

    public NotificaDisponibilitaRisposta(String nmAmbiente, String nmVersatore, String globalHash) {
        this.nmAmbiente = nmAmbiente;
        this.nmVersatore = nmVersatore;
        this.globalHash = globalHash;
    }

    public String getCdErr() {
        return cdErr;
    }

    public void setCdErr(String cdErr) {
        this.cdErr = cdErr;
    }

    public EsitoServizio getCdEsito() {
        return cdEsito;
    }

    public void setCdEsito(EsitoServizio cdEsito) {
        this.cdEsito = cdEsito;
    }

    public String getGlobalHash() {
        return globalHash;
    }

    public void setGlobalHash(String globalHash) {
        this.globalHash = globalHash;
    }

    public String getDlErr() {
        return dlErr;
    }

    public void setDlErr(String dlErr) {
        this.dlErr = dlErr;
    }

    public String getNmAmbiente() {
        return nmAmbiente;
    }

    public void setNmAmbiente(String nmAmbiente) {
        this.nmAmbiente = nmAmbiente;
    }

    public String getNmVersatore() {
        return nmVersatore;
    }

    public void setNmVersatore(String nmVersatore) {
        this.nmVersatore = nmVersatore;
    }

    @XmlType(name = "esitoServizio")
    @XmlEnum
    public enum EsitoServizio {

        OK, KO;

        public String value() {
            return name();
        }

        public static EsitoServizio fromValue(String v) {
            return valueOf(v);
        }

    }
}
