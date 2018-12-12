<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/WEB-INF/ui2/layout/main.jsp" flush="true">
    <tiles:put name="jsController" value="wfe.processes"/>
    <tiles:put name="mainMenuActiveItem" value="processes"/>
    <tiles:put name="head" type="string">
        <title>Задачи</title>
        <script type="text/javascript" src="/wfe/js/ui2/processes.js"></script>
    </tiles:put>
    <tiles:put name="content" type="string">
        <div class="filters">
            <img src="/wfe/images/ui2/ico/filter.png" onmouseover="this.title.attr=filters" title="" /><!-- пока на hover, надо на click -->
            <div>
				<input type="checkbox" />Ссылка на схему<br />
				<input type="checkbox" />Ответственный<br />
				<input type="checkbox" />Версия<br />
            </div>
        </div>
        <p class="pagename">Запущенные процессы</p>
        <table class="work process"><tbody>
            <tr>
                <td><input type="checkbox"/></td>
                <td><a href="" title="Сортировка">Процесс</td>
                <td><a href="" title="Сортировка">Запущен</a></td>
                <td><a href="" title="Сортировка">Окончен</a></td>
				<td><a href="" title="Сортировка">№</a></td>
				<td><a href="" title="Сортировка">Статус</a></td>
            </tr>
            <tr v-for="o in rows">
                <td><input type="checkbox"/></td>
                <td>{{o.name}}</td>
                <td>{{o.startDate}}</td>
				<td>{{o.endDate}}</td>
				<td>{{o.id}}</td>
				<td>{{o.executionStatus}}</td>
            </tr>
        </tbody></table>
    </tiles:put>
</tiles:insert>
