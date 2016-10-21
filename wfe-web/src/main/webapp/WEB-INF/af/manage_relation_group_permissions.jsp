<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="ru.runa.common.web.Commons"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">
    <tiles:put name="body" type="string">
        <wf:updatePermissionsOnRelationGroupForm>
            <table width="100%">
                <tr>
                    <td align="left">
                        <wf:grantPermissionsOnRelationGroupLink />
                    </td>
                </tr>
            </table>
        </wf:updatePermissionsOnRelationGroupForm>
    </tiles:put>
    <tiles:put name="messages" value="../common/messages.jsp"/>
</tiles:insert>