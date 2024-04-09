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

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class Messages {

    @Value("${web.generalError.single}")
    private String singleGeneralError;

    @Value("${web.generalError.double}")
    private String doubleGeneralError;

    @Value("${web.generalError.fatal}")
    private String fatalError;

    @Value("${web.service.notAvail}")
    private String serviceUnavailable;

    @Value("${web.ricDiario.filtroPazNotAvail}")
    private String ricercaDiarioFiltroPazUnavailable;

    @Value("${web.service.ok}")
    private String serviceOk;

    @Value("${web.service.error}")
    private String serviceError;

    @Value("${web.ricDiario.noGlobalHashDir}")
    private String noGlobalHashDir;

    @Value("${web.ricDiario.noUdFile}")
    private String noUdFile;

    @Value("${web.ricDiario.noPcFile}")
    private String noPcFile;

    @Value("${web.ricDiario.noUdIndexFile}")
    private String noUdIndexFile;

    @Value("${web.ricDiario.noPcIndexFile}")
    private String noPcIndexFile;

    @Value("${web.ricDiario.ok}")
    private String ricDiarioOpOk;

    @Value("${web.invioAsync.noMotivChiusWarning}")
    private String noMotivChiusWarning;

    @Value("${web.invioAsync.noMotivVersWarning}")
    private String noMotivVersWarning;

    @Value("${web.nododicom.notAvail}")
    private String dicomNodeNotAvail;

    @Value("${web.nododicom.error}")
    private String dicomNodeError;

    @Value("${web.nododicom.ok}")
    private String dicomNodeOk;

    @Value("${web.nododicom.dirNotPresent}")
    private String pacsStudyDirNotPresent;

    @Value("${web.nododicom.zipNotPresent}")
    private String pacsStudyZipNotPresent;

    @Value("${web.nododicom.fileLengthError}")
    private String zipFileLengthError;

    @Value("${web.querypacs.dateError}")
    private String queryPacsDateError;

    @Value("${web.querypacs.alreadyExecuting}")
    private String queryPacsAlreadyExecuting;

    @Value("${web.levels.ok}")
    private String loggerLevelsOk;

    @Value("${web.chiusWarnMultiple.ok}")
    private String chiusWarnMultipleOk;

    @Value("${web.chiusWarn.ok}")
    private String chiusWarnOk;

    @Value("${web.chiusWarn.error}")
    private String chiusWarnError;

    @Value("${web.versWarnMultiple.ok}")
    private String versWarnMultipleOk;

    @Value("${web.versWarn.ok}")
    private String versWarnOk;

    @Value("${web.versWarn.error}")
    private String versWarnError;

    public String getGeneralError(String cod, String msg) {
        return MessageFormat.format(doubleGeneralError, cod, msg);
    }

    public String getGeneralError(String msg) {
        return MessageFormat.format(singleGeneralError, msg);
    }

    public String getFatalError() {
        return fatalError;
    }

    public String getServiceOk() {
        return serviceOk;
    }

    public String getServiceError(String cod, String msg) {
        return MessageFormat.format(serviceError, cod, msg);
    }

    public String getServiceUnavailable(String serviceName) {
        return MessageFormat.format(serviceUnavailable, serviceName);
    }

    public String getRicercaDiarioFiltroPazUnavailable() {
        return ricercaDiarioFiltroPazUnavailable;
    }

    public String getNoGlobalHashDir() {
        return noGlobalHashDir;
    }

    public String getNoUdFile() {
        return noUdFile;
    }

    public String getNoPcFile() {
        return noPcFile;
    }

    public String getNoUdIndexFile() {
        return noUdIndexFile;
    }

    public String getNoPcIndexFile() {
        return noPcIndexFile;
    }

    public String getRicDiarioOpOk() {
        return ricDiarioOpOk;
    }

    public String getNoMotivChiusWarning() {
        return noMotivChiusWarning;
    }

    public String getNoMotivVersWarning() {
        return noMotivVersWarning;
    }

    public String getDicomNodeNotAvail(String node) {
        return MessageFormat.format(dicomNodeNotAvail, node);
    }

    public String getDicomNodeError() {
        return dicomNodeError;
    }

    public String getPacsStudyDirNotPresent() {
        return pacsStudyDirNotPresent;
    }

    public String getPacsStudyZipNotPresent() {
        return pacsStudyZipNotPresent;
    }

    public String getZipFileLengthError() {
        return zipFileLengthError;
    }

    public String getDicomNodeOk(int numImages, int transferedImages) {
        return MessageFormat.format(dicomNodeOk, String.valueOf(numImages), String.valueOf(transferedImages));
    }

    public String getQueryPacsDateError(String queryPacsStartDate) {
        return MessageFormat.format(queryPacsDateError, queryPacsStartDate);
    }

    public String getQueryPacsAlreadyExecuting() {
        return queryPacsAlreadyExecuting;
    }

    public String getLoggerLevelsOk() {
        return loggerLevelsOk;
    }

    public String getChiusWarnMultipleOk(int numChius, int totChius) {
        return MessageFormat.format(chiusWarnMultipleOk, numChius, totChius);
    }

    public String getChiusWarnOk() {
        return chiusWarnOk;
    }

    public String getChiusWarnError(String codErr, String dsErr, int numChius, int totChius) {
        return MessageFormat.format(chiusWarnError, codErr, dsErr, numChius, totChius);
    }

    public String getVersWarnMultipleOk(int numVers, int totVers) {
        return MessageFormat.format(versWarnMultipleOk, numVers, totVers);
    }

    public String getVersWarnOk() {
        return versWarnOk;
    }

    public String getVersWarnError(String codErr, String dsErr, int numVers, int totVers) {
        return MessageFormat.format(versWarnError, codErr, dsErr, numVers, totVers);
    }
}
