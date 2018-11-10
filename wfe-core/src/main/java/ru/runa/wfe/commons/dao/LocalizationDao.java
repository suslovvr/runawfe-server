package ru.runa.wfe.commons.dao;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * DAO for managing {@link Localization}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class LocalizationDao extends GenericDao<Localization> {

    private Map<String, String> localizations = Maps.newHashMap();

    public LocalizationDao() {
        super(Localization.class);
    }

    public void init() {
        for (Localization localization : getAll()) {
            localizations.put(localization.getName(), localization.getValue());
        }
    }

    /**
     * Load localized value.
     * 
     * @param name
     *            key
     * @return localized value or key if no localization exists
     */
    public String getLocalized(String name) {
        String value = localizations.get(name);
        if (value == null) {
            return name;
        }
        return value;
    }

    /**
     * Save localizations.
     * 
     * @param localizations
     *            localizations
     * @param rewrite
     *            rewrite existing localization
     */
    public void saveLocalizations(List<Localization> localizations, boolean rewrite) {
        for (Localization localization : localizations) {
            saveLocalization(localization.getName(), localization.getValue(), rewrite);
        }
    }

    /**
     * Save localization.
     * 
     * @param name
     *            key
     * @param value
     *            localized value
     * @param rewrite
     *            rewrite existing localization
     */
    private void saveLocalization(String name, String value, boolean rewrite) {
        val l = QLocalization.localization;
        Localization localization = queryFactory.selectFrom(l).where(l.name.eq(name)).fetchFirst();
        if (localization == null || rewrite) {
            localizations.put(name, value);
        }
        if (localization == null) {
            create(new Localization(name, value));
        } else if (rewrite) {
            localization.setValue(value);
        }
    }
}
