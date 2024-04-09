<%@ page import="it.eng.dpi.slite.gen.form.MonitoraggioForm" pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>

<sl:html>
<sl:head title="Ricerca Diario" />
<sl:body>
	<sl:header showChangeOrganizationBtn="false" />
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<sl:newLine skipLine="true" />

		<sl:contentTitle title="RICERCA DIARIO" />

		<slf:fieldSet borderHidden="false">
			<!-- piazzo i campi del filtro di ricerca -->
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.RICERCA_OGNI_SESSIONE%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.TI_STATO%>" colSpan="2" />
			<sl:newLine skipLine="true" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.NODO_DICOM%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.ACCESSION_NUMBER%>" colSpan="2" />
			<sl:newLine skipLine="true" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.DT_STUDY_DA%>" controlWidth="w70" colSpan="1" />
			<slf:doubleLblField name="<%=MonitoraggioForm.RicercaDiario.ORE_DT_STUDY_DA%>" name2="<%=MonitoraggioForm.RicercaDiario.MINUTI_DT_STUDY_DA%>"
				controlWidth="w20" controlWidth2="w20" colSpan="1" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.DT_STUDY_A%>" controlWidth="w70" colSpan="1" />
			<slf:doubleLblField name="<%=MonitoraggioForm.RicercaDiario.ORE_DT_STUDY_A%>" name2="<%=MonitoraggioForm.RicercaDiario.MINUTI_DT_STUDY_A%>"
				controlWidth="w20" controlWidth2="w20" colSpan="1" />
			<sl:newLine skipLine="true" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.DT_PRESA_IN_CARICO_DA%>" controlWidth="w70" colSpan="1" />
			<slf:doubleLblField name="<%=MonitoraggioForm.RicercaDiario.ORE_DT_PRESA_IN_CARICO_DA%>"
				name2="<%=MonitoraggioForm.RicercaDiario.MINUTI_DT_PRESA_IN_CARICO_DA%>" controlWidth="w20" controlWidth2="w20" colSpan="1" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.DT_PRESA_IN_CARICO_A%>" controlWidth="w70" colSpan="1" />
			<slf:doubleLblField name="<%=MonitoraggioForm.RicercaDiario.ORE_DT_PRESA_IN_CARICO_A%>"
				name2="<%=MonitoraggioForm.RicercaDiario.MINUTI_DT_PRESA_IN_CARICO_A%>" controlWidth="w20" controlWidth2="w20" colSpan="1" />
			<sl:newLine skipLine="true" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.PAZIENTE%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.FILTRO_PAZIENTE%>" colSpan="2" />
			<sl:newLine />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.SESSO_PAZIENTE%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.DT_NASCITA_PAZIENTE%>" colSpan="2" />
			<sl:newLine />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.ID_PAZIENTE%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.ISSUER_ID_PAZIENTE%>" colSpan="2" />
			<sl:newLine />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.MODALITY_STUDY%>" colSpan="4" />
			<sl:newLine />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.STUDY_UID%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.DCM_HASH%>" colSpan="2" />
		</slf:fieldSet>
		<sl:newLine skipLine="true" />

		<sl:pulsantiera>
			<!-- piazzo i bottoni di ricerca ed inserimento -->
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.RICERCA_DIARIO%>" width="w25" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.CHIUSURA_MULTIPLE_WARNINGS%>" width="w25" />
			<slf:lblField name="<%=MonitoraggioForm.RicercaDiario.VERSAMENTO_MULTIPLE_WARNINGS%>" width="w25" />
		</sl:pulsantiera>
		<sl:newLine skipLine="true" />

		<!--  piazzo la lista con i risultati -->
		<slf:list abbrLongList="true" name="<%= MonitoraggioForm.StudiList.NAME%>" />
		<slf:listNavBar name="<%= MonitoraggioForm.StudiList.NAME%>" />

	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>