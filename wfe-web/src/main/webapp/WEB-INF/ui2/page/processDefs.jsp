<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/WEB-INF/ui2/layout/main.jsp" flush="true">
    <tiles:put name="onload" value="wfe.processDefs.onLoad()"/>
    <tiles:put name="mainMenuActiveItem" value="processDefs"/>
    <tiles:put name="head" type="string">
        <title>Задачи</title>
        <script type="text/javascript" src="/wfe/js/ui2/processDefs.js"></script>
    </tiles:put>
    <tiles:put name="content" type="string">
        <div class="filters">
            <img src="/wfe/images/ui2/ico/filter.png" onmouseover="this.title.attr=filters" title="" /><!-- пока на hover, надо на click -->
            <div>
                Пример (пока на hover, надо на click)<br /><input type="checkbox" /> Владелец процесса<br /><input type="checkbox" />Дата загрузки
            </div>
        </div>
        <p class="pagename">Определение процессов</p>
        <table class="work def"><tbody>
            <tr>
                <td><input type="checkbox"/></td>
                <td>Запустить</td>
                <td><a href="" title="Сортировка">Имя</a></td>
                <td><a href="" title="Сортировка">Тип процесса</a></td>
                <td>Описание / (Длинное описание)</td>
            </tr>
            <tr v-for="o in rows">
                <td><input type="checkbox"/></td>
                <td><img src="/wfe/images/ui2/ico/start1_dark.png" alt="Запустить" class="start"/></td>
                <td>{{o.name}}</td>
                <td>{{o.category}}</td>
                <td class="description">{{o.description}}</td>
            </tr>
        </tbody></table>
    </tiles:put>
</tiles:insert>
