<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/WEB-INF/ui2/layout/main.jsp" flush="true">
    <tiles:put name="onload" value="wfe.myTasks.onLoad()"/>
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
            <tr>
                <td><input type="checkbox"/></td>
                <td>Ознакомиться, что задача не выполнена в срок</td>
                <td class="description">Принять решение по смене разработчика или продолжению</td>
                <td>ЗадачаПоУправлению</td>
                <td>416</td>
                <td>22.11.2018</td>
                <td>12.11.2018</td>
            </tr>
            <tr>
                <td><input type="checkbox"/></td>
                <td>Выбрать разработчика</td>
                <td class="description"></td>
                <td>ЗадачаПоПроекту</td>
                <td>418</td>
                <td>25.11.2018</td>
                <td>13.11.2018</td>
            </tr>
            <tr>
                <td><input type="checkbox"/></td>
                <td>Согласовать документы с юристом</td>
                <td></td>
                <td class="description">ОформлениеДоговора</td>
                <td>401</td>
                <td>28.11.2018</td>
                <td>02.11.2018</td>
            </tr>
        </tbody></table>
        <div class="act"><a href="" class="button">Взять на выполнение</a> Всего: 1</div>
    </tiles:put>
</tiles:insert>
