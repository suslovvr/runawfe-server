<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<html:html>
    <tiles:useAttribute name="onload"/>
    <tiles:useAttribute id="mm" name="mainMenuActiveItem"/>
    <head>
        <tiles:insert attribute="head" ignore="true"/>
    </head>
    <body onload="<%=onload%>">
        <div class="content"><div class="contentback">
            <tiles:insert attribute="content" ignore="true"/>
        </div></div>
        <div class="leftmenuback"><div class="leftmenuwrap">
            <div class="<%= "tasks".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/tasks.png" alt="Мои задачи" title="Мои задачи" onclick="wfe.spa.gotoUrl('/tasks')"/>
            </div>
            <div class="<%= "processDefs".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/processDefs.png" alt="Запустить процесс" title="Запустить процесс" onclick="wfe.spa.gotoUrl('/processDefs')"/>
            </div>
            <div class="<%= "processes".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/processes.png" alt="Запущенные процессы" title="Запущенные процессы" /></div>
            <div class="<%= "reports".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/reports.png" alt="Отчёты" title="Отчёты" />
            </div>
            <div>
                <img src="/wfe/images/ui2/ico/start1dark.png" alt="Развернуть" id="leftsizechange" title="Развернуть" />
            </div>
        </div></div>
        <div class="topmenuwrap">
            <img src="/wfe/images/ui2/ico/user.png" />
            <span>Имя пользователя</span>
        </div>
    </body>
</html:html>
