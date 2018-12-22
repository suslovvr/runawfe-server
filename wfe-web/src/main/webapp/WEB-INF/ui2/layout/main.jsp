<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<html:html>
    <tiles:useAttribute name="jsController"/>
    <tiles:useAttribute id="mm" name="mainMenuActiveItem"/>
    <head>
        <tiles:insert attribute="head" ignore="true"/>
    </head>
    <body data-jsController="<%= jsController %>">
        <div class="content">
            <div class="topmenuwrap">
		    <img src="/wfe/images/ui2/logo.png" class="logo" />
	            <img src="/wfe/images/ui2/ico/user.png" class="user" />
	            <img src="/wfe/images/ui2/ico/top-tun.png" alt="Настройки"  title="Настройки" class="tun"/><span>{{currentUser.name}}</span>
            </div>            
            <tiles:insert attribute="content" ignore="true"/>
        </div><div class="footer">Copyright, Cybernix, 2018</div>
        <div class="leftmenuback"><div class="leftmenuwrap">
            <div class="<%= "myTasks".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/myTasks.png" alt="Мои задачи" title="Мои задачи" onclick="wfe.spa.gotoUrl('/myTasks')"/>
            </div>
            <div class="<%= "processDefs".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/processDefs.png" alt="Запустить процесс" title="Запустить процесс" onclick="wfe.spa.gotoUrl('/processDefs')"/>
            </div>
            <div class="<%= "processes".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/processes.png" alt="Запущенные процессы" title="Запущенные процессы" onclick="wfe.spa.gotoUrl('/processes')"/>
            </div>
            <div class="<%= "reports".equals(mm) ? "active" : "" %>">
                <img src="/wfe/images/ui2/ico/mainMenu/reports.png" alt="Отчёты" title="Отчёты" />
            </div>
        </div></div>
    </body>
</html:html>
