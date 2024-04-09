<%@ page import="it.eng.dpi.slite.gen.form.MonitoraggioForm" pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>

<sl:html>
<sl:head title="Forza accettazione warning" />
<sl:body>
	<sl:header showChangeOrganizationBtn="false" />
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<sl:newLine skipLine="true" />

		<sl:contentTitle title="FORZA ACCETTAZIONE WARNING" />

		<slf:section name="<%=MonitoraggioForm.StudySummarySection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_PATIENT_NAME%>" colSpan="1" controlWidth="w80" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_PATIENT_SEX%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_PATIENT_BIRTH_DATE%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_STUDY_DATE%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_PRESA_IN_CARICO%>" colSpan="2" />
			</slf:fieldSet>
		</slf:section>
		
		<c:if test="${empty param.table}">
			<slf:list name="<%=MonitoraggioForm.ListaGestioneWarning.NAME%>" />
		</c:if>
		
		<sl:newLine skipLine="true" />
		<slf:fieldSet borderHidden="true">
			<slf:lblField name="<%=MonitoraggioForm.GestioneWarning.DL_MOTIVO_FORZA_ACCETTAZIONE%>" colSpan="4" />
		</slf:fieldSet>
		<sl:newLine skipLine="true" />
		<div>
			<input type="hidden" name="table" value="${param.table}" />
		</div>

		<sl:pulsantiera>
			<slf:lblField name="<%=MonitoraggioForm.GestioneWarning.ANNULLA_VERSAMENTO_WARNING%>" width="w25" />
			<slf:lblField name="<%=MonitoraggioForm.GestioneWarning.CONFERMA_VERSAMENTO_WARNING%>" width="w25" />
		</sl:pulsantiera>
	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>