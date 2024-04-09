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

package it.eng.dpi.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.dcm4che2.net.ConfigurationException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xadisk.bridge.proxies.interfaces.Session;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.exceptions.NoTransactionAssociatedException;

import it.eng.dpi.bean.CStoreBean;
import it.eng.dpi.bean.DicomNode;
import it.eng.dpi.bean.Patient;
import it.eng.dpi.bean.QueryBean;
import it.eng.dpi.bean.Study;
import it.eng.dpi.business.impl.InvioOggettoService;
import it.eng.dpi.component.DPIContext;
import it.eng.dpi.dicom.scu.QRConstants;
import it.eng.dpi.dicom.scu.QRConstants.QueryRetrieveLevel;
import it.eng.dpi.exception.WebGenericException;
import it.eng.dpi.exception.XAGenericException;
import it.eng.dpi.service.EchoService;
import it.eng.dpi.service.QueryMoveService;
import it.eng.dpi.service.SendService;

@Controller
public class TestController {

    @Autowired
    private QueryMoveService qrService;

    @Autowired
    private EchoService echoService;

    @Autowired
    private InvioOggettoService sender;

    @Autowired
    private SchedulerFactoryBean scheduler;

    @Autowired
    private SendService sendService;

    @Autowired
    private XAFileSystem xaDiskNativeFS;

    @Resource
    @Qualifier("dicomNodes")
    private List<DicomNode> dicomNodes;

    @Autowired
    private DPIContext context;

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @RequestMapping("/qr.do")
    public @ResponseBody Map<String, Object> qr(final HttpServletResponse res,
            @RequestParam(value = "pacshost", required = true) String hostname,
            @RequestParam(value = "pacsport", required = true) int port,
            @RequestParam(value = "pacsAET", required = true) String aet,
            @RequestParam(value = "qrlevel", required = true) String qrlevel,
            @RequestParam(value = "parname", required = false) String[] tagname,
            @RequestParam(value = "parval", required = false) String[] tagval,
            @RequestParam(value = "extkey", required = false) String ext)
            throws IOException, ConfigurationException, InterruptedException {

        Map<String, Object> response = new TreeMap<String, Object>();
        QueryBean query = new QueryBean(qrlevel.equals("P") ? QueryRetrieveLevel.PATIENT : (qrlevel.equals("SSR")
                ? QueryRetrieveLevel.STUDY_STUDY_ROOT_FIRST : QueryRetrieveLevel.STUDY_PAT_ROOT_FIRST));
        if (ext != null) {
            query.addReturnKey(QRConstants.PATIENT_RETURN_KEYS);
        }
        log.info("Query level: " + query.getQueryLevel().name());
        if (tagname != null && tagval != null) {
            for (int i = 0; i < tagname.length; i++) {
                query.addMatchingKey(new int[] { Integer.parseInt(tagname[i]) }, tagval[i]);
            }
        }

        if (qrlevel.equals("P")) {
            List<Patient> res1 = qrService.doCFindPatLevel(query, hostname, port, aet);
            response.put("qsize", "Trovati :" + String.valueOf(res1.size()) + " risultati.");
            response.put("result", res1);
        } else {
            List<Study> res1 = qrService.doCFindStudyLevel(query, hostname, port, aet);
            response.put("qsize", "Trovati :" + String.valueOf(res1.size()) + " risultati.");
            response.put("result", res1);
        }

        return response;

    }

    @RequestMapping("/move.do")
    public @ResponseBody Map<String, String> move(final HttpServletResponse res,
            @RequestParam(value = "pacshost", required = true) String hostname,
            @RequestParam(value = "pacsport", required = true) int port,
            @RequestParam(value = "pacsAET", required = true) String aet,
            @RequestParam(value = "qrlevel", required = true) String qrlevel,
            @RequestParam(value = "parname", required = false) String[] tagname,
            @RequestParam(value = "parval", required = false) String[] tagval,
            @RequestParam(value = "extkey", required = false) String ext)
            throws IOException, ConfigurationException, InterruptedException {

        QueryBean query = new QueryBean(qrlevel.equals("P") ? QueryRetrieveLevel.PATIENT : (qrlevel.equals("SSR")
                ? QueryRetrieveLevel.STUDY_STUDY_ROOT_FIRST : QueryRetrieveLevel.STUDY_PAT_ROOT_FIRST));
        log.info("Query level: " + query.getQueryLevel().name());
        if (ext != null) {
            query.addReturnKey(QRConstants.PATIENT_RETURN_KEYS);
        }
        if (tagname != null && tagval != null) {
            for (int i = 0; i < tagname.length; i++) {
                query.addMatchingKey(new int[] { Integer.parseInt(tagname[i]) }, tagval[i]);
            }
        }

        qrService.doCFindCMove(query, hostname, port, aet);
        Map<String, String> response = new TreeMap<String, String>();
        response.put("message", "Trasferiti :" + query.getCompleted() + " - Falliti :" + query.getFailed());
        return response;

    }

