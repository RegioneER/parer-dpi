<%--
    Document   : index
    Created on : 30-mag-2011, 14.48.16
    Author     : Quaranta_M
--%>
<%@page import="java.util.Properties" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    Properties prop = new Properties();
    prop.load(getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
    String appVersion = prop.getProperty("App-Version");
    String appBuildDate = prop.getProperty("App-BuildDate");
    String appBuildNum = prop.getProperty("App-BuildNumber");
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DPI Lab</title>

        <style>
            h1 {
                font: 25px verdana,verdana, arial;
                margin: 20px 0px 40px 10px;
                padding: 0;
                border-collapse: collapse;
                text-align: left;
                color: #333;
                line-height: 19px;
            }
            h2 {
                font: 17px verdana,verdana, arial;
                margin: 20px 0px 40px 10px;
                padding: 0;
                border-collapse: collapse;
                text-align: left;
                color: #333;
                line-height: 19px;
            }
            .green{
                background: green;
                padding:0px 6px;
                border:1px solid #3b6e22;
                height:24px;
                line-height:24px;
                color:#FFFFFF;
                font-size:12px;
                margin:20px 10px 0 20px;
                display:inline-block;
                text-decoration:none;
            }
            div.box .input-text{
                border:1px solid #3b6e22;
                color:#666666;
            }

            div.box label{
                display:block;
                margin-bottom:10px;
                color:#555555;
            }

            div.box label span{
                font: 11px verdana,verdana, arial;
                display:block;
                float:left;
                padding-right:6px;
                width:150px;
                text-align:left;
                font-weight:bold;
            }
            table {
                font: 11px verdana,verdana, arial;
                margin: 0;
                padding: 0;
                border-collapse: collapse;
                text-align: left;
                color: #333;
                line-height: 19px;
            }

            caption {
                font-size: 14px;
                font-weight: bold;
                margin-bottom: 20px;
                text-align: left;
                text-transform: uppercase;
            }

            td {
                margin: 0;
                padding: 10px 10px;
                border: 1px dotted #f5f5f5;
            }

            th {
                font-weight: normal;
                text-transform: uppercase;
            }

            thead tr th {
                background-color: #575757;
                padding:  20px 10px;
                color: #fff;
                font-weight: bold;
                border-right: 2px solid #333;
                text-transform: uppercase;
                text-align:center;
            }

            tfoot tr th, tfoot tr td {
                background-color: transparent;
                padding:  20px 10px;
                color: #ccc;
                border-top: 1px solid #ccc;
            }

            tbody tr th {
                padding: 10px 10px;
                border-bottom: 1px dotted #fafafa;
            }

            tr {
                background-color: #FBFDF6;
            }
            tr.odd {
                background-color: #EDF7DC;
            }

            tr:hover {
            }

            tr:hover td, tr:hover td a, tr:hover th a {
                color: #a10000;
            }

            td:hover {
            }

            tr:hover th a:hover {
                background-color: #F7FBEF;
                border-bottom: 2px solid #86C200;
            }

            table a {
                color: #608117;
                background-image: none;
                text-decoration: none;
                border-bottom: 1px dotted #8A8F95;
                padding: 2px;
                padding-right: 12px;
            }

            table a:hover {
                color: #BBC4CD;
                background-image: none;
                text-decoration: none;
                border-bottom: 3px solid #333;
                padding: 2px;
                padding-right: 12px; color: #A2A2A2;
            }

            table a:visited {
                text-decoration: none;
                border-bottom: 1px dotted #333;
                text-decoration: none;
                padding-right: 12px; color: #A2A2A2;
            }

            table a:visited:hover {
                background-image: none;
                text-decoration: none;
                border-bottom: 3px solid #333;
                padding: 2px;
                padding-right: 12px; color: #A2A2A2;
            }

        </style>


    </head>
    <body>
        <h1>DPI Lab</h1>
        <h2>v. <% out.println(appVersion);%></h2>       
        <a href="ProvaUp.jsp">Lancia la pagina di versamento sincrono Sacer (max 15 file)...</a>
        <br/>
        <a href="ProvaUpPre.jsp">Lancia la pagina di versamento sincrono Sacer Pre (max 15 file)...</a>
        <br/>
        <a href="ProvaUpBeta.jsp">Lancia la pagina di versamento sincrono Sacer Beta (max 15 file)...</a>
        <br/>
        <a href="ProvaUpSvil.jsp">Lancia la pagina di versamento sincrono Sacer Sviluppo (max 15 file)...</a>
        <br/>
    </body>
</html>
