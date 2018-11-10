package ru.runa.wfe.validation.impl;

import ru.runa.wfe.validation.FieldValidator;

/**
 * Base class for range based validators.
 */
public class RangeFieldValidator<T extends Object> extends FieldValidator {

    protected T getMinComparatorValue(Class<T> clazz) {
        return getParameter(clazz, "min", null);
    }

    protected T getMaxComparatorValue(Class<T> clazz) {
        return getParameter(clazz, "max", null);
    }

    @Override
    public void validate() {
        Comparable<T> value = (Comparable<T>) getFieldValue();
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (value == null) {
            return;
        }

        boolean inclusive = getParameter(boolean.class, "inclusive", true);
        T minValue = getMinComparatorValue((Class<T>) value.getClass());
        if (minValue != null) {
            if (inclusive) {
                if (value.compareTo(minValue) < 0) {
                    addError();
                }
            } else {
                if (value.compareTo(minValue) <= 0) {
                    addError();
                }
            }
        }

        T maxValue = getMaxComparatorValue((Class<T>) value.getClass());
        if (maxValue != null) {
            if (inclusive) {
                if (value.compareTo(maxValue) > 0) {
                    addError();
                }
            } else {
                if (value.compareTo(maxValue) >= 0) {
                    addError();
                }
            }
        }
    }

}
