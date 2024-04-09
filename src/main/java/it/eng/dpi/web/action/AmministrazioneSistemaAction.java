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

package it.eng.dpi.web.action;

import it.eng.dpi.component.Messages;
import it.eng.dpi.slite.gen.Application;
import it.eng.dpi.slite.gen.action.AmministrazioneSistemaAbstractAction;
import it.eng.dpi.slite.gen.form.AmministrazioneSistemaForm;
import it.eng.dpi.web.util.Constants;
import it.eng.dpi.web.util.Constants.LoggerElements;
import it.eng.spagoCore.error.EMFError;
import it.eng.spagoLite.form.fields.Field;
import it.eng.spagoLite.form.fields.Fields;
import it.eng.spagoLite.form.fields.impl.Button;
import it.eng.spagoLite.form.fields.impl.CheckBox;
import it.eng.spagoLite.form.fields.impl.Input;
import it.eng.spagoLite.message.MessageBox.ViewMode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import ch.qos.logback.classic.Level;

public class AmministrazioneSistemaAction extends AmministrazioneSistemaAbstractAction {
    private static final Logger log = LoggerFactory.getLogger(AmministrazioneSistemaAction.class);

    @Autowired
    private SchedulerFactoryBean scheduler;

    @Autowired
    private Messages messagesCtx;

    private static final Map<String, JobKey> jobsMap = new HashMap<String, JobKey>();

    private static int pausedJobs = 0;

    @Override
    public void initOnClick() throws EMFError {
    }

    public void loadGestioneJob() throws EMFError {
        getUser().getMenu().reset();
        getUser().getMenu().select("Menu.AmministrazioneSistema.GestioneJob");

        pausedJobs = 0;

        try {
            refreshAllJobs();
        } catch (SchedulerException e) {
            log.error("Errore al caricamento dello scheduler dei job", e);
            getMessageBox().addError(messagesCtx.getGeneralError("Impossibile caricare lo schedulatore dei job"));
        }
        getForm().getQueryPacsPingQJobD().setEditMode();
        getForm().getQueryPacsPingQJobW().setEditMode();
        getForm().getQueryPacsPingQJobM().setEditMode();
        getForm().getObjectCreatorQJob1().setEditMode();
        getForm().getObjectCreatorQJob2().setEditMode();
        getForm().getObjectCreatorQJob3().setEditMode();
        getForm().getObjectCreatorCoordinatorQJob().setEditMode();
        getForm().getObjectSenderQJob().setEditMode();
        getForm().getNotificaPrelievoQJob().setEditMode();
        getForm().getNotificaInAttesaPrelievoQJob().setEditMode();
        getForm().getPrelievoFTPQJob().setEditMode();
        getForm().getFTPTransfertQJob().setEditMode();
        getForm().getUpdateSOPClassQJob().setEditMode();
        getForm().getPuliziaInAttesaFileQJob().setEditMode();
        getForm().getJobButtonList().setEditMode();
        getForm().getObjectCopyQJob().setEditMode();
        getForm().getGenericObjectCreatorQJob().setEditMode();
        forwardToPublisher(Application.Publisher.GESTIONE_JOB);
    }

