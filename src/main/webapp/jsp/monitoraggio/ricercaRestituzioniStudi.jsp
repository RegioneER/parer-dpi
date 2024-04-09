<%@ page import="it.eng.dpi.slite.gen.form.MonitoraggioForm" pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>

<sl:html>
    <sl:head title="Ricerca restituzioni studi" />
    <sl:body>
        <sl:header showChangeOrganizationBtn="false"/>
        <sl:menu showChangePasswordBtn="true" />
        <sl:content>
            <slf:messageBox />
            <sl:newLine skipLine="true"/>

            <sl:contentTitle title="RICERCA RESTITUZIONI STUDI"/>

            <slf:fieldSet  borderHidden="false">
                <!-- piazzo i campi del filtro di ricerca -->
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.DT_APERTURA_SESSIONE_DA%>" controlWidth="w70" colSpan="1"/>
                <slf:doubleLblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ORE_DT_APERTURA_SESSIONE_DA%>" name2="<%=MonitoraggioForm.RicercaRestituzioniStudi.MINUTI_DT_APERTURA_SESSIONE_DA%>" controlWidth="w20" controlWidth2="w20" colSpan="1"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.DT_APERTURA_SESSIONE_A%>" controlWidth="w70" colSpan="1"/>
                <slf:doubleLblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ORE_DT_APERTURA_SESSIONE_A%>" name2="<%=MonitoraggioForm.RicercaRestituzioniStudi.MINUTI_DT_APERTURA_SESSIONE_A%>" controlWidth="w20" controlWidth2="w20" colSpan="1"/>
                <sl:newLine skipLine="true" />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.DT_STUDY_DA%>" controlWidth="w70" colSpan="1"/>
                <slf:doubleLblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ORE_DT_STUDY_DA%>" name2="<%=MonitoraggioForm.RicercaRestituzioniStudi.MINUTI_DT_STUDY_DA%>" controlWidth="w20" controlWidth2="w20" colSpan="1"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.DT_STUDY_A%>" controlWidth="w70" colSpan="1"/>
                <slf:doubleLblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ORE_DT_STUDY_A%>" name2="<%=MonitoraggioForm.RicercaRestituzioniStudi.MINUTI_DT_STUDY_A%>" controlWidth="w20" controlWidth2="w20" colSpan="1"/>
                <sl:newLine skipLine="true" />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.NODO_DICOM%>" colSpan="2"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.TI_STATO%>" colSpan="2"/>
                <sl:newLine />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.PAZIENTE%>" colSpan="2" />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.FILTRO_PAZIENTE%>" colSpan="2" />
                <sl:newLine />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.SESSO_PAZIENTE%>" colSpan="2"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.DT_NASCITA_PAZIENTE%>" colSpan="2"/>
                <sl:newLine />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ID_PAZIENTE%>" colSpan="2"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ISSUER_ID_PAZIENTE%>" colSpan="2"/>
                <sl:newLine />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.ACCESSION_NUMBER%>" colSpan="2"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.MEDICO%>" colSpan="2"/>
                <sl:newLine />
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.STUDY_UID%>" colSpan="2"/>
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.MODALITY_STUDY%>" colSpan="2"/>
            </slf:fieldSet>
            <sl:newLine skipLine="true" />

            <sl:pulsantiera>
                <!-- piazzo i bottoni di ricerca ed inserimento -->
                <slf:lblField name="<%=MonitoraggioForm.RicercaRestituzioniStudi.RICERCA_RESTITUZIONI_STUDI%>" width="w25" />
            </sl:pulsantiera>
            <sl:newLine skipLine="true"/>

            <!--  piazzo la lista con i risultati -->
            <slf:list abbrLongList="true" name="<%= MonitoraggioForm.RestituzioniStudiList.NAME%>" />
            <slf:listNavBar  name="<%= MonitoraggioForm.RestituzioniStudiList.NAME%>" />

        </sl:content>
        <sl:footer />
    </sl:body>
</sl:html>