<%@ page import="it.eng.dpi.slite.gen.form.MonitoraggioForm" pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>

<sl:html>
<sl:head title="Trasferimento al nodo DICOM" />
<sl:body>
	<sl:header showChangeOrganizationBtn="false" />
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<sl:newLine skipLine="true" />

		<sl:contentTitle title="TRASFERIMENTO AL NODO DICOM" />

		<slf:section name="<%=MonitoraggioForm.StudySummarySection.NAME%>" styleClass="importantContainer">
			<slf:fieldSet borderHidden="true">
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DS_PATIENT_NAME%>" colSpan="1" controlWidth="w80" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.TI_PATIENT_SEX%>" colSpan="1" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_PATIENT_BIRTH_DATE%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_STUDY_DATE%>" colSpan="2" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.DT_PRESA_IN_CARICO%>" colSpan="2" />
				<sl:newLine skipLine="true" />
				<slf:lblField name="<%=MonitoraggioForm.RicercaDiarioDettaglio.CD_AET_NODO_DICOM%>" colSpan="1" />
			</slf:fieldSet>
		</slf:section>

		<sl:newLine skipLine="true" />
		<slf:fieldSet borderHidden="true">
			<slf:lblField name="<%=MonitoraggioForm.TrasferimentoPacs.TRANSFER_NODE%>" colSpan="2" />
		</slf:fieldSet>
		<sl:newLine skipLine="true" />
		<div>
			<input type="hidden" name="table" value="${param.table}" />
		</div>

		<sl:pulsantiera>
			<slf:lblField name="<%=MonitoraggioForm.TrasferimentoPacs.ANNULLA_TRASFERIMENTO%>" width="w25" />
			<slf:lblField name="<%=MonitoraggioForm.TrasferimentoPacs.CONFERMA_TRASFERIMENTO%>" width="w25" />
		</sl:pulsantiera>
	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>