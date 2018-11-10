package ru.runa.common.web;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Element;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.WebResources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.audit.presentation.FileValue;
import ru.runa.wfe.audit.presentation.HtmlValue;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;

@CommonsLog
public class HTMLUtils {

    private HTMLUtils() {
    }

    public static String writeHtml(Document document) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name());
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            String string = new String(outputStream.toByteArray(), Charsets.UTF_8);
            // crunch to fix issue #646
            // setting xalan property
            // "{http://xml.apache.org/xalan}line-separator" to "\n" did not
            // work
            string = string.replaceAll("&#13;\r\n", "\n");
            return string;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static Document readHtml(byte[] htmlBytes) {
        try {
            DOMParser parser = new DOMParser();
            InputSource inputSource = new InputSource(new ByteArrayInputStream(htmlBytes));
            inputSource.setEncoding(Charsets.UTF_8.name());
            parser.parse(inputSource);
            return parser.getDocument();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static String encodeFileName(HttpServletRequest request, String fileName) {
        try {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                if (userAgent.indexOf("MSIE") != -1 || userAgent.indexOf("Trident") != -1 || userAgent.indexOf("Edge") != -1) {
                    // IE
                    fileName = URLEncoder.encode(fileName, Charsets.UTF_8.name());
                    fileName = fileName.replaceAll("\\+", " ");
                } else {
                    fileName = MimeUtility.encodeText(fileName, Charsets.UTF_8.name(), "B");
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
        return fileName;
    }

    public static TR createCheckboxRow(String label, String name, boolean checked, boolean enabled, boolean required) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(Resources.CLASS_LIST_TABLE_TD));
        Input input = new Input(Input.CHECKBOX, name);
        input.setChecked(checked);
        input.setDisabled(!enabled);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static Input createCheckboxInput(String name, boolean checked, boolean enabled, boolean required) {
        Input input = new Input(Input.CHECKBOX, name);
        input.setChecked(checked);
        input.setDisabled(!enabled);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }
        return input;
    }

    public static TR createSelectRow(String label, String name, Option[] options, boolean enabled, boolean required) {
        Select select = new Select(name, options);
        select.setID(name);
        select.setDisabled(!enabled);
        return createSelectRow(label, select, required);
    }

    public static TR createSelectRow(String label, Select select, boolean required) {
        Element element = select;
        if (required) {
            Div div = new Div();
            div.addElement(select);
            div.setClass(Resources.CLASS_REQUIRED);
            element = div;
        }
        return createRow(label, element);
    }

    public static Option createOption(String value, boolean isSelected) {
        return createOption(value, value, isSelected);
    }

    public static Option createOption(String value, String label, boolean isSelected) {
        Option option = new Option();
        option.setValue(value == null ? "" : value);
        option.addElement(label);
        if (isSelected) {
            option.setSelected(true);
        }
        return option;
    }

    public static Option createOption(int value, String label, boolean isSelected) {
        Option option = new Option();
        option.setValue(value);
        option.addElement(label);
        if (isSelected) {
            option.setSelected(true);
        }
        return option;
    }

    public static Input createInput(String name, String value) {
        return createInput(Input.TEXT, name, value, true, false);
    }

    public static Input createInput(String name, String value, boolean enabled, boolean required) {
        return createInput(Input.TEXT, name, value, enabled, required);
    }

    public static Input createInput(String type, String name, String value) {
        return createInput(type, name, value, true, false);
    }

    public static Input createInput(String type, String name, String value, boolean enabled, boolean required) {
        if (value == null) {
            value = "";
        }
        Input input = new Input(type, name, value);
        input.setDisabled(!enabled);
        if (required) {
            input.setClass(Resources.CLASS_REQUIRED);
        }
        return input;
    }

    public static TR createRow(String label, Element element) {
        TR tr = new TR();
        tr.addElement(new TD(label).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD elementTd = element instanceof TD ? (TD) element : new TD(element);
        tr.addElement(elementTd.setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static String getExecutorName(Executor executor, PageContext pageContext) {
        String result;
        if (executor == null) {
            result = "";
        } else if (Executor.UNAUTHORIZED_EXECUTOR_NAME.equals(executor.getName())) {
            result = MessagesExecutor.UNAUTHORIZED_EXECUTOR_NAME.message(pageContext);
        } else if (executor instanceof EscalationGroup) {
            result = MessagesExecutor.ESCALATION_GROUP_NAME.message(pageContext);
        } else if (executor instanceof TemporaryGroup) {
            result = MessagesExecutor.DYNAMIC_GROUP_NAME.message(pageContext);
        } else if (executor.getName().equals(SystemExecutors.PROCESS_STARTER_NAME)) {
            result = MessagesProcesses.PROCESS_STARTER_NAME.message(pageContext);
        } else if (SystemProperties.isV3CompatibilityMode() && executor.getName().startsWith("__TmpGroup")) {
            result = MessagesExecutor.ESCALATION_GROUP_NAME.message(pageContext);
        } else {
            result = executor.getName();
        }
        return result;
    }

    public static ConcreteElement createExecutorElement(User user, PageContext pageContext, Executor executor) {
        if (executor == null || !Delegates.getAuthorizationService().isAllowed(user, Permission.LIST, executor)) {
            return new StringElement(getExecutorName(executor, pageContext));
        }
        return createExecutorElement(pageContext, executor);
    }

    public static ConcreteElement createExecutorElement(PageContext pageContext, Executor executor) {
        String executorName = getExecutorName(executor, pageContext);
        if (Strings.isNullOrEmpty(executorName)) {
            return new StringElement(executorName);
        }
        String url = "";
        if (!Executor.UNAUTHORIZED_EXECUTOR_NAME.equals(executor.getName())) {
            url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, executor.getId(), pageContext,
                    PortletUrlType.Render);
        }
        return new A(url, executorName);
    }

    /**
     * Substitutes arguments for process history logs
     * 
     * @param pageContext Can be <code>null</code>.
     * @return representable values
     */
    public static Object[] substituteArguments(User user, PageContext pageContext, Object[] arguments) {
        Object[] result = new Object[arguments.length];
        for (int i = 0; i < result.length; i++) {
            if (arguments[i] instanceof ExecutorNameValue) {
                String name = ((ExecutorNameValue) arguments[i]).getName();
                if (name == null) {
                    result[i] = "null";
                    continue;
                }
                try {
                    if (name.startsWith(TemporaryGroup.GROUP_PREFIX) || name.startsWith(EscalationGroup.GROUP_PREFIX)) {
                        result[i] = name;
                    } else {
                        Executor executor = Delegates.getExecutorService().getExecutorByName(user, name);
                        result[i] = pageContext != null ? createExecutorElement(pageContext, executor) : executor.toString();
                    }
                } catch (Exception e) {
                    log.debug("could not get executor '" + name + "': " + e.getMessage());
                    result[i] = name;
                }
            } else if (arguments[i] instanceof ExecutorIdsValue) {
                List<Long> ids = ((ExecutorIdsValue) arguments[i]).getIds();
                if (ids == null || ids.isEmpty()) {
                    result[i] = "null";
                    continue;
                }
                final StringBuilder executors = new StringBuilder("{ ");
                for (Long id : ids) {
                    try {
                        Executor executor = Delegates.getExecutorService().getExecutor(user, id);
                        executors.append(pageContext != null ? createExecutorElement(pageContext, executor) : executor.toString()).append("&nbsp;");
                    } catch (Exception e) {
                        log.debug("could not get executor by " + id + ": " + e.getMessage());
                        executors.append(id).append("&nbsp;");
                    }
                }
                executors.append("}");
                result[i] = executors.toString();
            } else if (arguments[i] instanceof ProcessIdValue) {
                Long processId = ((ProcessIdValue) arguments[i]).getId();
                if (processId == null) {
                    result[i] = "null";
                    continue;
                }
                if (pageContext == null) {
                    result[i] = processId;
                    continue;
                }
                Map<String, Object> params = Maps.newHashMap();
                params.put(IdForm.ID_INPUT_NAME, processId);
                String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render);
                result[i] = new A(url, processId.toString()).setClass(Resources.CLASS_LINK).toString();
            } else if (arguments[i] instanceof FileValue) {
                FileValue fileValue = (FileValue) arguments[i];
                if (pageContext != null) {
                    result[i] = ViewUtil.getFileLogOutput(new StrutsWebHelper(pageContext), fileValue.getLogId(), fileValue.getFileName());
                } else {
                    result[i] = fileValue.getFileName() + " (ID=" + fileValue.getLogId() + ")";
                }
            } else if (arguments[i] instanceof HtmlValue) {
                result[i] = ((HtmlValue) arguments[i]).getString();
            } else if (arguments[i] instanceof String) {
                result[i] = StringEscapeUtils.escapeHtml((String) arguments[i]);
            } else {
                result[i] = arguments[i];
            }
        }
        return result;
    }

    private static Map<String, Pattern> patternForTagCache = Maps.newHashMap();

    private static Pattern getPatternForTag(String tagName) {
        final String pattern = "<\\s*%s(\\s+.*>|>)";
        if (!patternForTagCache.containsKey(tagName)) {
            patternForTagCache.put(tagName,
                    Pattern.compile(String.format(pattern, tagName), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
        }
        return patternForTagCache.get(tagName);
    }

    public static boolean checkForBlockElements(String html) {
        Set<String> blockElements = WebResources.getHtmlBlockElements();

        for (String element : blockElements) {
            Pattern pattern = getPatternForTag(element);
            if (pattern.matcher(html).find()) {
                return true;
            }
        }
        return false;
    }

    public static Input createSelectionStatusPropagator() {
        Input propagator = new Input(Input.CHECKBOX);
        propagator.setClass("selectionStatusPropagator");
        return propagator;
    }

}
