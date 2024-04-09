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

package it.eng.dpi.component;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class DPIContext {

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.allineasopclass}")
    private String wsdlAllineaSopClassUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.inviooggettoasincrono}")
    private String wsdlInvioOggettoUrl;

    @Value("${sacer_ping.ws.inviooggettoasincrono.flforzawarningcmove}")
    private String flForzaWarningCMove;

    @Value("${dpi.work_path}")
    private String workingPath;

    @Value("${dpi.web.num_records}")
    private String webNumRecords;

    @Value("${sacer_ping.ftp.secure}")
    private boolean secureFtp;

    @Value("${sacer_ping.ftp.ip}")
    private String ftpIP;

    @Value("${sacer_ping.ftp.port}")
    private String ftpPort;

    @Value("${sacer_ping.ftp.user}")
    private String ftpUser;

    @Value("${sacer_ping.ftp.password}")
    private String ftpPassword;

    @Value("${sacer_ping.ftp.input_folder}")
    private String ftpInputFolder;

    @Value("${sacer_ping.ftp.output_folder}")
    private String ftpOutputFolder;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.notificatrasferimentofile}")
    private String wsdlNotificaUrl;

    @Value("${sacer_ping.ws.timeout}")
    private String wsTimeout;

    @Value("${job.querypacs.daily.delay}")
    private String dailyDelay;

    @Value("${job.querypacs.daily.hourbefore}")
    private String dailyHourBefore;

    @Value("${job.querypacs.weekly.delay}")
    private String weeklyDelay;

    @Value("${job.querypacs.weekly.hourbefore}")
    private String weeklyHourBefore;

    @Value("${job.querypacs.monthly.delay}")
    private String monthlyDelay;

    @Value("${job.querypacs.monthly.hourbefore}")
    private String monthlyHourBefore;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.ricercadiario}")
    private String wsdlRicercaDiarioUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.richiestarestituzioneoggetto}")
    private String wsdlRichiestaRestituzioneOggettoUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.notificaprelievo}")
    private String wsdlNotificaPrelievoUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.richiestachiusurawarning}")
    private String wsdlRichiestaChiusuraWarningUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.ricercarestituzionioggetti}")
    private String wsdlRicercaRestituzioniOggettiUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.notificainattesaprelievo}")
    private String wsdlNotificaInAttesaPrelievoUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.ricercarecuperati}")
    private String wsdlRicercaRecuperatiUrl;

    @Value("${sacer_ping.ws.host}${sacer_ping.ws.pulizianotificato}")
    private String wsdlPuliziaNotificatoUrl;

    @Value("${sacer_iam.ws.host}${sacer_iam.ws.recupautoruser}")
    private String wsdlRecuperoAutorizzazionioUrl;

    @Value("${sacer_iam.ws.host}${sacer_iam.ws.recupnews}")
    private String wsdlRestituzioneNewsApplicazioneUrl;

    @Value("${sacer_iam.ws.host}${sacer_iam.modificapassword}")
    private String modificaPasswordUrl;

    @Value("${sacer_iam.ws.user}")
    private String userIam;

    @Value("${sacer_iam.ws.password}")
    private String passIam;

    @Value("${sacer_iam.ws.nmapplic}")
    private String nmApplic;

    @Value("${sacer_ping.ws.ambiente}")
    private String nmAmbiente;

    @Value("${sacer_ping.ws.versatore}")
    private String nmVersatore;

    @Value("${sacer_ping.ws.user}")
    private String userPing;

    @Value("${sacer_ping.ws.password}")
    private String passPing;

    @Value("${job.querypacs.startdate}")
    private String queryPacsStartDate;

    @Value("${job.querypacs.daily.enable}")
    private boolean dailyEnable;

    @Value("${job.querypacs.weekly.enable}")
    private boolean weeklyEnable;

    @Value("${job.querypacs.monthly.enable}")
    private boolean monthlyEnable;

    @Value("${dpi.user.admin}")
    private String adminUser;

    @Value("${dpi.user.pwd}")
    private String adminPwd;

    @Value("${dpi.user.nome}")
    private String adminName;

    @Value("${dpi.user.cognome}")
    private String adminSurname;

    @Value("${dpi.user.attivo}")
    private boolean adminAttivo;

    @Value("${dpi.querypacs.daybeforenow}")
    private int dayBeforeNow;

    @Value("${dpi.querypacs.daylimit}")
    private int dayLimit;

    // Davide
    @Value("${dpi.studiodicom_path}")
    private String studioDicomPath;

    public String getWsdlInvioOggettoUrl() {
        return wsdlInvioOggettoUrl;
    }

    public String getFlForzaWarningCMove() {
        return flForzaWarningCMove;
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public String getStudioDicomPath() {
        return studioDicomPath;
    }

    public Boolean getSecureFtp() {
        return secureFtp;
    }

    public String getFtpIP() {
        return ftpIP;
    }

    public String getFtpPort() {
        return ftpPort;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public String getFtpInputFolder() {
        return ftpInputFolder;
    }

    public String getWsdlNotificaUrl() {
        return wsdlNotificaUrl;
    }

    public String getWsTimeout() {
        return wsTimeout;
    }

    public String getDailyDelay() {
        return dailyDelay;
    }

    public String getDailyHourBefore() {
        return dailyHourBefore;
    }

    public String getWeeklyDelay() {
        return weeklyDelay;
    }

    public String getWeeklyHourBefore() {
        return weeklyHourBefore;
    }

    public String getMonthlyDelay() {
        return monthlyDelay;
    }

    public String getMonthlyHourBefore() {
        return monthlyHourBefore;
    }

    public String getWsdlRicercaDiarioUrl() {
        return wsdlRicercaDiarioUrl;
    }

    public String getNmAmbiente() {
        return nmAmbiente;
    }

    public String getNmVersatore() {
        return nmVersatore;
    }

    public boolean isDailyEnable() {
        return dailyEnable;
    }

    public boolean isWeeklyEnable() {
        return weeklyEnable;
    }

    public boolean isMonthlyEnable() {
        return monthlyEnable;
    }

    public String getWsdlAllineaSopClassUrl() {
        return wsdlAllineaSopClassUrl;
    }

    public String getWsdlRichiestaRestituzioneOggettoUrl() {
        return wsdlRichiestaRestituzioneOggettoUrl;
    }

    public String getWsdlNotificaPrelievoUrl() {
        return wsdlNotificaPrelievoUrl;
    }

    public String getWsdlRichiestaChiusuraWarningUrl() {
        return wsdlRichiestaChiusuraWarningUrl;
    }

    public String getWsdlRicercaRestituzioniOggettiUrl() {
        return wsdlRicercaRestituzioniOggettiUrl;
    }

    public String getWsdlNotificaInAttesaPrelievoUrl() {
        return wsdlNotificaInAttesaPrelievoUrl;
    }

    public String getWsdlRicercaRecuperatiUrl() {
        return wsdlRicercaRecuperatiUrl;
    }

    public String getWsdlPuliziaNotificatoUrl() {
        return wsdlPuliziaNotificatoUrl;
    }

    public String getWebNumRecords() {
        return webNumRecords;
    }

    public String getFtpOutputFolder() {
        return ftpOutputFolder;
    }

    public void setFtpOutputFolder(String ftpOutputFolder) {
        this.ftpOutputFolder = ftpOutputFolder;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPwd() {
        return adminPwd;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getAdminSurname() {
        return adminSurname;
    }

    public boolean isAdminAttivo() {
        return adminAttivo;
    }

    public int getDayBeforeNow() {
        return dayBeforeNow;
    }

    public int getDayLimit() {
        return dayLimit;
    }

    @Override
    public String toString() {
        return (new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)).toString().replace("\n", "<br />");
    }

    public String getQueryPacsStartDate() {
        return queryPacsStartDate;
    }

    public String getWsdlRecuperoAutorizzazionioUrl() {
        return wsdlRecuperoAutorizzazionioUrl;
    }

    public String getWsdlRestituzioneNewsApplicazioneUrl() {
        return wsdlRestituzioneNewsApplicazioneUrl;
    }

    public String getUserIam() {
        return userIam;
    }

    public String getPassIam() {
        return passIam;
    }

    public String getUserPing() {
        return userPing;
    }

    public String getPassPing() {
        return passPing;
    }

    public String getNmApplic() {
        return nmApplic;
    }

    public String getModificaPasswordUrl() {
        return modificaPasswordUrl;
    }

    public void setModificaPasswordUrl(String modificaPasswordUrl) {
        this.modificaPasswordUrl = modificaPasswordUrl;
    }

}
