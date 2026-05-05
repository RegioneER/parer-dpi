<%--
 Engineering Ingegneria Informatica S.p.A.

 Copyright (C) 2023 Regione Emilia-Romagna
 <p/>
 This program is free software: you can redistribute it and/or modify it under the terms of
 the GNU Affero General Public License as published by the Free Software Foundation,
 either version 3 of the License, or (at your option) any later version.
 <p/>
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Affero General Public License for more details.
 <p/>
 You should have received a copy of the GNU Affero General Public License along with this program.
 If not, see <https://www.gnu.org/licenses/>.
 --%>

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
