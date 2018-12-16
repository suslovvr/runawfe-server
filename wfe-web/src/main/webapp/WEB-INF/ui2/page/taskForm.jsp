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
    <form class="task">
		<div class="input-buttons">
		    <button>Кнопка</button>
		    <input type="button" value="Действие1" />
		    <input type="button" value="Действие2" />
			<input type="button" value="Действие3" />
		</div>
		<h1>Ознакомьтесь, что задача не выполнена в срок</h1>
		<h2>ЗадачаПоУправлению</h2>
		<div class="work">
			<div class="process-info light1">
				<span>Информация об экземпляре<br />
				статус процесса
				<br />
				дата создания</span>
			</div>
			<div class="taskform"  id="taskform">				
				<h4>Выполните задание</h4>
				<table class="process-var">
				<tbody>
					<tr>
		 			 <td>Отображение переменной</td>
		 			 <td><input type="text" disabled="disabled" value="значение переменной" /></td>
					</tr>
					<tr>
					  <td>Ввод переменной1</td>
					  <td><input type="text" value="значение переменной1" name="переменная1" /></td>
					</tr>
					<tr>
					  <td>Ввод переменной2</td>
					  <td><textarea value="значение переменной2" name="переменная2"></textarea></td>
					</tr>
					<tr>
					  <td>Загрузите файл</td>
					  <td>
					  <div class="file-upload">
 						<label>
 						<input type="file" name="file" id="uploaded-file">
 						<span>Выберите файл</span>
 						</label>
 					  </div>
 					  <div id="filename"></div><div class="var-del"> - </div>
 					  </td>
					</tr>
					<tr>
					  <td></td>
					  <td>
					  <div class="uploaded-file" id="uploaded-file"> 
 						<span>test1.txt</span> 
 					  </div>
 					  <div id="filename"></div><div class="var-del"> - </div>
 					  </td>
					</tr>
					<tr>
					  <td>Ввод даты</td>
					  <td><input type="date" value="" name="переменная3" /></td>
					</tr>
				</tbody>
				</table>		
	        </div>
		</div><!-- work-->
    </form>
    </div>
    </tiles:put>
</tiles:insert>
