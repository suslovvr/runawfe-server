package ru.runa.af.web.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.form.RelationPairForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

/**
 * List relations which contain executor on the left side.
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listExecutorLeftRelationsForm")
public class ListExecutorLeftRelationsFormTag extends SecuredObjectFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<Executor> executors = new ArrayList<>();
        executors.add(getSecuredObject());
        BatchPresentation batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        executors.addAll(Delegates.getExecutorService().getExecutorGroups(getUser(), getSecuredObject(), batchPresentation, false));
        List<Relation> relations = Delegates.getRelationService().getRelationsContainingExecutorsOnLeft(getUser(), executors);
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(null, BatchPresentationFactory.RELATIONS.createDefault(), null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(relations, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_MANAGE_RELATION, "", new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(getNames());
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_EXECUTOR_LEFT_RELATIONS.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected Executor getSecuredObject() {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getExecutor(getUser(), getIdentifiableId());
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.READ;
    }

    protected String[] getNames() {
        BatchPresentation batchPresentation = BatchPresentationFactory.RELATIONS.createDefault();
        FieldDescriptor[] fields = batchPresentation.getDisplayFields();
        String[] result = new String[fields.length];
        for (int i = 0; i < fields.length; ++i) {
            result[i] = Messages.getMessage(fields[i].displayName, pageContext);
        }
        return result;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            Map<String, Object> params = new HashMap<>();
            params.put(RelationPairForm.RELATION_ID, ((Relation) item).getId());
            params.put(RelationPairForm.EXECUTOR_FROM, getIdentifiableId());
            return Commons.getActionUrl(baseUrl, params, pageContext, PortletUrlType.Action);
        }
    }
}
