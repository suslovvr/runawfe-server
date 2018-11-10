package ru.runa.wfe.commons.dao;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for database initialization and variables managing. Creates appropriate
 * tables (drops tables if such tables already exists) and records.
 */
@Component
// TODO rm700
@Transactional
public class SettingDao extends GenericDao<Setting> {

    public SettingDao() {
        super(Setting.class);
    }

    public String getValue(String fileName, String name) {
        val s = QSetting.setting;
        return queryFactory.select(s.value).from(s).where(s.fileName.eq(fileName).and(s.name.eq(name))).fetchFirst();
    }
    
    public Long getIdentifier(String fileName, String name) {
        val s = QSetting.setting;
        return queryFactory.select(s.id).from(s).where(s.fileName.eq(fileName).and(s.name.eq(name))).fetchFirst();
    }

    public void setValue(String fileName, String name, String value) {
        log.debug("setValue(" + fileName + ", " + name + ", " + value + ")");

        val s = QSetting.setting;
        BooleanExpression cond = s.fileName.eq(fileName).and(s.name.eq(name));

        if (value == null) {
            queryFactory.delete(s).where(cond).execute();
            return;
        }

        val id = queryFactory.select(s.id).from(s).where(cond).fetchFirst();
        if (id == null) {
            create(new Setting(fileName, name, value));
        } else {
            queryFactory.update(s).set(s.value, value).where(cond).execute();
        }
    }
    
    @Override
    public void delete(Long id) {
        Preconditions.checkNotNull(id);
        QSetting s = QSetting.setting; 
        queryFactory.delete(s).where(s.id.eq(id)).execute();
    }

    public void clear() {
        val s = QSetting.setting;
        queryFactory.delete(s).execute();
    }
}
