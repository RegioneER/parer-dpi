# DPI (Digital Preservation Interface)

Fonte template redazione documento:  https://www.makeareadme.com/.


# Descrizione

Il modulo software DPI (Digital Preservation Interface), sviluppato e manutenuto dall’Amministrazione, consiste in un sistema di interfaccia tra i sistemi dell’Ente produttore e PING che può essere installato sia all’interno della rete del Sistema di conservazione, sia all’interno della rete del Produttore dove può essere gestito secondo le sue politiche di sicurezza e autenticarsi sul suo IdP.  


# Installazione

Requisiti minimi per installazione: 

- Sistema operativo : consigliato Linux server (in alternativa compatibilità con Windows server);
- Java versione 11 (OpenJDK / Oracle);
- Tomcat 9.
  
## Instalazione JDK 

Consigliata adozione della OpenJDK alla versione 11, guida all'installazione https://openjdk.org/install/.

## Setup application server (Tomcat 9)

TODO


# Utilizzo

DPI implementa funzionalità di versamento per specifiche tipologie di SIP. In particolare, qualificandosi come nodo DICOM, DPI riceve dai PACS studi diagnostici, che poi trasmette a PING per la trasformazione e il versamento a SacER.  

DPI può operare con logiche sia push che pull, ricevendo o estraendo dati e documenti dai sistemi del Produttore per poi versarli nel Sistema, richiamando gli opportuni servizi di PING.   

Inoltre, DPI fornisce strumenti di monitoraggio dei versamenti effettuati a disposizione dell’Ente produttore. 


# Librerie utilizzate

|  GroupId | ArtifactId  | Version  | Type   |  Licenses |
|---|---|---|---|---|
|cglib|cglib-nodep|3.3.0|jar|ASF 2.0|
|ch.qos.logback|logback-classic|1.3.8|jar|Eclipse Public License - v 1.0GNU Lesser General Public License|
|ch.qos.logback|logback-core|1.3.8|jar|Eclipse Public License - v 1.0GNU Lesser General Public License|
|com.fasterxml.jackson.core|jackson-annotations|2.13.5|jar|The Apache Software License, Version 2.0|
|com.fasterxml.jackson.core|jackson-core|2.13.5|jar|The Apache Software License, Version 2.0|
|com.fasterxml.jackson.core|jackson-databind|2.13.5|jar|The Apache Software License, Version 2.0|
|com.google.guava|guava|31.1-jre|jar|Apache License, Version 2.0|
|commons-beanutils|commons-beanutils|1.9.4|jar|Apache License, Version 2.0|
|commons-codec|commons-codec|1.15|jar|Apache License, Version 2.0|
|commons-io|commons-io|2.12.0|jar|Apache-2.0|
|commons-net|commons-net|3.9.0|jar|Apache License, Version 2.0|
|dcm4che|dcm4che-core|2.0.29|jar|-|
|dcm4che|dcm4che-net|2.0.29|jar|-|
|it.eng.parer|spagofat-core|4.11.0|jar|-|
|it.eng.parer|spagofat-middle|4.11.0|jar|-|
|it.eng.parer|spagofat-paginator-ejb|4.11.0|ejb|-|
|it.eng.parer|spagofat-si-client|4.11.0|jar|-|
|it.eng.parer|spagofat-si-util|4.11.0|jar|-|
|javax.resource|connector-api|1.5|jar|-|
|net.java.xadisk|xadisk|1.2.2.5|jar|-|
|net.sf|j2ep|0.0.1|jar|-|
|org.apache.commons|commons-compress|1.23.0|jar|Apache-2.0|
|org.apache.commons|commons-lang3|3.12.0|jar|Apache License, Version 2.0|
|org.apache.commons|commons-text|1.10.0|jar|Apache License, Version 2.0|
|org.apache.cxf.build-utils|cxf-buildtools|3.4.4|jar|Apache License, Version 2.0|
|org.apache.taglibs|taglibs-standard-impl|1.2.5|jar|The Apache Software License, Version 2.0|
|org.apache.taglibs|taglibs-standard-jstlel|1.2.5|jar|The Apache Software License, Version 2.0|
|org.apache.taglibs|taglibs-standard-spec|1.2.5|jar|The Apache Software License, Version 2.0|
|org.aspectj|aspectjrt|1.9.19|jar|Eclipse Public License - v 2.0|
|org.aspectj|aspectjweaver|1.9.19|jar|Eclipse Public License - v 2.0|
|org.dom4j|dom4j|2.1.3|jar|BSD 3-clause New License|
|org.quartz-scheduler|quartz|2.3.2|jar|The Apache Software License, Version 2.0|
|org.slf4j|slf4j-api|2.0.7|jar|MIT License|
|org.springframework|spring-aop|5.3.28|jar|Apache License, Version 2.0|
|org.springframework|spring-context|5.3.28|jar|Apache License, Version 2.0|
|org.springframework|spring-context-support|5.3.28|jar|Apache License, Version 2.0|
|org.springframework|spring-core|5.3.28|jar|Apache License, Version 2.0|
|org.springframework|spring-tx|5.3.28|jar|Apache License, Version 2.0|
|org.springframework|spring-web|5.3.28|jar|Apache License, Version 2.0|
|org.springframework|spring-webmvc|5.3.28|jar|Apache License, Version 2.0|
|org.springframework.security|spring-security-config|5.7.9|jar|Apache License, Version 2.0|
|org.springframework.security|spring-security-core|5.7.9|jar|Apache License, Version 2.0|
|org.springframework.security|spring-security-web|5.7.9|jar|Apache License, Version 2.0|
|org.springframework.security.extensions|spring-security-saml2-core|1.0.10.RELEASE|jar|The Apache Software License, Version 2.0|
|xalan|xalan|2.7.2|jar|The Apache Software License, Version 2.0|


# Supporto

Mantainer del progetto è [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Contributi

Se interessati a crontribuire alla crescita del progetto potete scrivere all'indirizzo email <a href="mailto:areasviluppoparer@regione.emilia-romagna.it">areasviluppoparer@regione.emilia-romagna.it</a>.

# Credits

Progetto di proprietà di [Regione Emilia-Romagna](https://www.regione.emilia-romagna.it/) sviluppato a cura di [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Licenza

Questo progetto è rilasciato sotto licenza GNU Affero General Public License v3.0 or later ([LICENSE.txt](LICENSE.txt)).
