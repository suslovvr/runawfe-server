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
    <div class="one-contentback" onLoad="top-ico.onLoad()">	
        <div class="top-ico" id="top-ico">
        <img src="/wfe/images/ui2/ico/filter.png" title="Фильтры" id="but-filter" /><img src="/wfe/images/ui2/ico/view.png" id="but-view" title="Вид" /><img src="/wfe/images/ui2/ico/info.png" id="but-info" title="Информация" />
        <div id="view"><img src='/wfe/images/ui2/ico/close.png' class='close-ti' />
		<h3>Вид</h3><br />
		<input type="checkbox" id="checkbox-isp" /><label for="checkbox-isp"> Исполнитель</label><br />
		<input type="checkbox" id="checkbox-d1"/><label for="checkbox-d1"> Дата запуска процесса</label><br />
		</div>
		<div id="info"><img src='/wfe/images/ui2/ico/close.png' class='close-ti' />
		<h3>Информация</h3><br />
	    <p><div class="task1"></div>Установленный срок задачи подходит к концу<br clear="all" /></p>
		<p><div class="task2"></div>Задача не выполнена в установленный срок<br clear="all" /></p>
		<p><div class="task3"></div>Задача получена по эскалации<br clear="all" /></p>
		<p><div class="task4"></div>Задача получена по замещению<br clear="all" /></p>
		</div>
	    </div>
        <h1>Мои задачи</h1>
        <div class="work">
        <table class="task"><tbody>
            <tr>
                <td colspan="9"></td>
            </tr>
            <tr>
                <td><div class="filter"></div></td>
                <td class="chb"><input type="checkbox"/><div class="filter"></div></td>
                <td><p>Задача</p><div class="filter"><input type="text" value="Содержит" /></div></td>
                <td><p>Описание</p><div class="filter"><input type="text" value="Содержит" /></div></td>
                <td><p>Процесс</p><div class="filter"><input type="text" value="Содержит" /></div></td>
                <td class="num"><p>№экз</p><div class="filter"><input type="text" value="от"/><br /><input type="text" value="до" /></div></td>
                <td class="data"><p>Дата создания</p><div class="filter"><input type="date" value="от" /><br /><input type="date" value="до" /></div></td>
                <td class="data"><p>Дата выполнения</p><div class="filter"><input type="date" value="от" /><br /><input type="date" value="до" /></div></td>
                <td><div class="filter"></div></td>
            </tr>
            <tr>
                <td colspan="9"></td>
            </tr>
            <tr v-for="o in rows">
                <td></td>
                <td class="chb"><input type="checkbox"/></td>
                <td>{{o.name}}</td>
                <td class="description">{{o.description}}</td>
                <td>{{o.definitionName}}</td>
                <td class="num">{{o.id}}</td>
                <td class="data">{{o.creationDate}}</td>
                <td class="data">{{o.deadlineDate}}</td>
                <td></td>
            </tr>
        </tbody></table>
        <div class="act"><a href="" class="button">Взять на выполнение</a> Всего: {{count}}</div>
        </div>
    </div>
    </tiles:put>
</tiles:insert>
