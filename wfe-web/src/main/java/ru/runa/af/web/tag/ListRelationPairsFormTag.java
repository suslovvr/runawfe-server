package ru.runa.af.web.tag;

import java.util.List;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.action.RemoveRelationPairsAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listRelationPairsForm")
public class ListRelationPairsFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;
    private Long relationId;

    public Long getRelationId() {
        return relationId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.ALL, SecuredSingleton.RELATIONS);
        Relation relation = Delegates.getRelationService().getRelation(getUser(), relationId);
        BatchPresentation batchPresentation = getBatchPresentation();
        List<RelationPair> relationPairs = Delegates.getRelationService().getRelationPairs(getUser(), relation.getName(), batchPresentation);
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder checkboxBuilder = new CheckboxTdBuilder(null, Permission.ALL) {

            @Override
            protected String getIdValue(Object object) {
                return String.valueOf(((RelationPair) object).getId());
            }

            @Override
            protected boolean isEnabled(Object object, Env env) {
                return true;
            }
        };
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { checkboxBuilder }, batchPresentation, null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(relationPairs, batchPresentation, pageContext, WebResources.ACTION_MAPPING_UPDATE_EXECUTOR,
                getReturnAction(), IdForm.ID_INPUT_NAME, builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        tdFormElement.addElement(new Input(Input.HIDDEN, RelationForm.RELATION_ID, relationId.toString()));
    }

    @Override
    protected String getTitle() {
        return Delegates.getRelationService().getRelation(getUser(), relationId).getName();
    }

    @Override
    public String getAction() {
        return RemoveRelationPairsAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }
}
