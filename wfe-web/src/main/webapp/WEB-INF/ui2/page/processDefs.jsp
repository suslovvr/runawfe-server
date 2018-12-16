<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/WEB-INF/ui2/layout/main.jsp" flush="true">
    <tiles:put name="jsController" value="wfe.processDefs"/>
    <tiles:put name="mainMenuActiveItem" value="processDefs"/>
    <tiles:put name="head" type="string">
        <title>Задачи</title>
        <script type="text/javascript" src="/wfe/js/ui2/processDefs.js"></script>
    </tiles:put>
    <tiles:put name="content" type="string">
        <div class="two-contentback" id="two-contentback">
	    <form>
		<div class="input-buttons">
		    <input type="submit" value="Запустить процесс" />
		</div>
		<h1>Оформление договора</h1>
		<div class="work">
			<div class="def">
				<div class="long-description" id="long-description1">
					<!-- Длинное описание-->
					Пример 111 <p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p>
				</div>	
				<div class="firstform"  id="firstform1">
					<h3>Заполните форму, если она есть и запустите процесс</h3>
				<table class="process-var">
				<tbody>
					<tr>
		 			 <td>Отображение переменной1</td>
		 			 <td>значение переменной1</td>
					</tr>
					<tr>
					  <td>Ввод переменной2</td>
					  <td><input type="text" value="значение переменной2" /></td>
					</tr>
				</tbody>
				</table>
				</div>
			</div>
		</div></form>
	</div>
	
	<div class="one-contentback oc-shadow" id="one-contentback">	
		<h1>Запустить процесс</h1>
		<div class="work">
			<div class="def">
				<ul>
				<li v-for="o in rows"><span title="Запустить" class="process-name">{{o.name}}</span>
		 		 <div class="description" id="description1" title="Подробнее">{{o.category}} / {{o.description}}. <span class="desc-ling-open">Подробнее...</span>
	             </div>
				</li>
				</ul>

			</div>
		</div>	
     </div>
    </tiles:put>
</tiles:insert>
