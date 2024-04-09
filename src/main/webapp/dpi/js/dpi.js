var oTable;
$(document).ready(start);

function start() {
	$("input:submit, a, button", ".demo").button();
	fillDCM4CHEE();
	$("input:radio").buttonset();
	$("#studylevel").hide();
	$("input:radio").click(function() {
		if (this.value == "P") {
			$("#studylevel").hide();
			$("#patientlevel").show();
			if(oTable){
				
				oTable.fnDestroy(true);
				$('#container').append('<table id="results" cellpadding="0" cellspacing="0" border="0" class="display"></table>');
				oTable = null;
			}
		}
		if (this.value == "SSR" || this.value == "SPR") {
			$("#studylevel").show();
			$("#patientlevel").hide();
			if(oTable){
				
				oTable.fnDestroy(true);
				$('#container').append('<table id="results" cellpadding="0" cellspacing="0" border="0" class="display"></table>');
				oTable = null;
			}
		}
	});
	$("button").click(function() {
		if (this.value == "Query") {
			doAjax("qr.do");
		}
		if (this.value == "SendWarning") {
			doAjax("warn.do");
		}
		if (this.value == "Send") {
			doAjax("send.do");
		}
		if (this.value == "SendTx") {
			doAjax("sendtx.do");
		}
		if (this.value == "Move") {
			doAjax("move.do");
		}
		if (this.value == "Echo") {
			doAjax("echo.do");
		}
		if (this.value == "StopJobs") {
			$.getJSON("stopJobs.do",function(data, success) {
				alert(data);	
			});
		}
		if (this.value == "StartJobs") {
			$.getJSON("startJobs.do",function(data, success) {
				alert(data);	
			});
		}
		if (this.value == "InfoJobs") {
			$.getJSON("infoJobs.do",function(data, success) {
				alert(data);	
			});
		}
		if (this.value == "Fill") {
			fillDCM4CHEE();
			
		}
	});
}

function fillDCM4CHEE(){
	$.getJSON("fillDefPacs.do",function(data, success) {		
		$("#host").val(data[0].hostname);
		$("#port").val(data[0].port);
		$("#AET").val(data[0].aet);
	});
}

function doAjax(page) {
	if(oTable){
//		oTable.fnClearTable();
		oTable.fnDestroy(true);
		$('#container').append('<table id="results" cellpadding="0" cellspacing="0" border="0" class="display"></table>');
		oTable = null;
	}
	var host = $("#host").val();
	var port = $("#port").val();
	var aet = $("#AET").val();
	var qrlevel = $('input[name=qrlevel]:checked').val();
	var params ="";
	var studyRoot = $("#studyRootDir").val();
	if (!host || !port || !aet) {
		alert("Inserire i paramentri di connessione del nodo DICOM da contattare");
		return;
	} 
	$("#patientlevel > input:text").each(function(index, el){
		if(el.value != ""){
			params+="&parname="+el.name+"&parval="+el.value;
		}
	});
	$("#studylevel > input:text").each(function(index, el){
		if(el.value != ""){
			params+="&parname="+el.name+"&parval="+el.value;
		}
	});
	if($("#extkey")[0].checked){
			params+="&extkey=true";
	}
	
//	$("#mixlevel > input:text").each(function(index, el){
//		if(el.value != ""){
//			params+="&parname="+el.name+"&parval="+el.value;
//		}
//	});
	$.getJSON(page + "?pacshost=" + host + "&pacsport=" + port
				+ "&pacsAET=" + aet + "&qrlevel="
				+ qrlevel + params+"&studyRoot="+studyRoot, function(data, success) {
			if(page == 'qr.do' || page== 'move.do'){
				if(qrlevel=='P'){					
					populateGridP(data);
				}else{
					populateGridS(data,$("#extkey")[0].checked);
				}
			}else{
				alert(data.message);				
			}
		}).error(function(d) {
			alert("error"); 
		});
	
};




function populateGridP(data){
	oTable = $('#results').dataTable( {
	"aaData": formatResultsP(eval(data.result)),
	"bJQueryUI": true,
	"aoColumns": [
	    { "sTitle": 'Patient ID'},
		{ "sTitle": 'Patient Name'},
		{ "sTitle": 'Patient BirthDate'},
		{ "sTitle": 'Patient Sex'},
		{ "sTitle": 'Number of Study for Patient'},
		{ "sTitle": 'Number of Series for Patient'},
		{ "sTitle": 'Number of Images for Patient'}
	]
} );	

};

function populateGridS(data,extended){
	
	
	if(extended){
		oTable =	$('#results').dataTable( {	
			"aaData": formatResultsS(eval(data.result),extended),
			"bJQueryUI": true,
			"aoColumns": [
			    {"sTitle": 'Patient ID'},
			    {"sTitle": 'Patient Name'},
			    {"sTitle": 'Patient BirthDate'},
			    {"sTitle": 'Patient Sex'},
				{"sTitle": 'Study Instance UID' },
				{"sTitle": 'Study ID' },
				{"sTitle": 'Study DateTime' },
				{"sTitle": 'Accession Number' },		
				{"sTitle": 'Number of Images in Study'}
			]
		} );
	}else{	
		
		
		oTable =	$('#results').dataTable( {
			"aaData": formatResultsS(eval(data.result),extended),
			"bJQueryUI": true,
			"aoColumns": [		   
				{"sTitle": 'Study Instance UID' },
				{"sTitle": 'Study ID' },
				{"sTitle": 'Study DateTime' },
				{"sTitle": 'Accession Number' },
				{"sTitle": 'Number of Series in Study' },			
				{"sTitle": 'Number of Images in Study'}
			]
		} );
		
	}
	};

