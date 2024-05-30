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

package it.eng.dpi.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.filesystem.NativeXAFileSystem;
import org.xadisk.filesystem.standalone.StandaloneFileSystemConfiguration;

import it.eng.dpi.bean.DicomNode;
import it.eng.dpi.bean.ObjectType;
import it.eng.dpi.dicom.scp.EchoSCP;
import it.eng.dpi.dicom.scp.TxStoreSCP;
import it.eng.dpi.dicom.scu.EchoSCU;
import it.eng.dpi.dicom.scu.QRSCU;
import it.eng.dpi.dicom.scu.StoreSCU;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.ArrayUtils;
import org.dcm4che2.net.Device;

@Configuration
@ImportResource({ "file:///${catalina.base}/conf/dpi-${env}/securityContext.xml" })
@ComponentScan(basePackages = "org.springframework.security.saml")
public class DPIConfigInitializer {
    /*
     * Common stuff
     */
    private static final Logger log = LoggerFactory.getLogger(DPIConfigInitializer.class);
    private Properties properties = null;
    private Boolean enableSCP = null;
    private String aeTitle = null;
    private Integer port = null;
    private Integer reaperTimeoutMS = null;
    private Integer connTimeoutMS = null;
    // MEV#30349
    private Boolean enableDicomTLS = null;
    // end MEV#30349
    @Resource
    @Qualifier("validAET")
    private List<String> validAET;
    @Autowired
    private DicomServer dicomServer;
    @Autowired
    private EchoSCU echoSCU;
    @Autowired
    private QRSCU qrSCU;
    @Autowired
    private QueryMoveService queryMoveService;
    // @Autowired
    // private StoreCmtSCP storeCmtSCP;
    @Autowired
    private StoreSCU storeSCU;
    @Autowired
    private XAFileSystem xaDiskNativeFS;
    // The number of threads here likely corresponds to how many concurrent
    // associations can be served
    // (but need at least two to serve a single association; also, starting with
    // a smaller core pool
    // does not seem to work, so keep the two numbers in sync)
    private final ExecutorService executor = new ThreadPoolExecutor(50, 50, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<Runnable>());

    @PostConstruct
    public void postConstruct() {
        try {
            createDPIFileSystem();
            startDicomServer();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (dicomServer != null) {
            dicomServer.stop();
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    log.warn("DICOM SERVER Executor service did not terminate.");
                }
            }
        } catch (final InterruptedException e) {
            executor.shutdownNow();
        }

