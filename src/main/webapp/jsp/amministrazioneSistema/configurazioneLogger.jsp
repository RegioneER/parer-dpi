<%@ page import="it.eng.dpi.slite.gen.form.AmministrazioneSistemaForm" pageEncoding="UTF-8"%>
<%@ include file="../../include.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<sl:html>
<sl:head title="Configurazione logger" />
<sl:body>
	<sl:header showChangeOrganizationBtn="false" />
	<sl:menu showChangePasswordBtn="true" />
	<sl:content>
		<slf:messageBox />
		<sl:newLine skipLine="true" />
		<slf:fieldSet borderHidden="false">
			<c:forEach var="currentLogger" items="${requestScope.loggers}" varStatus="status">
				<div class="containerLeft w30">
					
					<label class="slLabel w60">${currentLogger}:&nbsp;</label> <select name="loggerlevel_${status.count}" class="slText w10">
						<option value="" <c:if test="${requestScope.levels[status.count-1] == '' }">selected</c:if>></option>
						<option value="ALL" <c:if test="${requestScope.levels[status.count-1] == 'ALL' }">selected</c:if>>All</option>
						<option value="TRACE" <c:if test="${requestScope.levels[status.count-1] == 'TRACE' }">selected</c:if>>Trace</option>
						<option value="DEBUG" <c:if test="${requestScope.levels[status.count-1] == 'DEBUG' }">selected</c:if>>Debug</option>
						<option value="INFO" <c:if test="${requestScope.levels[status.count-1] == 'INFO' }">selected</c:if>>Info</option>
						<option value="WARN" <c:if test="${requestScope.levels[status.count-1] == 'WARN' }">selected</c:if>>Warn</option>
						<option value="ERROR" <c:if test="${requestScope.levels[status.count-1] == 'ERROR' }">selected</c:if>>Error</option>
						<option value="OFF" <c:if test="${requestScope.levels[status.count-1] == 'OFF' }">selected</c:if>>Off</option>
					</select>

				</div>
				<c:if test="${status.count mod 3 == 0}">
					<sl:newLine skipLine="true" />
				</c:if>
				<div>
					<input type="hidden" name="loggers" value="${currentLogger}" />
				</div>
			</c:forEach>
		</slf:fieldSet>
		<sl:newLine skipLine="true" />
		<sl:pulsantiera>
			<slf:lblField name="<%=AmministrazioneSistemaForm.Logger.APPLICA_LIVELLI%>" colSpan="4" />
		</sl:pulsantiera>
	</sl:content>
	<sl:footer />
</sl:body>
</sl:html>
