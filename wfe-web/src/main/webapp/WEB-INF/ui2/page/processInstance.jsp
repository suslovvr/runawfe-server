<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/WEB-INF/ui2/layout/main.jsp" flush="true">
    <tiles:put name="jsController" value="wfe.processes"/>
    <tiles:put name="mainMenuActiveItem" value="processes"/>
    <tiles:put name="head" type="string">
        <title>Запущенные процессы</title>
        <script type="text/javascript" src="/wfe/js/ui2/processes.js"></script>
    </tiles:put>
    <tiles:put name="content" type="string">
<div class="one-contentback">	
	<div class="top-ico" id="top-ico">
		<div id="info"><img src='/wfe/images/ui2/ico/close.png' class='close-ti' onclick="javascript:document.getElementById('info').style.display='none';" />
		    	<h3>Информация</h3><br />
					Об определении процесса
		</div>
	</div>		
	<h1>Оформление договора<img src="/wfe/images/ui2/ico/info.png" onclick="javascript:document.getElementById('info').style.display='block';"  style="cursor:pointer; width:20px; height:auto; padding-left:10px;" title="Информация" /></h1>
	<div class="work">
		<div class="process-info light1">
			 <span>Информация об экземпляре<br />статус процесса<br />дата создания</span>
		</div>
		<div class="taskform">
		<h4>Схема процесса</h4>
		Граф процесса
		</div>
	</div>		
</div>
    </tiles:put>
</tiles:insert>
