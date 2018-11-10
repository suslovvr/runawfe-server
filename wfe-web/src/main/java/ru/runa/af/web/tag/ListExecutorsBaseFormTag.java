package ru.runa.af.web.tag;

import java.util.List;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SecuredObjectCheckboxTdBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchedTag;
import ru.runa.common.web.tag.ReturningTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;

/**
 * Created on 06.09.2004
 *
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public abstract class ListExecutorsBaseFormTag extends UpdateExecutorBaseFormTag implements BatchedTag, ReturningTag {
    private static final long serialVersionUID = 1L;
    protected boolean buttonEnabled;

    @Override
    protected boolean isSubmitButtonEnabled() {
        return buttonEnabled && super.isSubmitButtonEnabled();
    }

    private String batchPresentationId;

    @Attribute(required = true)
    @Override
    public void setBatchPresentationId(String batchPresentationId) {
        this.batchPresentationId = batchPresentationId;
    }

    @Override
    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    @Override
    public BatchPresentation getBatchPresentation() {
        return getProfile().getActiveBatchPresentation(batchPresentationId);
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        int executorsCount = getExecutorsCount();
        List<? extends Executor> executors = getExecutors();
        if (super.isSubmitButtonEnabled()) {
            buttonEnabled = BatchPresentationUtils.isExecutorPermissionAllowedForAnyone(getUser(), executors, getBatchPresentation(),
                    getExecutorsPermission());
        }
        BatchPresentation batchPresentation = getBatchPresentation();
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, executorsCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { new SecuredObjectCheckboxTdBuilder(getExecutorsPermission()) },
                batchPresentation, null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(executors, batchPresentation, pageContext, WebResources.ACTION_MAPPING_UPDATE_EXECUTOR,
                getReturnAction(), IdForm.ID_INPUT_NAME, builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, returnAction, pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    /**
     * Load executors to show.
     *
     * @return Array of executors to show in tag.
     */
    protected abstract List<? extends Executor> getExecutors();

    /**
     * Load count of all executors may be shown in tag. Used to setup pages before and after executor list.
     *
     * @return Executors count.
     */
    protected abstract int getExecutorsCount();

    protected abstract Permission getExecutorsPermission();

    private String returnAction;

    @Override
    public String getReturnAction() {
        return returnAction;
    }

    @Attribute(required = false, rtexprvalue = true)
    @Override
    public void setReturnAction(String returnAction) {
        this.returnAction = returnAction;
    }

}
