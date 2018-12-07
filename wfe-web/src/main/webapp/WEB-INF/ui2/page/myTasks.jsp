<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/WEB-INF/ui2/layout/main.jsp" flush="true">
    <tiles:put name="jsController" value="wfe.myTasks"/>
    <tiles:put name="mainMenuActiveItem" value="myTasks"/>
    <tiles:put name="head" type="string">
        <title>Задачи</title>
        <script type="text/javascript" src="/wfe/js/ui2/myTasks.js"></script>
    </tiles:put>
    <tiles:put name="content" type="string">
        <div class="filters">
            <img src="/wfe/images/ui2/ico/filter.png" onmouseover="this.title.attr=filters" title="" /><!-- пока на hover, надо на click -->
            <div>
                Пример (пока на hover, надо на click)<br /><input type="checkbox" /> Ответственный<br /><input type="checkbox" />Дата начала процесса
            </div>
        </div>
        <p class="pagename">Мои задачи</p>
        <table class="work task"><tbody>
            <tr>
                <td><input type="checkbox"/></td>
                <td><a href="" title="Сортировка">Задача</a></td>
                <td><a href="" title="Сортировка">Описание</a></td>
                <td><a href="" title="Сортировка">Процесс</a></td>
                <td><a href="" title="Сортировка">№ экземпляра</a></td>
                <td>Время окончания</td>
                <td><a href="" title="Сортировка">Создана</a></td>
            </tr>
            <tr v-for="o in rows">
                <td><input type="checkbox"/></td>
                <td>{{o.name}}</td>
                <td class="description">{{o.description}}</td>
                <td>{{o.definitionName}}</td>
                <td>{{o.id}}</td>
                <td>{{o.creationDate}}</td>
                <td>{{o.deadlineDate}}</td>
            </tr>
        </tbody></table>
        <div class="act"><a href="" class="button">Взять на выполнение</a> Всего: {{count}}</div>
    </tiles:put>
</tiles:insert>