function formatResultsP(data){
	var rows = Array();
	for (var i = 0; i < data.length; i++) {
		var item = data[i];
		rows.push([item.patientID,
		                   item.patientName,
		                   new Date(item.patientBirthDate),
		                   item.patientSex,
		                   item.numberOfPatientRelatedStudies,
		                   item.numberOfPatientRelatedSeries,
		                   item.numberOfPatientRelatedInstances]);
	}
	return rows;
}

function formatResultsS(data,extended){

	var rows = Array();
	for (var i = 0; i < data.length; i++) {
	var item = data[i];
	if(extended){
		rows.push([item.patientID,
                   item.patientName,
                   new Date(item.patientBirthDate),
                   item.patientSex,
		           		   item.studyInstanceUID,
		                   item.studyID,
		                   new Date(item.studyDateTime),
		                   item.accessionNumber,		
//		                   item.numberOfStudyRelatedSeries,
		                   item.numberOfStudyRelatedInstances]);
	}else{
		rows.push([item.studyInstanceUID,
                   item.studyID,
                   new Date(item.studyDateTime),
                   item.accessionNumber,
                   item.numberOfStudyRelatedSeries,
                   item.numberOfStudyRelatedInstances]);
		
	}
}
return rows;
}





//function populateGridP(data){
//	$("#results").flexigrid({	
//		dataType: 'json',		
//		colModel : [
//			{display: 'Patient ID', name : 'patientID', width : 40, sortable : true, align: 'left'},
//			{display: 'Patient Name', name : 'patientName', width : 180, sortable : true, align: 'left'},
//			{display: 'Patient BirthDate', name : 'patientBirthDate', width : 120, sortable : true, align: 'left'},
//			{display: 'Patient Sex', name : 'patientSex', width : 40, sortable : true, align: 'left'},
//			{display: 'NumberOfPatientRelatedStudies', name : 'numberOfPatientRelatedStudies', width : 140, sortable : true, align: 'right'},
//			{display: 'NumberOfPatientRelatedSeries', name : 'numberOfPatientRelatedSeries', width : 140, sortable : true, align: 'right'},
//			{display: 'NumberOfPatientRelatedInstances', name : 'numberOfPatientRelatedInstances', width : 140, sortable : true, align: 'right'}
//			],
//		searchitems : [
//			{display: 'patientID', name : 'patientID'},
//			{display: 'patientName', name : 'patientName', isdefault: true}
//			],
//		sortname: "patientName",
//		sortorder: "asc",
//		usepager: true,
//		title: 'Patients',
////		useRp: true,
////		rp: 15,
//		showTableToggleBtn: true,
//		width: 1100,		
//		height: 400
//	});
//
//	$("#results").flexAddData(formatResultsP(eval(data.result)));
//	$("#results").flexReload();
//}
//
//function populateGridS(data){
//	$("#results").flexigrid({	
//		dataType: 'json',		
//		colModel : [
//			{display: 'Study Instance UID', name : 'studyInstanceUID', width : 40, sortable : true, align: 'left'},
//			{display: 'Study ID', name : 'studyID', width : 180, sortable : true, align: 'left'},
//			{display: 'Study DateTime', name : 'studyDateTime', width : 120, sortable : true, align: 'left'},
//			{display: 'AccessionNumber', name : 'accessionNumber', width : 40, sortable : true, align: 'left'},
//			{display: 'numberOfStudyRelatedSeries', name : 'numberOfStudyRelatedSeries', width : 140, sortable : true, align: 'right'},			
//			{display: 'numberOfStudyRelatedInstances', name : 'numberOfStudyRelatedInstances', width : 140, sortable : true, align: 'right'}
//			],
//		searchitems : [
//			{display: 'studyID', name : 'studyID'},
//			{display: 'studyInstanceUID', name : 'studyInstanceUID', isdefault: true}
//			],
//		sortname: "studyInstanceUID",
//		sortorder: "asc",
//		usepager: true,
//		title: 'Studies',
////		useRp: true,
////		rp: 15,
//		showTableToggleBtn: true,
//		width: 1100,		
//		height: 400
//	});
//
//	$("#results").flexAddData(formatResultsS(eval(data.result)));
//	$("#results").flexReload();
//}
//
//function formatResultsS(data){
////	
//    var rows = Array();
//    for (var i = 0; i < data.length; i++) {
//        var item = data[i];
//        rows.push({ cell: [item.studyInstanceUID,
//                           item.studyID,
//                           new Date(item.studyDateTime),
//                           item.accessionNumber,
//                           item.numberOfStudyRelatedSeries,
//                           item.numberOfStudyRelatedInstances]});
//    }
//    return {
//        total: data.length,
//        page: 1,
//        rows: rows
//    };
//
//
//}
//
//function formatResultsP(data){
////	
//    var rows = Array();
//    for (var i = 0; i < data.length; i++) {
//        var item = data[i];
//        rows.push({ cell: [item.patientID,
//                           item.patientName,
//                           new Date(item.patientBirthDate),
//                           item.patientSex,
//                           item.numberOfPatientRelatedStudies,
//                           item.numberOfPatientRelatedSeries,
//                           item.numberOfPatientRelatedInstances]});
//    }
//    return {
//        total: data.length,
//        page: 1,
//        rows: rows
//    };