    @SuppressWarnings("unchecked")
    private void refreshAllJobs() throws SchedulerException {
        // getForm().getJobButtonList().getStartAllJobs().setHidden(true);
        getForm().getJobButtonList().getStopAllJobs().setHidden(true);
        Scheduler sched = scheduler.getScheduler();
        for (JobKey key : sched.getJobKeys(GroupMatcher.jobGroupEquals(JobKey.DEFAULT_GROUP))) {
            getJobsMap().put(key.getName(), key);
            refreshJob(sched, key);
        }
        for (JobExecutionContext jobCtx : sched.getCurrentlyExecutingJobs()) {
            if (jobCtx.getJobDetail().getKey().getGroup().equals(JobKey.DEFAULT_GROUP)) {
                CheckBox<String> fl_attivo = (CheckBox<String>) ((Fields<Field>) getForm()
                        .getComponent(jobCtx.getJobDetail().getKey().getName()))
                                .getComponent(AmministrazioneSistemaForm.ObjectCreatorQJob1.attivo);
                fl_attivo.setChecked(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshJob(Scheduler sched, JobKey key) throws SchedulerException {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ssXXX");
        if (!sched.getTriggersOfJob(key).isEmpty()) {
            Trigger trigger = sched.getTriggersOfJob(key).get(0);
            Date lastFire = trigger.getPreviousFireTime();
            Date nextFire = trigger.getNextFireTime();
            // Recupero i componenti della pagina in base al nome del
            // job che sto gestendo
            Button<String> startButton = (Button<String>) ((Fields<Field>) getForm().getComponent(key.getName()))
                    .getComponent("Start" + key.getName());
            Button<String> stopButton = (Button<String>) ((Fields<Field>) getForm().getComponent(key.getName()))
                    .getComponent("Stop" + key.getName());
            Input<Timestamp> dt_last = (Input<Timestamp>) ((Fields<Field>) getForm().getComponent(key.getName()))
                    .getComponent(AmministrazioneSistemaForm.ObjectCreatorQJob1.dt_reg_log_job_ini);
            if (lastFire != null) {
                dt_last.setValue(df.format(lastFire));
            }
            Input<Timestamp> dt_next = (Input<Timestamp>) ((Fields<Field>) getForm().getComponent(key.getName()))
                    .getComponent(AmministrazioneSistemaForm.ObjectCreatorQJob1.dt_prossima_attivazione);
            if (nextFire != null && !sched.getTriggerState(trigger.getKey()).equals(Trigger.TriggerState.PAUSED)) {
                getForm().getJobButtonList().getStopAllJobs().setHidden(false);
                dt_next.setValue(df.format(nextFire));
                startButton.setHidden(true);
                stopButton.setHidden(false);
            } else {
                // getForm().getJobButtonList().getStartAllJobs().setHidden(false);
                dt_next.setValue(null);
                startButton.setHidden(false);
                stopButton.setHidden(true);
                incPausedJobs();
            }

            // if (getPausedJobs() == 0) {
            // getForm().getJobButtonList().getStartAllJobs().setHidden(true);
            // } else
            if (getPausedJobs() == getJobsMap().size()) {
                getForm().getJobButtonList().getStopAllJobs().setHidden(true);
            }
        }
    }

    public Map<String, JobKey> getJobsMap() {
        return jobsMap;
    }

    public void incPausedJobs() {
        pausedJobs++;
    }

    public void decPausedJobs() {
        pausedJobs--;
    }

    public int getPausedJobs() {
        return pausedJobs;
    }

    @Override
    public void insertDettaglio() throws EMFError {
    }

    @Override
    public void loadDettaglio() throws EMFError {
    }

    @Override
    public void undoDettaglio() throws EMFError {
    }

    @Override
    public void saveDettaglio() throws EMFError {
    }

    @Override
    public void dettaglioOnClick() throws EMFError {
    }

    @Override
    public void elencoOnClick() throws EMFError {
    }

    @Override
    protected String getDefaultPublsherName() {
        return Application.Publisher.GESTIONE_JOB;
    }

    @Override
    public void process() throws EMFError {
    }

    @Override
    public void reloadAfterGoBack(String publisherName) {
    }

    @Override
    public String getControllerName() {
        return Application.Actions.AMMINISTRAZIONE_SISTEMA;
    }

    private void startJob(String jobName) {
        try {
            decPausedJobs();
            Scheduler sched = scheduler.getScheduler();
            JobKey job = jobsMap.get(jobName);
            TriggerKey triggerKey = sched.getTriggersOfJob(job).get(0).getKey();
            sched.resumeTrigger(triggerKey);
            refreshJob(sched, job);
        } catch (SchedulerException e) {
            log.error("Errore al caricamento dei trigger dello scheduler dei job in fase di avvio del job " + jobName,
                    e);
            getMessageBox()
                    .addError(messagesCtx.getGeneralError("Impossibile caricare i trigger dello schedulatore dei job"));
        }
        forwardToPublisher(getDefaultPublsherName());
    }

    private void stopJob(String jobName) {
        try {
            Scheduler sched = scheduler.getScheduler();
            JobKey job = getJobsMap().get(jobName);
            TriggerKey triggerKey = sched.getTriggersOfJob(job).get(0).getKey();
            sched.pauseTrigger(triggerKey);
            refreshJob(sched, job);
        } catch (SchedulerException e) {
            log.error("Errore al caricamento dei trigger dello scheduler dei job in fase di stop del job " + jobName,
                    e);
            getMessageBox()
                    .addError(messagesCtx.getGeneralError("Impossibile caricare i trigger dello schedulatore dei job"));
        }
        forwardToPublisher(getDefaultPublsherName());
    }

    @Override
    public void startObjectSenderQJob() throws EMFError {
        startJob(Constants.ObjectSenderQJob);
    }

    @Override
    public void stopObjectSenderQJob() throws EMFError {
        stopJob(Constants.ObjectSenderQJob);
    }

    @Override
    public void startFTPTransfertQJob() throws EMFError {
        startJob(Constants.FTPTransfertQJob);
    }

    @Override
    public void stopFTPTransfertQJob() throws EMFError {
        stopJob(Constants.FTPTransfertQJob);
    }

    @Override
    public void startUpdateSOPClassQJob() throws EMFError {
        startJob(Constants.UpdateSOPClassQJob);
    }

    @Override
    public void stopUpdateSOPClassQJob() throws EMFError {
        stopJob(Constants.UpdateSOPClassQJob);
    }

    public void confermaAttivaConfrontoJob() {
        String nmJob = getRequest().getParameter("name");
        if (StringUtils.isNotBlank(nmJob)) {
            startJob(nmJob);
        } else {
            getMessageBox().addError("Errore nell'attivazione del job di confronto");
            forwardToPublisher(getDefaultPublsherName());
        }
    }

    @Override
    public void startQueryPacsPingQJobD() throws EMFError {
        getRequest().setAttribute("confermaAttivazioneConfrontoBox", true);
        getRequest().setAttribute("nmConfrontoJob", Constants.QueryPacsPingQJobD);
        getRequest().setAttribute("dsConfrontoJob", getForm().getQueryPacsPingQJobD().getDescription());
        forwardToPublisher(getDefaultPublsherName());
    }

    @Override
    public void stopQueryPacsPingQJobD() throws EMFError {
        stopJob(Constants.QueryPacsPingQJobD);
    }

    @Override
    public void startQueryPacsPingQJobW() throws EMFError {
        getRequest().setAttribute("confermaAttivazioneConfrontoBox", true);
        getRequest().setAttribute("nmConfrontoJob", Constants.QueryPacsPingQJobW);
        getRequest().setAttribute("dsConfrontoJob", getForm().getQueryPacsPingQJobW().getDescription());
        forwardToPublisher(getDefaultPublsherName());
    }

    @Override
    public void stopQueryPacsPingQJobW() throws EMFError {
        stopJob(Constants.QueryPacsPingQJobW);
    }

    @Override
    public void startQueryPacsPingQJobM() throws EMFError {
        getRequest().setAttribute("confermaAttivazioneConfrontoBox", true);
        getRequest().setAttribute("nmConfrontoJob", Constants.QueryPacsPingQJobM);
        getRequest().setAttribute("dsConfrontoJob", getForm().getQueryPacsPingQJobM().getDescription());
        forwardToPublisher(getDefaultPublsherName());
    }

    @Override
    public void stopQueryPacsPingQJobM() throws EMFError {
        stopJob(Constants.QueryPacsPingQJobM);
    }

    @Override
    public void startPrelievoFTPQJob() throws EMFError {
        startJob(Constants.PrelievoFTPQJob);
    }

    @Override
    public void stopPrelievoFTPQJob() throws EMFError {
        stopJob(Constants.PrelievoFTPQJob);
    }

    @Override
    public void startNotificaPrelievoQJob() throws EMFError {
        startJob(Constants.NotificaPrelievoQJob);
    }

    @Override
    public void stopNotificaPrelievoQJob() throws EMFError {
        stopJob(Constants.NotificaPrelievoQJob);
    }

    @Override
    public void startNotificaInAttesaPrelievoQJob() throws EMFError {
        startJob(Constants.NotificaInAttesaPrelievoQJob);
    }

    @Override
    public void stopNotificaInAttesaPrelievoQJob() throws EMFError {
        stopJob(Constants.NotificaInAttesaPrelievoQJob);
    }

    // @Override
    // public void startAllJobs() throws EMFError {
    // try {
    // pausedJobs = 0;
    // Scheduler sched = scheduler.getScheduler();
    // sched.resumeJobs(GroupMatcher.jobGroupEquals(JobKey.DEFAULT_GROUP));
    // refreshAllJobs();
    // } catch (SchedulerException e) {
    // log.error("Errore al caricamento dei trigger dello scheduler dei job in fase di avvio di tutti i job",
    // e);
    // getMessageBox().addError(messagesCtx.getGeneralError("Impossibile eseguire il riavvio dei job"));
    // }
    // forwardToPublisher(getDefaultPublsherName());
    // }

    @Override
    public void stopAllJobs() throws EMFError {
        try {
            pausedJobs = 0;
            Scheduler sched = scheduler.getScheduler();
            for (TriggerKey key : sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(TriggerKey.DEFAULT_GROUP))) {
                if (!sched.getTriggerState(key).equals(Trigger.TriggerState.PAUSED)) {
                    sched.pauseTrigger(key);
                }
            }
            // sched.pauseTriggers(null);
            refreshAllJobs();
        } catch (SchedulerException e) {
            log.error("Errore al caricamento dei trigger dello scheduler dei job in fase di stop di tutti i job", e);
            getMessageBox().addError(messagesCtx.getGeneralError("Impossibile eseguire lo stop dei job"));
        }
        forwardToPublisher(getDefaultPublsherName());
    }

    /**
     * Carica la lista dei livelli di log
     * 
     * @throws EMFError
     *             eccezione generica
     */
    public void loadLoggerLevels() throws EMFError {
        getUser().getMenu().reset();
        getUser().getMenu().select("Menu.AmministrazioneSistema.LivelliLogger");

        // Generate a list of all the loggers and levels
        HashSet<String> loggers = new HashSet<String>();
        ArrayList<String> levels = new ArrayList<String>();
        HashMap<String, Object> loggersHM = new HashMap<String, Object>();

        // GetRootLogger
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        String rootLoggerName = rootLogger.getName();
        loggers.add(rootLoggerName);
        loggersHM.put(rootLoggerName, rootLogger);

        for (LoggerElements elem : Constants.LoggerElements.values()) {
            String name = elem.getName();
            if (loggers.add(name)) {
                loggersHM.put(name, LoggerFactory.getLogger(name));
            }
        }
        // LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // for (ch.qos.logback.classic.Logger log : lc.getLoggerList()) {
        // if (loggers.add(log.getName())) {
        // loggersHM.put(log.getName(), log);
        // }
        // }

        String[] arrayNomi = loggers.toArray(new String[0]);
        Arrays.sort(arrayNomi, new LengthComparator());

        for (String nome : arrayNomi) {
            ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) loggersHM.get(nome);
            levels.add(log.getEffectiveLevel().toString());
        }

        getRequest().setAttribute("loggers", arrayNomi);
        getRequest().setAttribute("levels", levels);

        getForm().getLogger().getApplicaLivelli().setEditMode();
        forwardToPublisher(Application.Publisher.CONFIGURAZIONE_LOGGER);
    }

    /*
     * Inner class che esegue la comparazione di stringhe verificando la lunghezza
     */
    class LengthComparator implements Comparator<String>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(String first, String second) {
            int result = 0;
            if (first.length() > second.length()) {
                result = 1;
            } else if (first.length() < second.length()) {
                result = -1;
            }
            return result;
        }
    }

    @Override
    public void applicaLivelli() throws EMFError {
        String[] names = getRequest().getParameterValues("loggers");
        for (int i = 0; i < names.length; i++) {
            String thisLevel = getRequest().getParameter("loggerlevel_" + (i + 1));
            ch.qos.logback.classic.Logger tmpLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(names[i]);
            tmpLogger.setLevel(Level.toLevel(thisLevel));
        }
        getMessageBox().addInfo(messagesCtx.getLoggerLevelsOk());
        getMessageBox().setViewMode(ViewMode.plain);

        loadLoggerLevels();

    }

    @Override
    public void startObjectCreatorCoordinatorQJob() throws EMFError {
        startJob(Constants.ObjectCreatorCoordinatorQJob);

    }

    @Override
    public void stopObjectCreatorCoordinatorQJob() throws EMFError {
        stopJob(Constants.ObjectCreatorCoordinatorQJob);

    }

    @Override
    public void startObjectCreatorQJob1() throws EMFError {
        startJob(Constants.ObjectCreatorQJob1);
    }

    @Override
    public void startObjectCreatorQJob2() throws EMFError {
        startJob(Constants.ObjectCreatorQJob2);
    }

    @Override
    public void startObjectCreatorQJob3() throws EMFError {
        startJob(Constants.ObjectCreatorQJob3);
    }

    @Override
    public void stopObjectCreatorQJob1() throws EMFError {
        stopJob(Constants.ObjectCreatorQJob1);
    }

    @Override
    public void stopObjectCreatorQJob2() throws EMFError {
        stopJob(Constants.ObjectCreatorQJob2);
    }

    @Override
    public void stopObjectCreatorQJob3() throws EMFError {
        stopJob(Constants.ObjectCreatorQJob3);
    }

    @Override
    public void startPuliziaInAttesaFileQJob() throws EMFError {
        startJob(Constants.PuliziaInAttesaFileQJob);
    }

    @Override
    public void stopPuliziaInAttesaFileQJob() throws EMFError {
        stopJob(Constants.PuliziaInAttesaFileQJob);
    }

    @Override
    public void startObjectCopyQJob() throws EMFError {
        startJob(Constants.ObjectCopyQJob);
    }

    @Override
    public void stopObjectCopyQJob() throws EMFError {
        stopJob(Constants.ObjectCopyQJob);
    }

    @Override
    public void startGenericObjectCreatorQJob() throws EMFError {
        startJob(Constants.GenericObjectCreatorQJob);
    }

    @Override
    public void stopGenericObjectCreatorQJob() throws EMFError {
        stopJob(Constants.GenericObjectCreatorQJob);
    }

}