        try {
            xaDiskNativeFS.shutdown();
        } catch (IOException e) {
            log.error("Errore durante lo spegnimento dell'istanza XaDisk", e);

        }
    }

    @Bean
    @Scope(value = "singleton")
    protected DicomServer getDicomServer() throws IOException, GeneralSecurityException {
        // Device
        DicomServer dicomServer = new DicomServer(validAET, DPIConstants.defaultStorageSOPClasses, getReaperTimeoutMS(),
                getConnTimeoutMS());
        dicomServer.setAETitle(getDpiAETitle());
        dicomServer.setPort(getPort());
        // MEV#30394
        if (enableDicomTLS) {
            String[] tlsProtocols = getTlsProtocols().isEmpty() ? getActiveProtocols()
                    : ArrayUtils.toStringArray(getTlsProtocols().toArray());
            String[] tlsCipherSuites = getTlsCipherSuites().isEmpty() ? getActiveCipherSuites()
                    : ArrayUtils.toStringArray(getTlsCipherSuites().toArray());
            dicomServer.getNetworkConnection().setTlsProtocol(tlsProtocols);
            dicomServer.getNetworkConnection().setTlsCipherSuite(tlsCipherSuites);
            initTLS(dicomServer.getNetworkConnection().getDevice());
        }
        // end MEV#30394
        return dicomServer;
    }

    @Bean
    @Scope(value = "singleton")
    protected EchoSCP getEchoSCP() throws IOException {
        return new EchoSCP(dicomServer.getAeStoreSCP());
    }

    @Bean
    @Scope(value = "singleton")
    protected TxStoreSCP getTxStoreSCP() throws IOException {
        TxStoreSCP txStoreSCP = new TxStoreSCP(dicomServer.getAeStoreSCP(), DPIConstants.defaultStorageSOPClasses,
                queryMoveService);
        txStoreSCP.setStorageRootDir(storageSCPDir());
        txStoreSCP.setXaDiskNativeFS(xaDiskNativeFS);
        txStoreSCP.setDicomNodes(getDicomNodes());
        // DAV
        txStoreSCP.setAcceptMore(getAcceptMoreTag());
        // DAV
        txStoreSCP.setContaMultiframe(getContaMultiframeTag());
        return txStoreSCP;
    }
    //
    // @Bean
    // @Scope(value = "singleton")
    // protected StoreCmtSCP getStoreCmtSCP() throws IOException {
    // StoreCmtSCP storeCmtSCP = new StoreCmtSCP(dicomServer.getAeStoreSCP(), executor);
    // storeCmtSCP.setStorageRootDir(storageSCPDir());
    // return storeCmtSCP;
    // }

    @Bean
    @Scope(value = "singleton")
    protected QRSCU getQrSCU() {
        return new QRSCU(dicomServer.getAeSCU());
    }

    @Bean
    @Scope(value = "singleton")
    protected StoreSCU getStoreSCU() {
        return new StoreSCU(dicomServer.getAeStoreSCU());
    }

    @Bean
    @Scope(value = "singleton")
    protected EchoSCU getEchoSCU() {
        return new EchoSCU(dicomServer.getAeSCU());
    }

    @Bean
    @Scope(value = "singleton")
    protected QueryMoveService getQueryMoveService() {
        return new QueryMoveService(qrSCU, executor);
    }

    @Bean
    @Scope(value = "singleton")
    protected SendService getSendService() {
        return new SendService(storeSCU, executor);
    }

    @Bean
    @Scope(value = "singleton")
    protected EchoService getEchoService() {
        return new EchoService(echoSCU, executor);
    }

    @Bean
    @Scope(value = "singleton")
    public XAFileSystem xaDiskNativeFS() throws IOException {
        StandaloneFileSystemConfiguration configuration = new StandaloneFileSystemConfiguration(
                getConfigString("dpi.work_path") + getConfigString("dpi.xadisk_path"), getDpiAETitle());
        // transaction timeout 1h
        configuration.setTransactionTimeout(3600);
        configuration.setLockTimeOut(90000);
        // Commentato il settaggio della dimensione del buffer in modo che utilizzi il default 4K
        configuration.setBufferSize(getConfigInt("dpi.xadisk_buffersize"));
        log.info("Parametri XADisk: "
                + ReflectionToStringBuilder.toString(configuration, ToStringStyle.MULTI_LINE_STYLE));
        XAFileSystem xafs = NativeXAFileSystem.bootXAFileSystemStandAlone(configuration);
        try {
            xafs.waitForBootup(60000);
        } catch (InterruptedException e) {
            log.error("Errore durante l'avvio di XAdisk", e);
        }

        return xafs;
    }

    private void startDicomServer() throws IOException {
        try {
            dicomServer.start(executor);
        } catch (final IOException e) {
            // Need to catch and re-throw checked exceptions for @PostConstruct
            throw new RuntimeException(e);
        }

    }

    private Properties envSpecificProperties() throws IOException {
        if (properties == null) {
            final FileSystemResource fsResource = new FileSystemResource(
                    System.getProperty("catalina.base") + "/conf/dpi-" + System.getProperty("env") + "/dpi.properties");
            final Properties defaultProperties = new Properties();
            defaultProperties.load(fsResource.getInputStream());
            properties = defaultProperties;
        }
        return properties;
    }

    protected String getConfigString(final String key) throws IOException {
        return envSpecificProperties().getProperty(key);
    }

    protected Integer getConfigInt(final String key) throws IOException {
        final String stringVal = getConfigString(key);
        return StringUtils.isBlank(stringVal) ? null : Integer.valueOf(stringVal);
    }

    protected Boolean getConfigBool(final String key) throws IOException {
        final String stringVal = getConfigString(key);
        return StringUtils.isBlank(stringVal) ? null : Boolean.valueOf(stringVal);
    }

    public Boolean enableSCP() throws IOException {
        if (enableSCP == null) {
            enableSCP = getConfigBool("dpi.storescp.enable");
        }
        return enableSCP;
    }

    public void createDPIFileSystem() throws IOException {

        final String studioDicomBasePath = getConfigString("dpi.work_path") + getConfigString("dpi.studiodicom_path");
        final String basePathSCP = studioDicomBasePath + getConfigString("dpi.storescp.path");
        final String basePathSCU = studioDicomBasePath + getConfigString("dpi.storescu.path");
        int i = 0;
        if (StringUtils.isNotBlank(basePathSCP) && StringUtils.isNotBlank(basePathSCU)) {
            String relpath;
            while ((relpath = getConfigString("nododicom.aet." + i)) != null && !relpath.equals("")) {
                File pacsRootDir = new File(storageSCPDir(), relpath);
                pacsRootDir.mkdirs();
                // genero le dir cmove e cstore
                new File(pacsRootDir, DPIConstants.CMOVE_DIR).mkdirs();
                new File(pacsRootDir, DPIConstants.CSTORE_DIR).mkdirs();
                pacsRootDir = new File(storageSCUDir(), relpath);
                pacsRootDir.mkdirs();
                new File(pacsRootDir, DPIConstants.WORK_FOLDER).mkdirs();
                i++;
            }
        }
        final File newDir = new File(studioDicomBasePath + DPIConstants.NEW_FOLDER);
        new File(newDir, DPIConstants.CMOVE_DIR).mkdirs();
        new File(newDir, DPIConstants.CSTORE_DIR).mkdirs();

        final File outDir = new File(studioDicomBasePath + DPIConstants.OUT_FOLDER);
        outDir.mkdirs();

        final File warnDir = new File(studioDicomBasePath + DPIConstants.WARN_FOLDER);
        warnDir.mkdirs();

        // FOLDER PER IL RECUPER
        final File richieste = new File(studioDicomBasePath + DPIConstants.RICHIESTE_FOLDER);
        richieste.mkdirs();

        final File disponibili = new File(studioDicomBasePath + DPIConstants.DISPONIBILI_FOLDER);
        disponibili.mkdirs();

        final File prelevati = new File(studioDicomBasePath + DPIConstants.PRELEVATI_FOLDER);
        prelevati.mkdirs();

        final File notificati = new File(studioDicomBasePath + DPIConstants.NOTIFICATI_FOLDER);
        notificati.mkdirs();

        int j = 0;
        String genericRelpath;

        while ((genericRelpath = getConfigString("tipoObj." + j)) != null && !"".equals(genericRelpath)) {
            File genericItemRootDir = new File(getConfigString("dpi.work_path") + File.separatorChar + genericRelpath);
            // genericItemRootDir.mkdirs();
            File genericItemNewDir = new File(genericItemRootDir + DPIConstants.NEW_FOLDER);
            genericItemNewDir.mkdirs();
            File genericItemOutDir = new File(genericItemRootDir + DPIConstants.OUT_FOLDER);
            genericItemOutDir.mkdirs();
            // creo la cartella dei copiati (per gli oggetti generici)
            File afterCopyDir = new File(
                    getConfigString("tipoObj.copiato_path." + j) + File.separatorChar + genericRelpath);
            if (!afterCopyDir.exists()) {
                afterCopyDir.mkdirs();
            }
            // creo la cartella di input (per gli oggetti generici)
            File inputCopyDir = new File(
                    getConfigString("tipoObj.input_path." + j) + File.separatorChar + genericRelpath);
            if (!inputCopyDir.exists()) {
                inputCopyDir.mkdirs();
            }
            j++;
        }
    }

    @Bean(name = { "delayTime" })
    @Scope(value = "singleton")
    public Optional<Integer> getDelayTime() throws IOException {
        // MEV#27600
        return Optional.ofNullable(getConfigInt("dpi.delay_time"));
        // end MEV#27600
    }

    @Bean
    @Scope(value = "singleton")
    public File storageSCPDir() throws IOException {
        final String studioDicomBasePath = getConfigString("dpi.work_path") + getConfigString("dpi.studiodicom_path");
        return new File(studioDicomBasePath + getConfigString("dpi.storescp.path"));
    }

    @Bean
    @Scope(value = "singleton")
    public File storageSCUDir() throws IOException {
        final String studioDicomBasePath = getConfigString("dpi.work_path") + getConfigString("dpi.studiodicom_path");
        return new File(studioDicomBasePath + getConfigString("dpi.storescu.path"));
    }

    @Bean(name = { "validAET" })
    @Scope(value = "singleton")
    public List<String> getValidAET() throws IOException {
        ArrayList<String> aets = new ArrayList<String>();
        int i = 0;
        String aet;
        while ((aet = getConfigString("validaet." + i)) != null && !aet.equals("")) {
            aets.add(aet);
            i++;
        }
        return aets;
    }

    @Bean(name = { "pacsList" })
    @Scope(value = "singleton")
    public List<String> getPacsList() throws IOException {
        ArrayList<String> aets = new ArrayList<String>();
        int i = 0;
        String aet;
        while ((aet = getConfigString("nododicom.aet." + i)) != null && !aet.equals("")) {
            aets.add(aet);
            i++;
        }
        return aets;
    }

    @Bean(name = { "pacsListForSearch" })
    @Scope(value = "singleton")
    public List<String> getPacsListForSearch() throws IOException {
        ArrayList<String> aets = new ArrayList<String>();
        int i = 0;
        String aet;
        while ((aet = getConfigString("nododicom.aet." + i)) != null && !aet.equals("")) {
            aets.add(aet);
            i++;
        }

        i = 0;
        while ((aet = getConfigString("dicomstopped.aet." + i)) != null && !aet.equals("")) {
            aets.add(aet);
            i++;
        }
        return aets;
    }

    @Bean(name = { "dicomNodes" })
    @Scope(value = "singleton")
    public List<DicomNode> getDicomNodes() throws IOException {
        ArrayList<DicomNode> nodes = new ArrayList<DicomNode>();
        int i = 0;
        String aet;
        while ((aet = getConfigString("nododicom.aet." + i)) != null && !aet.equals("")) {
            String ip = getConfigString("nododicom.ip." + i);
            int port = Integer.parseInt(getConfigString("nododicom.port." + i));
            DicomNode node = new DicomNode(ip, port, aet);
            nodes.add(node);
            i++;
        }
        return nodes;
    }

    @Bean(name = { "transferPacsList" })
    @Scope(value = "singleton")
    public List<String> getTransferPacsList() throws IOException {
        ArrayList<String> aets = new ArrayList<String>();
        int i = 0;
        String aet;
        while ((aet = getConfigString("nododicomtrasf.aet." + i)) != null && !aet.equals("")) {
            aets.add(aet);
            i++;
        }
        return aets;
    }

    @Bean(name = { "transferDicomNodes" })
    @Scope(value = "singleton")
    public List<DicomNode> getTransferDicomNodes() throws IOException {
        ArrayList<DicomNode> nodes = new ArrayList<DicomNode>();
        int i = 0;
        String aet;
        while ((aet = getConfigString("nododicomtrasf.aet." + i)) != null && !aet.equals("")) {
            String ip = getConfigString("nododicomtrasf.ip." + i);
            int port = Integer.parseInt(getConfigString("nododicomtrasf.port." + i));
            DicomNode node = new DicomNode(ip, port, aet);
            nodes.add(node);
            i++;
        }
        return nodes;
    }

    @Bean(name = { "dcmHashDicomTag" })
    @Scope(value = "singleton")
    public List<String> getDcmHashDicomTag() throws IOException {
        ArrayList<String> tags = new ArrayList<String>();
        int i = 0;
        String tag;
        while ((tag = getConfigString("dpi.dcmhash.attributo." + i)) != null) {
            tags.add(tag);
            i++;
        }
        return tags;
    }

    @Bean
    @Scope(value = "singleton")
    public String getDpiAETitle() throws IOException {
        if (aeTitle == null) {
            final String prop = getConfigString("dpi.aet");
            if (StringUtils.isBlank(prop)) {
                aeTitle = "";
            } else {
                aeTitle = prop;
            }
        }
        return aeTitle;
    }

    @Bean(name = { "dlMotivoWarningStandards" })
    @Scope(value = "singleton")
    public List<String> getDlMotivoWarningStandards() throws IOException {
        ArrayList<String> motivi = new ArrayList<String>();
        int i = 0;
        String motivo;
        while ((motivo = getConfigString("dpi.monitoraggio.chiusurawarning." + i)) != null) {
            motivi.add(motivo);
            i++;
        }
        return motivi;
    }

    public Integer getPort() throws IOException {
        if (port == null) {
            port = enableDicomTLS() ? getConfigInt("dpi.dicom_tls.port") : getConfigInt("dpi.port");
        }
        return port;
    }

    public Integer getReaperTimeoutMS() throws IOException {
        if (reaperTimeoutMS == null) {
            reaperTimeoutMS = getConfigInt("dpi.reaper_timeout_ms");
        }
        return reaperTimeoutMS;
    }

    public Integer getConnTimeoutMS() throws IOException {
        if (connTimeoutMS == null) {
            connTimeoutMS = getConfigInt("dpi.conn_timeout_ms");
        }
        return connTimeoutMS;
    }

    @Bean(name = { "objectTypes" })
    @Scope(value = "singleton")
    public List<ObjectType> getObjectTypes() throws IOException, Exception {
        ArrayList<ObjectType> objectTypes = new ArrayList<ObjectType>();
        int i = 0;
        String objType;
        while ((objType = getConfigString("tipoObj." + i)) != null && !objType.equals("")) {
            String inputPath = getConfigString("tipoObj.input_path." + i);
            String copiatoPath = getConfigString("tipoObj.copiato_path." + i);
            boolean flCreaZip = new Boolean(getConfigString("tipoObj.flCreaZip." + i));
            String tiCalcRegistroUd = getConfigString("tipoObj.tiCalcRegistroUd." + i);
            if (!tiCalcRegistroUd.equals(DPIConstants.PRODUTTORE)) {
                throw new Exception(
                        "Il valore del parametro 'tipoObj.tiCalcRegistroUd' letto dal file di property non è tra quelli attesi");
            }
            String tiCalcAnnoUd = getConfigString("tipoObj.tiCalcAnnoUd." + i);
            if (!tiCalcAnnoUd.equals(DPIConstants.PRIMI_4_CRT)) {
                throw new Exception(
                        "Il valore del parametro 'tipoObj.tiCalcAnnoUd' letto dal file di property non è tra quelli attesi");
            }
            String tiCalcKeyUd = getConfigString("tipoObj.tiCalcKeyUd." + i);
            if (!tiCalcKeyUd.equals(DPIConstants.DA_5_A_8_CRT)) {
                throw new Exception(
                        "Il valore del parametro 'tipoObj.tiCalcKeyUd' letto dal file di property non è tra quelli attesi");
            }
            String tipoFile = getConfigString("tipoObj.tipoFile." + i);
            String tiCalcProfiloUd = getConfigString("tipoObj.tiCalcProfiloUd." + i);
            if (!tiCalcProfiloUd.isEmpty()) {
                if (!tiCalcProfiloUd.equals(DPIConstants.CALC_1)) {
                    throw new Exception(
                            "Il valore del parametro 'tipoObj.tiCalcProfiloUd' letto dal file di property non è tra quelli attesi");
                }
            }
            ObjectType objectType = new ObjectType(objType, inputPath, copiatoPath, flCreaZip, tiCalcRegistroUd,
                    tiCalcAnnoUd, tiCalcKeyUd, tipoFile, tiCalcProfiloUd);
            objectTypes.add(objectType);
            i++;
        }
        return objectTypes;
    }

    @Bean(name = { "acceptMore" })
    @Scope(value = "singleton")
    public boolean getAcceptMoreTag() throws IOException {
        return getConfigBool("dpi.accept_more");
    }

    @Bean(name = { "contaMultiframe" })
    @Scope(value = "singleton")
    public boolean getContaMultiframeTag() throws IOException {
        return getConfigBool("dpi.conta_multiframe");
    }

    @Bean(name = { "replaceEmptyTag" })
    @Scope(value = "singleton")
    public boolean getReplaceEmptyTag() throws IOException {
        return getConfigBool("dpi.replace_empty_tag");
    }

    // MEV#30349
    public Boolean enableDicomTLS() throws IOException {
        if (enableDicomTLS == null) {
            enableDicomTLS = getConfigBool("dpi.dicom_tls.enable");
        }
        return enableDicomTLS;
    }

    public List<String> getTlsProtocols() throws IOException {
        ArrayList<String> protocols = new ArrayList<>();
        int i = 0;
        String protocol;
        while ((protocol = getConfigString("dpi.dicom_tls.protocol." + i)) != null && !protocol.equals("")) {
            protocols.add(protocol);
            i++;
        }
        return protocols;
    }

    public List<String> getTlsCipherSuites() throws IOException {
        ArrayList<String> cipherSuites = new ArrayList<>();
        int i = 0;
        String cipher;
        while ((cipher = getConfigString("dpi.dicom_tls.cipher_suite." + i)) != null && !cipher.equals("")) {
            cipherSuites.add(cipher);
            i++;
        }
        return cipherSuites;
    }

    private static String[] getActiveProtocols() {
        String[] protocols = {};
        try {
            protocols = SSLContext.getDefault().createSSLEngine().getEnabledProtocols();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to get enabled protocols:", e);
        }
        return protocols;
    }

    private static String[] getActiveCipherSuites() {
        String[] cipherSuites = {};
        try {
            cipherSuites = SSLContext.getDefault().createSSLEngine().getEnabledCipherSuites();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to get enabled cipher suites:", e);
        }
        return cipherSuites;
    }

    public void initTLS(Device device) throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(getConfigString("dpi.dicom_tls.keyStoreURL"),
                getConfigString("dpi.dicom_tls.keyStorePassword").toCharArray());
        KeyStore trustStore = loadKeyStore(getConfigString("dpi.dicom_tls.trustStoreURL"),
                getConfigString("dpi.dicom_tls.trustStorePassword").toCharArray());
        device.initTLS(keyStore,
                getConfigString("dpi.dicom_tls.keyPassword") != null
                        ? getConfigString("dpi.dicom_tls.keyPassword").toCharArray()
                        : getConfigString("dpi.dicom_tls.keyStorePassword").toCharArray(),
                trustStore);
    }

    private static KeyStore loadKeyStore(String url, char[] password) throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("/")) {
            final FileSystemResource fsResource = new FileSystemResource(
                    System.getProperty("catalina.base") + "/dicom_tls/dpi-" + System.getProperty("env") + url);
            return fsResource.getInputStream();
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12") ? "PKCS12" : "JKS";
    }
    // end MEV#30349
}
