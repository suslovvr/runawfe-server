package ru.runa.common.web.tag;

import org.tldgen.annotations.Attribute;
import ru.runa.wfe.presentation.BatchPresentation;

public abstract class BatchReturningTitledFormTag extends TitledFormTag implements BatchedTag, ReturningTag {

    private static final long serialVersionUID = 1L;
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

    private String returnAction;

    @Override
    public String getReturnAction() {
        return returnAction;
    }

    @Attribute
    @Override
    public void setReturnAction(String returnAction) {
        this.returnAction = returnAction;
    }
}
