<%@ page import="it.eng.dpi.slite.gen.form.AmministrazioneSistemaForm"
	pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>

<sl:html>
<sl:head title="Gestione Job">
	<script type="text/javascript">
		$(document)
				.ready(
						function() {
							$('.confermaAttivazioneConfrontoBox')
									.dialog(
											{
												autoOpen : true,
												width : 600,
												modal : true,
												closeOnEscape : true,
												resizable : false,
												dialogClass : "alertBox",
												buttons : {
													"Ok" : function() {
														$(this).dialog("close");
														var nmJob = $(
																"#nmConfrontoJob")
																.val();
														window.location = "AmministrazioneSistema.html?operation=confermaAttivaConfrontoJob&name="
																+ nmJob;
													},
													"Annulla" : function() {
														$(this).dialog("close");
													}
												}
											});
						});
	</script>
</sl:head>
<sl:body>
	<sl:header showChangeOrganizationBtn="false" />
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<c:if test="${!empty requestScope.confermaAttivazioneConfrontoBox}">
			<div class="messages confermaAttivazioneConfrontoBox ">
				<ul>
					<li class="message info ">Desideri attivare il job
						${fn:escapeXml(requestScope.dsConfrontoJob)} ?</li>
				</ul>
			</div>
		</c:if>
		<div>
			<input type="hidden" id="nmConfrontoJob" name="nmConfrontoJob"
				value="${fn:escapeXml(requestScope.nmConfrontoJob)}" />
		</div>

		<sl:contentTitle title="GESTIONE JOB" />

		<sl:pulsantiera>
			<slf:buttonList
				name="<%=AmministrazioneSistemaForm.JobButtonList.NAME%>" />
		</sl:pulsantiera>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.ObjectCreatorCoordinatorQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorCoordinatorQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorCoordinatorQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorCoordinatorQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorCoordinatorQJob.START_OBJECT_CREATOR_COORDINATOR_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorCoordinatorQJob.STOP_OBJECT_CREATOR_COORDINATOR_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.ObjectCreatorQJob1.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob1.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob1.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob1.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob1.START_OBJECT_CREATOR_QJOB1%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob1.STOP_OBJECT_CREATOR_QJOB1%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.ObjectCreatorQJob2.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob2.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob2.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob2.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob2.START_OBJECT_CREATOR_QJOB2%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob2.STOP_OBJECT_CREATOR_QJOB2%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.ObjectCreatorQJob3.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob3.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob3.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob3.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob3.START_OBJECT_CREATOR_QJOB3%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCreatorQJob3.STOP_OBJECT_CREATOR_QJOB3%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.ObjectSenderQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectSenderQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectSenderQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectSenderQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectSenderQJob.START_OBJECT_SENDER_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectSenderQJob.STOP_OBJECT_SENDER_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.FTPTransfertQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.FTPTransfertQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.FTPTransfertQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.FTPTransfertQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.FTPTransfertQJob.START_FTPTRANSFERT_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.FTPTransfertQJob.STOP_FTPTRANSFERT_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.UpdateSOPClassQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.UpdateSOPClassQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.UpdateSOPClassQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.UpdateSOPClassQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.UpdateSOPClassQJob.START_UPDATE_SOPCLASS_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.UpdateSOPClassQJob.STOP_UPDATE_SOPCLASS_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.QueryPacsPingQJobD.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobD.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobD.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobD.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobD.START_QUERY_PACS_PING_QJOB_D%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobD.STOP_QUERY_PACS_PING_QJOB_D%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.QueryPacsPingQJobW.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobW.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobW.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobW.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobW.START_QUERY_PACS_PING_QJOB_W%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobW.STOP_QUERY_PACS_PING_QJOB_W%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.QueryPacsPingQJobM.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobM.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobM.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobM.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobM.START_QUERY_PACS_PING_QJOB_M%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.QueryPacsPingQJobM.STOP_QUERY_PACS_PING_QJOB_M%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.PrelievoFTPQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.PrelievoFTPQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.PrelievoFTPQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.PrelievoFTPQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.PrelievoFTPQJob.START_PRELIEVO_FTPQJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.PrelievoFTPQJob.STOP_PRELIEVO_FTPQJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.NotificaPrelievoQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.NotificaPrelievoQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.NotificaPrelievoQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.NotificaPrelievoQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.NotificaPrelievoQJob.START_NOTIFICA_PRELIEVO_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.NotificaPrelievoQJob.STOP_NOTIFICA_PRELIEVO_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.NotificaInAttesaPrelievoQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.NotificaInAttesaPrelievoQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.NotificaInAttesaPrelievoQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.NotificaInAttesaPrelievoQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.NotificaInAttesaPrelievoQJob.START_NOTIFICA_IN_ATTESA_PRELIEVO_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.NotificaInAttesaPrelievoQJob.STOP_NOTIFICA_IN_ATTESA_PRELIEVO_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.PuliziaInAttesaFileQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.PuliziaInAttesaFileQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.PuliziaInAttesaFileQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.PuliziaInAttesaFileQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.PuliziaInAttesaFileQJob.START_PULIZIA_IN_ATTESA_FILE_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.PuliziaInAttesaFileQJob.STOP_PULIZIA_IN_ATTESA_FILE_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.ObjectCopyQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCopyQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCopyQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.ObjectCopyQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCopyQJob.START_OBJECT_COPY_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.ObjectCopyQJob.STOP_OBJECT_COPY_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

		<slf:fieldSet
			legend="<%=AmministrazioneSistemaForm.GenericObjectCreatorQJob.DESCRIPTION%>">
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.GenericObjectCreatorQJob.ATTIVO%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.GenericObjectCreatorQJob.DT_REG_LOG_JOB_INI%>"
				colSpan="4" />
			<sl:newLine />
			<slf:lblField
				name="<%=AmministrazioneSistemaForm.GenericObjectCreatorQJob.DT_PROSSIMA_ATTIVAZIONE%>"
				colSpan="4" />
			<sl:newLine skipLine="true" />
			<sl:pulsantiera>
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.GenericObjectCreatorQJob.START_GENERIC_OBJECT_CREATOR_QJOB%>"
					colSpan="2" />
				<slf:lblField
					name="<%=AmministrazioneSistemaForm.GenericObjectCreatorQJob.STOP_GENERIC_OBJECT_CREATOR_QJOB%>"
					colSpan="2" />
			</sl:pulsantiera>
		</slf:fieldSet>

	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>
