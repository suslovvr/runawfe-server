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
        <div class="one-contentback">
	<div class="top-ico" id="top-ico">
	    <img src="/wfe/images/ui2/ico/filter.png" title="Фильтры" id="but-filter"/><img src="/wfe/images/ui2/ico/view.png" title="Вид" id="but-view" /><img src="/wfe/images/ui2/ico/info.png" title="Информация" id="but-info" />
	    <div id="view"><form class="pro">
	        <h3>Вид</h3><br />
		<input type="checkbox" id="checkbox-shema" disabled="disabled" /><label for="checkbox-shema">Cсылка на схему</label><br />
		<input type="checkbox" id="checkbox-otv" /><label for="checkbox-otv">Ответственный</label><br />
		<input type="checkbox" id="checkbox-ver" /><label for="checkbox-ver">Версия</label><br /></form>
	     </div>
	     <div id="info"><form>
		<h3>Информация</h3><br />
		<input type="checkbox" id="checkbox-otv" /><label for="checkbox-otv"> Ответственный</label><br /><input type="checkbox" id="checkbox-creationdate" /><label for="checkbox-creationdate">Дата начала процесса</label></form>
	     </div>
	</div>          
        <h1>Запущенные процессы</h1>
        <div class="work">
	<form>
	<table class="task"><tbody>
	    <tr>
	        <td colspan="7"></td>
	    </tr>
            <tr>
                <td><div class="filter"></div></td>
                <td><p>Процесс</p><div class="filter"><input type="text" value="Содержит" /></div></td>
		<td class="data"><p>Запущен</p><div class="filter"><input type="date" /><br /><input type="date" /></div></td>
		<td class="data"><p>Окончен</p><div class="filter"><input type="date" /><br /><input type="date" /></div></td>
          	<td class="num"><p>№</p><div class="filter"><input type="text" value="от"/><br /><input type="text" value="до" /></div></td>
		<td><p>Статус</p><div class="filter"><select><option>Активен</option><option>Завершён</option></select></div></td>
		<td><div class="filter"></div></td>
            </tr>
	    <tr>
		<td colspan="7"></td>
	    </tr>
            <tr v-for="o in rows">
		<td></td>
                <td>{{o.name}}</td>
                <td>{{o.startDate}}</td>
		<td>{{o.endDate}}</td>
		<td>{{o.id}}</td>
		<td>{{o.executionStatus}}</td>
		<td></td>
            </tr>
            </tbody></table>
	    <div class="act">Всего: {{count}}</div>
	    </form>
	    </div>
	    </div>
    </tiles:put>
</tiles:insert>
