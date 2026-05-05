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
<sl:head title="Confronto Pacs Pre-Ingest" />
<sl:body>
	<sl:header showChangeOrganizationBtn="false"/>
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<sl:newLine skipLine="true" />

		<sl:contentTitle title="CONFRONTO PACS PRE-INGEST" />

		<slf:fieldSet borderHidden="true">
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.DT_CONFRONTO_DA%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.DT_CONFRONTO_A%>" colSpan="2" />
		</slf:fieldSet>
		<sl:newLine skipLine="true" />
		
		<slf:list name="<%= MonitoraggioForm.ConfrontiList.NAME%>" />

		<sl:newLine skipLine="true" />
		<slf:fieldSet borderHidden="true">
			<p>Confermare l'avvio del confronto?</p>
		</slf:fieldSet>

		<sl:pulsantiera>
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.CONFERMA_CONFRONTO%>" width="w25" />
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.ANNULLA_CONFRONTO%>" width="w25" />
		</sl:pulsantiera>
	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>
