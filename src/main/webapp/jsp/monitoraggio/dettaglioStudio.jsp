<%@ page import="it.eng.dpi.slite.gen.form.MonitoraggioForm" pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>

<sl:html>
<sl:head title="Dettaglio diario" />
<sl:body>
	<sl:header showChangeOrganizationBtn="false" />
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<sl:newLine skipLine="true" />

		<sl:contentTitle title="DETTAGLIO DIARIO" />

		<slf:listNavBarDetail name="${param.table}" />

		<sl:newLine skipLine="true" />
		<slf:tab name="<%= MonitoraggioForm.DettaglioTabs.NAME%>" tabElement="<%= MonitoraggioForm.DettaglioTabs.info_studio%>">
			<slf:fieldSet borderHidden="false">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_PATIENT_NAME%>" colSpan="1" controlWidth="w80" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_PATIENT_SEX%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_PATIENT_BIRTH_DATE%>" colSpan="2" />
				<sl:newLine />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_STUDY_DATE%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_AET_NODO_DICOM%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_STATO_OBJECT%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_ERR%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_ERR%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CHIAVE_UNITA_DOC%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_ACCESSION_NUMBER%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_PATIENT_ID%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_PATIENT_ID_ISSUER%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_REF_PHYSICIAN_NAME%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_INSTITUTION_NAME%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_STUDY_DESCRIPTION%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_STUDY_INSTANCE_UID%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_STUDY_ID%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.NI_STUDY_RELATED_SERIES%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.NI_STUDY_RELATED_IMAGES%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_PRESA_IN_CARICO%>" colSpan="2" />
			</slf:fieldSet>
		</slf:tab>
		<slf:tab name="<%= MonitoraggioForm.DettaglioTabs.NAME%>" tabElement="<%= MonitoraggioForm.DettaglioTabs.info_recupero%>">
			<slf:fieldSet borderHidden="false">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_STATO_SESSIONE_RECUP%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_APERTURA_SESSIONE_RECUP%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.FL_TRANSFER_DICOM%>" colSpan="2" />
			</slf:fieldSet>
		</slf:tab>
		<slf:tab name="<%= MonitoraggioForm.DettaglioTabs.NAME%>" tabElement="<%= MonitoraggioForm.DettaglioTabs.info_sessione%>">
			<slf:fieldSet borderHidden="false">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.ID_SESSIONE%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_STATO_SESSIONE%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_APERTURA_SESSIONE%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_CHIUSURA_SESSIONE%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.FL_FORZA_ACCETTAZIONE%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.FL_FORZA_WARNING%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_MOTIVO_FORZA_ACCETTAZIONE%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_MOTIVO_CHIUSO_WARNING%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<%-- 			<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_MOTIVO_CHIUSO_WARNING_STANDARD%>" colSpan="2" /> --%>
			</slf:fieldSet>
		</slf:tab>
		<sl:newLine skipLine="true" />
		<div>
			<input type="hidden" name="table" value="${param.table}" />
		</div>
		<sl:newLine skipLine="true" />
		<sl:pulsantiera>
			<slf:buttonList name="<%=MonitoraggioForm.DetailButtonList.NAME%>" />
		</sl:pulsantiera>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.ModalityInStudySection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:field name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_LISTA_MODALITY_IN_STUDY%>" colSpan="4" controlWidth="w90" />
			</slf:fieldSet>
		</slf:section>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.SopClassSection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:field name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DL_LISTA_SOP_CLASS%>" colSpan="4" controlWidth="w90" />
			</slf:fieldSet>
		</slf:section>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.DCMSection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_DCM_HASH%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_ALGO_DCM_HASH%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_ENCODING_DCM_HASH%>" colSpan="1" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.BL_DCM_HASH_TXT%>" colSpan="4" controlWidth="w100" />
			</slf:fieldSet>
		</slf:section>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.GLOBALSection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_GLOBAL_HASH%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_ALGO_GLOBAL_HASH%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_ENCODING_GLOBAL_HASH%>" colSpan="1" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.BL_GLOBAL_HASH_TXT%>" colSpan="4" controlWidth="w100" />
			</slf:fieldSet>
		</slf:section>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.FILESection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_FILE_HASH%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_ALGO_FILE_HASH%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_ENCODING_FILE_HASH%>" colSpan="1" />
			</slf:fieldSet>
		</slf:section>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.XMLUDSection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:field name="<%=MonitoraggioForm.RicercaDiarioDettaglio.BL_XML_INDICE_UD %>" colSpan="4" controlWidth="w100" />
			</slf:fieldSet>
		</slf:section>
		<sl:newLine skipLine="true" />
		<slf:section name="<%=MonitoraggioForm.XMLPCSection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:field name="<%=MonitoraggioForm.RicercaDiarioDettaglio.BL_XML_INDICE_PC %>" colSpan="4" controlWidth="w100" />
			</slf:fieldSet>
		</slf:section>
	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>