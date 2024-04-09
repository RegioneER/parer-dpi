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
			<p>Selezionare le date in modo che:</p>
			<ul>
				<li>la differenza tra DATA CONFRONTO DA e DATA CONFRONTO A non sia più di 45 giorni</li>
				<li>DATA CONFRONTO A sia antecedente di almeno 7 giorni alla data odierna</li>
				<li>DATA CONFRONTO DA sia successiva a ${requestScope.queryPacsStartDate}</li>
			</ul>
		</slf:fieldSet>

		<slf:fieldSet borderHidden="true">
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.DT_CONFRONTO_DA%>" colSpan="2" />
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.DT_CONFRONTO_A%>" colSpan="2" />
		</slf:fieldSet>
		<sl:newLine skipLine="true" />

		<sl:pulsantiera>
			<slf:lblField name="<%=MonitoraggioForm.ConfrontoPacsPreIngest.START_CONFRONTO%>" width="w25" />
		</sl:pulsantiera>
	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>