    @RequestMapping("/echo.do")
    public @ResponseBody Map<String, String> echo(final HttpServletResponse res,
            @RequestParam(value = "pacshost", required = true) String hostname,
            @RequestParam(value = "pacsport", required = true) int port,
            @RequestParam(value = "pacsAET", required = true) String aet)
            throws IOException, ConfigurationException, InterruptedException {
        String result = echoService.echo(hostname, port, aet, 1);
        Map<String, String> response = new TreeMap<String, String>();
        response.put("message", result);
        return response;
    }

    @RequestMapping("/sendtx.do")
    public @ResponseBody Map<String, String> sendTx(final HttpServletResponse res,
            @RequestParam(value = "pacshost", required = true) String hostname,
            @RequestParam(value = "pacsport", required = true) int port,
            @RequestParam(value = "pacsAET", required = true) String aet,
            @RequestParam(value = "studyRoot", required = true) String studyRoot) throws IOException,
            ConfigurationException, InterruptedException, XAGenericException, NoTransactionAssociatedException {
        Map<String, String> response = new TreeMap<String, String>();
        File studyRootDir = new File(studyRoot);
        if (!studyRootDir.isDirectory()) {
            response.put("message", "Il percorso indicato non esiste");
            return response;
        }
        Session session = xaDiskNativeFS.createSessionForLocalTransaction();
        CStoreBean result = sendService.doTxCStore(hostname, port, aet, studyRootDir, session);
        session.commit();
        response.put("message",
                "Inviate " + result.getTransferedImagesInStudy() + " immagini su " + result.getNumImagesInStudy());
        return response;
    }

    @RequestMapping("/send.do")
    public @ResponseBody Map<String, String> send(final HttpServletResponse res,
            @RequestParam(value = "pacshost", required = true) String hostname,
            @RequestParam(value = "pacsport", required = true) int port,
            @RequestParam(value = "pacsAET", required = true) String aet,
            @RequestParam(value = "studyRoot", required = true) String studyRoot) throws IOException,
            ConfigurationException, InterruptedException, XAGenericException, NoTransactionAssociatedException {
        Map<String, String> response = new TreeMap<String, String>();
        File studyRootDir = new File(studyRoot);
        if (!studyRootDir.isDirectory()) {
            response.put("message", "Il percorso indicato non esiste");
            return response;
        }

        CStoreBean result = sendService.doCStore(hostname, port, aet, studyRootDir);

        response.put("message",
                "Inviate " + result.getTransferedImagesInStudy() + " immagini su " + result.getNumImagesInStudy());
        return response;
    }

    @RequestMapping("/warn.do")
    public @ResponseBody void sendStudiWarning(final HttpServletResponse res,
            @RequestParam(value = "pacshost", required = true) String hostname,
            @RequestParam(value = "pacsport", required = true) int port,
            @RequestParam(value = "pacsAET", required = true) String aet)
            throws XAGenericException, IOException, WebGenericException {
        sender.processWarnFolder();
    }

    @RequestMapping("/stopJobs.do")
    public @ResponseBody Map<String, String> stopJobs(final HttpServletResponse res) throws SchedulerException {
        Scheduler sched = scheduler.getScheduler();
        for (String groupName : sched.getTriggerGroupNames()) {
            Set<TriggerKey> keys = sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName));
            for (TriggerKey triggerKey : keys) {
                sched.pauseTrigger(triggerKey);
            }
        }
        return infoJobs(res);
    }

    @RequestMapping("/infoJobs.do")
    public @ResponseBody Map<String, String> infoJobs(final HttpServletResponse res) throws SchedulerException {
        Scheduler sched = scheduler.getScheduler();
        Map<String, String> m = new HashMap<String, String>();
        for (String groupName : sched.getTriggerGroupNames()) {

            Set<TriggerKey> keys = sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName));
            for (TriggerKey triggerKey : keys) {
                Trigger t = sched.getTrigger(triggerKey);
                m.put(triggerKey.getName(), "Job: " + triggerKey.getName() + " - Ultima esecuzione: "
                        + t.getPreviousFireTime() + " - Prossima esecuzione: " + t.getNextFireTime());
            }

        }
        return m;
    }

    @RequestMapping("/startJobs.do")
    public @ResponseBody Map<String, String> startJobs(final HttpServletResponse res) throws SchedulerException {
        Scheduler sched = scheduler.getScheduler();
        for (String groupName : sched.getTriggerGroupNames()) {
            Set<TriggerKey> keys = sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName));
            for (TriggerKey triggerKey : keys) {
                sched.resumeTrigger(triggerKey);
            }
        }
        return infoJobs(res);
    }

    @RequestMapping("/fillDefPacs.do")
    public @ResponseBody List<DicomNode> getDefPacsInfo(final HttpServletResponse res) {
        return dicomNodes;
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public ModelAndView getDpiConfigutation(ModelAndView mav) {
        mav.setViewName("config");

        mav.addObject("config", context);
        return mav;
    }

}
