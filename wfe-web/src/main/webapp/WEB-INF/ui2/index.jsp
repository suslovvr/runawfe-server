<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="ru.runa.common.Version" %>
<!DOCTYPE html>
<html>
<head>
    <title>RunaWFE</title>
    <script type="text/javascript" src="/wfe/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="/wfe/js/vue-2.5.17.min.js"></script>
    <script type="text/javascript" src="/wfe/js/ui2/spa.js"></script>
</head>
<body onload="spaInit('<%=Version.getHash()%>')">
<div id="spa-splash">
    <h1>RunaWFE is loading...</h1>
    <p>Please wait.</p>
    <p>If stuck here, check JavaScript is enabled in your browser.</p>
</div>
<div id="spa-wait">Please wait...</div>
<div id="spa-error">
    <p>Could not load requested page.</p>
    <p>TODO Show error message, retry, goto home</p>
</div>
<div id="spa-body"></div>
</body>
</html>
