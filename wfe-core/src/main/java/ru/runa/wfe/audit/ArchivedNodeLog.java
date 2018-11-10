package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.NodeType;

@Entity
@DiscriminatorValue(value = "0")
public abstract class ArchivedNodeLog extends ArchivedProcessLog implements NodeLog {

    @Override
    @Transient
    public Type getType() {
        return Type.NODE;
    }

    @Override
    @Transient
    public String getNodeName() {
        return getAttributeNotNull(ATTR_NODE_NAME);
    }

    @Override
    @Transient
    public NodeType getNodeType() {
        return NodeType.valueOf(getAttributeNotNull(ATTR_NODE_TYPE));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getNodeName() };
    }
}
