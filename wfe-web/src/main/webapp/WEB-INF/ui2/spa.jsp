<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="ru.runa.common.Version" %>
<!DOCTYPE html>
<html>
<head>
<title>RunaWFE</title>
<script type="text/javascript" src="/wfe/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="/wfe/js/vue-2.5.17.min.js"></script>
<script type="text/javascript" src="/wfe/js/ui2/spa.js"></script>
<style type="text/css">/* <![CDATA[ */
    html, body, #spa-wait, #spa-error {
        width: 100%;
        height: 100%;
        padding: 0;
        margin: 0;
    }
    #spa-wait {
        position: fixed;
        z-index: 1000;
        background-color: white;
        opacity: 0.7;
        font-size: 32px;
    }
    #spa-wait td {
        text-align: center;
        vertical-align: middle;
    }
    #spa-error {
        padding: 20px 40px;
        display: none;
    }
/* ]]> */</style>
</head>

<body onload="wfeSpa.init('<%=Version.getHash()%>')">
<table id="spa-wait"><tr><td>Please wait...</td></tr></table>
<div id="spa-error">
    <h1>Error</h1>
    <p>Could not load requested page.</p>
    <p>TODO Show error message, retry, goto home.</p>
</div>
<div id="spa-body"></div>
</body>
</html>
