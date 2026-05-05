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

<%@page import="org.dcm4che2.data.ElementDictionary"%>
<%@page import="it.eng.dpi.dicom.scu.QRConstants"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link type="text/css" media="screen" rel="stylesheet" href="css/custom/jquery-ui-custom/1.13.3/jquery-ui.min.css"   />
<style type="text/css" title="currentStyle">
@import "css/demo_page.css";

@import "css/demo_table_jui.css";

@import "css/jquery.dataTables.css";
</style>
<link rel="stylesheet" type="text/css" href="css/flexigrid.css">
<script type="text/javascript" src="webjars/jquery/3.7.1/jquery.min.js"></script
<script type="text/javascript" src="webjars/jquery-ui/1.13.3/jquery-ui.min.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="js/flexigrid.js"></script>
<script type="text/javascript" src="js/dpi.js"></script>
<title>Test</title>
</head>
<body style="font-size: 12px; font-family: Arial;">


	<button name="send" value="SendWarning">Invia studi in warning</button>
	<button name="stop" value="StopJobs">Stop Jobs</button>
	<button name="stop" value="StartJobs">Start Jobs</button>
	<button name="stop" value="InfoJobs">Info Jobs</button>
	<h2>TEST SCU</h2>



	Host
	<input type="text" id="host" name="pacshost"></input> Porta
	<input type="text" id="port" name="pacsport"></input> AET
	<input type="text" id="AET" name="pacsAET"></input>
	<button name="send" value="Fill">Fill DCM4CHEE value</button>
	<br />
	<br />
	<h3>STORE SCU</h3>
	Directory studio da trasferire
	<input type="text" id="studyRootDir" name="studyRootDir"></input>
	<button name="send" value="Send">Store SCU</button>
	<button name="sendtx" value="SendTx">Store SCU - TX</button>
	<h3>Query level - Info Model</h3>
	Patient Level - Patient Root
	<input type="radio" name="qrlevel" value="P" checked="checked" />
	<!--             Study Level - Patient Root <input type="radio" name="qrlevel" value="SPR"/>  -->
	Study Level - Study Root
	<input type="radio" name="qrlevel" value="SSR" />
	<br />
	<br /> Usare * per query in like
	<br /> Usare - per query range nelle date (yyyymmdd) Formato ore
	(HHMMSS.fraction)
	<br />
	<br />
	<div id="patientlevel">
		<%
			for (int i : QRConstants.PATIENT_MATCHING_KEYS) {
		%>
		<%=ElementDictionary.getDictionary().nameOf(i)%>

		<input type="text" name="<%=i%>" /> <br />

		<%
			}
		%>

	</div>
	<div id="studylevel">
		<input type="checkbox" name="extkey" id="extkey">Ritorna
		attributi relativi al Patient</input><br />

		<%
			for (int i : QRConstants.STUDY_MATCHING_KEYS) {
		%>
		<%=ElementDictionary.getDictionary().nameOf(i)%>

		<input type="text" name="<%=i%>" /> <br />

		<%
			}
		%>

	</div>
	<br />

	<button name="send" value="Query">Query</button>
	<button name="send" value="Move">Move</button>
	<button name="Send" value="Echo">Echo</button>
	<!--         </form>    -->

	<!--         <h2>ECHO SCU</h2> -->
	<!--         <form title="Test ECHO SCU" action="echo" method="post"  > -->
	<!--             Host <input type="text" id="host"  name="pacshost"></input> -->
	<!--             Porta <input type="text" id="porta"  name="pacsport"></input> -->
	<!--             AET <input type="text" id="AET"  name="pacsAET"></input>  -->
	<!--             <br/><br/>             -->


	<!--         </form>    -->

	<br />
	<br />
	<div id="container">
		<table id="results" cellpadding="0" cellspacing="0" border="0"
			class="display"></table>
	</div>

</body>
</html>
