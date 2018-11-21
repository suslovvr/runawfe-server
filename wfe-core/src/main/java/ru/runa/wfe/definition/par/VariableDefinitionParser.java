package ru.runa.wfe.definition.par;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dao.LocalizationDao;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.validation.ValidatorConfig;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableStoreType;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

@CommonsLog
public class VariableDefinitionParser implements ProcessArchiveParser {
    private static final String FORMAT = "format";
    private static final String SWIMLANE = "swimlane";
    private static final String NAME = "name";
    private static final String VARIABLE = "variable";
    private static final String PUBLIC = "public";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String SCRIPTING_NAME = "scriptingName";
    private static final String USER_TYPE = "usertype";
    private static final String DESCRIPTION = "description";
    private static final String STORE_TYPE = "storeType";
    private static final String VALIDATOR = "validator";
    private static final String TYPE = "type";
    private static final String MESSAGE = "message";
    private static final String PARAM = "param";

    @Autowired
    private LocalizationDao localizationDao;

    public void setLocalizationDao(LocalizationDao localizationDao) {
        this.localizationDao = localizationDao;
    }

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return false;
    }

    @Override
    public void readFromArchive(ProcessArchive archive, ParsedProcessDefinition parsedProcessDefinition) {
        byte[] xml = parsedProcessDefinition.getFileDataNotNull(FileDataProvider.VARIABLES_XML_FILE_NAME);
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        List<Element> typeElements = document.getRootElement().elements(USER_TYPE);
        for (Element typeElement : typeElements) {
            UserType type = new UserType(typeElement.attributeValue(NAME));
            parsedProcessDefinition.addUserType(type);
        }
        for (Element typeElement : typeElements) {
            UserType type = parsedProcessDefinition.getUserTypeNotNull(typeElement.attributeValue(NAME));
            List<Element> attributeElements = typeElement.elements(VARIABLE);
            for (Element element : attributeElements) {
                VariableDefinition variableDefinition = parse(parsedProcessDefinition, element);
                type.addAttribute(variableDefinition);
            }
        }
        for (UserType userType : parsedProcessDefinition.getUserTypes()) {
            for (VariableDefinition variableDefinition : userType.getAttributes()) {
                parseDefaultValue(parsedProcessDefinition, variableDefinition);
            }
        }
        List<Element> variableElements = root.elements(VARIABLE);
        for (Element element : variableElements) {
            boolean swimlane = Boolean.parseBoolean(element.attributeValue(SWIMLANE, "false"));
            if (swimlane) {
                String name = element.attributeValue(NAME);
                String scriptingName = element.attributeValue(SCRIPTING_NAME, name);
                parsedProcessDefinition.setSwimlaneScriptingName(name, scriptingName);
            } else {
                VariableDefinition variableDefinition = parse(parsedProcessDefinition, element);
                parseDefaultValue(parsedProcessDefinition, variableDefinition);
                parsedProcessDefinition.addVariable(variableDefinition);
            }
        }
    }

    private VariableDefinition parse(ParsedProcessDefinition parsedProcessDefinition, Element element) {
        String name = element.attributeValue(NAME);
        String scriptingName = element.attributeValue(SCRIPTING_NAME, name);
        VariableDefinition variableDefinition = new VariableDefinition(name, scriptingName);
        variableDefinition.setDescription(element.attributeValue(DESCRIPTION));
        String userTypeName = element.attributeValue(USER_TYPE);
        if (userTypeName != null) {
            variableDefinition.setFormat(userTypeName);
            variableDefinition.setUserType(parsedProcessDefinition.getUserTypeNotNull(userTypeName));
        } else {
            String format = element.attributeValue(FORMAT);
            format = BackCompatibilityClassNames.getClassName(format);
            variableDefinition.setFormat(format);
            String formatLabel;
            if (format.contains(VariableFormatContainer.COMPONENT_PARAMETERS_START)) {
                formatLabel = localizationDao.getLocalized(variableDefinition.getFormatClassName());
                formatLabel += VariableFormatContainer.COMPONENT_PARAMETERS_START;
                String[] componentClassNames = variableDefinition.getFormatComponentClassNames();
                formatLabel += Joiner.on(VariableFormatContainer.COMPONENT_PARAMETERS_DELIM)
                        .join(Lists.transform(Lists.newArrayList(componentClassNames), new Function<String, String>() {

                            @Override
                            public String apply(String input) {
                                return localizationDao.getLocalized(input);
                            }
                        }));
                formatLabel += VariableFormatContainer.COMPONENT_PARAMETERS_END;
            } else {
                formatLabel = localizationDao.getLocalized(format);
            }
            variableDefinition.setFormatLabel(formatLabel);
        }
        variableDefinition.initComponentUserTypes(parsedProcessDefinition);
        variableDefinition.setPublicAccess(Boolean.parseBoolean(element.attributeValue(PUBLIC, "false")));
        List<Element> validatorElements = element.elements(VALIDATOR);
        Map<String, ValidatorConfig> validators = Maps.newHashMap();
        if (validatorElements != null) {
            for (Element validatorElement : validatorElements) {
                String validatorType = validatorElement.attributeValue(TYPE);
                String validatorMessage = validatorElement.element(MESSAGE).getText();
                Map<String, String> validatorParams = Maps.newHashMap();
                for (Element validatorParamElement : (List<Element>)validatorElement.elements(PARAM)) {
                    validatorParams.put(validatorParamElement.attributeValue(NAME), validatorParamElement.getText());
                }
                validators.put(validatorType, new ValidatorConfig(validatorType, validatorMessage, validatorParams));
            }
        }
        variableDefinition.setValidators(validators);
        variableDefinition.setDefaultValue(element.attributeValue(DEFAULT_VALUE));
        String storeTypeString = element.attributeValue(STORE_TYPE);
        if (!Strings.isNullOrEmpty(storeTypeString)) {
            variableDefinition.setStoreType(VariableStoreType.valueOf(storeTypeString.toUpperCase()));
        }
        return variableDefinition;
    }

    private void parseDefaultValue(ParsedProcessDefinition parsedProcessDefinition, VariableDefinition variableDefinition) {
        String stringDefaultValue = (String) variableDefinition.getDefaultValue();
        if (!Strings.isNullOrEmpty(stringDefaultValue)) {
            try {
                variableDefinition.setDefaultValue(null);
                VariableFormat variableFormat = FormatCommons.create(variableDefinition);
                Object value = variableFormat.parse(stringDefaultValue);
                variableDefinition.setDefaultValue(value);
            } catch (Exception e) {
                if (!SystemProperties.isVariablesInvalidDefaultValuesAllowed()
                        || parsedProcessDefinition.getProcessDefinitionVersion().getCreateDate().after(SystemProperties.getVariablesInvalidDefaultValuesAllowedBefore())
                ) {
                    throw new InternalApplicationException("Unable to parse default value '" + stringDefaultValue + "'", e);
                } else {
                    log.error("Unable to format default value '" + stringDefaultValue + "' in " + parsedProcessDefinition + ":" + variableDefinition, e);
                }
            }
        }
    }
}
