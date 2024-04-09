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