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
        <table class="work def">
            <tbody>
            <tr>
                <td><input type="checkbox"/></td>
                <td>Запустить</td>
                <td><a href="" title="Сортировка">Имя</a></td>
                <td><a href="" title="Сортировка">Тип процесса</a></td>
                <td>Описание / (Длинное описание)</td>
            </tr>
            <tr>
                <td><input type="checkbox"/></td>
                <td><img src="/wfe/images/ui2/ico/start1_dark.png" alt="Запустить" class="start"/></td>
                <td>Оформление договора</td>
                <td>Документы</td>
                <td class="description">Оформление договора и его ведение и отслеживание на всех стадиях
                    <div><!-- пока на hover, надо на click -->
                        Пример 111 (пока на hover, надо на click)<p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p>
                    </div>
                </td>
            </tr>
            <tr>
                <td><input type="checkbox"/></td>
                <td><img src="/wfe/images/ui2/ico/start1_dark.png" alt="Запустить" class="start"/></td>
                <td>Оформление отпуска</td>
                <td>Кадры</td>
                <td>Какие документы и где нужны</td>
            </tr>
            <tr>
                <td><input type="checkbox"/></td>
                <td><img src="/wfe/images/ui2/ico/start1_dark.png" alt="Запустить" class="start"/></td>
                <td>Приём на работу</td>
                <td>Кадры</td>
                <td class="description">Описание процесса
                    <div><!-- пока на hover, надо на click -->
                        Пример 333 (пока на hover, надо на click)<p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p><p>Длинное описание</p>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </tiles:put>
</tiles:insert>
