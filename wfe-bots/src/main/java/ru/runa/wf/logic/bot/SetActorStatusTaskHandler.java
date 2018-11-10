package ru.runa.wf.logic.bot;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Preconditions;

/**
 * This task handler changes actor status. Configuration looks like <config
 * actorVariableName='actorVar' statusVariableName='statusVar'/> where -
 * actorVar is variable which contains actor code - statusVar is variable of
 * Boolean or Number type and tells whether actor will be active or not
 * 
 * @since 3.0
 */
public class SetActorStatusTaskHandler extends TaskHandlerBase {
    private Config config;

    @Override
    public void setConfiguration(String configuration) {
        config = XmlParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) {
        Object value = variableProvider.getValueNotNull(config.actorVariableName);
        Actor actor = TypeConversionUtil.convertToExecutor(value, new DelegateExecutorLoader(user));
        boolean isActive = variableProvider.getValueNotNull(Boolean.class, config.statusVariableName);
        Delegates.getExecutorService().setStatus(user, actor, isActive);
        return null;
    }

    private static class Config {
        private String actorVariableName;
        private String statusVariableName;

        @Override
        public String toString() {
            return "<config actorVariableName='" + actorVariableName + "' statusVariableName='" + statusVariableName + "'/>";
        }
    }

    private static class XmlParser {
        private static final String ACTOR_ATTR_NAME = "actorVariableName";
        private static final String STATUS_ATTR_NAME = "statusVariableName";

        public static Config parse(String configuration) {
            Document document = XmlUtils.parseWithoutValidation(configuration);
            Element root = document.getRootElement();
            Config config = new Config();
            config.actorVariableName = root.attributeValue(ACTOR_ATTR_NAME);
            config.statusVariableName = root.attributeValue(STATUS_ATTR_NAME);
            Preconditions.checkNotNull(config.actorVariableName, ACTOR_ATTR_NAME);
            Preconditions.checkNotNull(config.statusVariableName, STATUS_ATTR_NAME);
            return config;
        }
    }
}
