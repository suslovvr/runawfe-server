<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="ru.runa.wfe.commons.dbpatch.InitializerLogic" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<!DOCTYPE html>
<html:html>
    <% String wss = InitializerLogic.getWhenStartedString(); %>
    <head>
        <title>RunaWFE</title>
        <script type="text/javascript" src="../js/jquery-3.3.1.min.js"></script>
        <script type="text/javascript" src="../js/vue-2.5.17.min.js"></script>
        <script type="text/javascript" src="../js/ui2/spa.js?.=<%= wss %>"></script>
        <link rel="stylesheet" type="text/css" href="../css/ui2/all.css?.=<%= wss %>"/>
    </head>
    <body onload="wfe.spa.onLoad('<%= wss %>')">
        <table id="spa-wait"><tr><td>Пожалуйста, подождите...</td></tr></table>
        <div id="spa-error">
            <h1>Ошибка</h1>
            <p id="spa-error-msg"></p>
        </div>
        <div id="spa-body"></div>
    </body>
</html:html>
