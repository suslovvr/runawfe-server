package ru.runa.wfe.presentation;

import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.commons.ArraysCommons;

/**
 * Holds field lists for {@link BatchPresentation}.
 */
class Store {

    /**
     * All fields from {@link BatchPresentation}.
     */
    public final FieldDescriptor[] allFields;

    /**
     * Displaying fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] displayFields;

    /**
     * Hidden fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] hiddenFields;

    /**
     * Sorted fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] sortedFields;

    /**
     * Grouped fields of {@link BatchPresentation}.
     */
    public final FieldDescriptor[] groupedFields;

    /**
     * Creates storage to hold different fields for batch presentation.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, to create field lists.
     */
    public Store(BatchPresentation batchPresentation) {
        List<DynamicField> dynamicFields = batchPresentation.getDynamicFields();
        allFields = new FieldDescriptor[batchPresentation.getType().getFields().length + dynamicFields.size()];
        for (int idx = 0; idx < dynamicFields.size(); ++idx) {
            DynamicField dynamicField = dynamicFields.get(idx);
            allFields[idx] = batchPresentation.getType().getFields()[dynamicField.getFieldIdx().intValue()].createConcreteEditableField(
                    dynamicField.getDynamicValue(), idx);
        }
        for (int idx = 0; idx < batchPresentation.getType().getFields().length; ++idx) {
            allFields[idx + dynamicFields.size()] = batchPresentation.getType().getFields()[idx].createConcreteField(idx
                    + dynamicFields.size());
        }
        displayFields = removeNotEnabled((FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields,
                batchPresentation.getFieldsToDisplayIds()));
        sortedFields = (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, batchPresentation.getFieldsToSortIds());
        groupedFields = (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, batchPresentation.getFieldsToGroupIds());
        int fieldsCount = allFields.length;
        List<Integer> fieldIdList = ArraysCommons.createArrayListFilledIncrement(fieldsCount);
        fieldIdList.removeAll(ArraysCommons.createIntegerList(batchPresentation.getFieldsToDisplayIds()));
        hiddenFields = (FieldDescriptor[]) ArraysCommons.createArrayValuesByIndex(allFields, ArraysCommons.createIntArray(fieldIdList));
    }

    /**
     * Removes all fields with not ENABLE state.
     * 
     * @param fields
     *            Fields, to remove not ENABLED.
     * @return ENABLED fields list.
     */
    private FieldDescriptor[] removeNotEnabled(FieldDescriptor[] fields) {
        List<FieldDescriptor> result = new ArrayList<>();
        for (FieldDescriptor fieldDescriptor : fields) {
            if (fieldDescriptor.fieldState == FieldState.ENABLED) {
                result.add(fieldDescriptor);
            }
        }
        return result.toArray(new FieldDescriptor[result.size()]);
    }
}
