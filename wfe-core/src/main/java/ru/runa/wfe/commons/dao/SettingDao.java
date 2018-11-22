/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.dao;

import com.google.common.base.Preconditions;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log log = LogFactory.getLog(SettingDao.class);

    private Setting get(String fileName, String name) {
        QSetting s = QSetting.setting;
        return queryFactory.selectFrom(s).where(s.fileName.eq(fileName).and(s.name.eq(name))).fetchFirst();
    }

    public String getValue(String fileName, String name) {
        Setting property = get(fileName, name);
        if (property == null) {
            return null;
        }
        return property.getValue();
    }
    
    public Long getIdentifier(String fileName, String name) {
        Setting property = get(fileName, name);
        if (property == null) {
           log.error("No property found for file " + fileName + " with name " + name + ".");
           return null;
        }
        return property.getId();
    }

    public void setValue(String fileName, String name, String value) {
        log.debug("setValue(" + fileName + ", " + name + ", " + value + ")");
        Setting property = get(fileName, name);
        if (value == null) {
            if (property != null) {
                delete(property);
            }
            return;
        }
        if (property == null) {
            create(new Setting(fileName, name, value));
        } else {
            property.setValue(value);
            update(property);
        }
    }
    
    @Override
    public void delete(Long id) {
        Preconditions.checkNotNull(id);
        QSetting s = QSetting.setting; 
        queryFactory.delete(s).where(s.id.eq(id)).execute();
    }

    public void clear() {
        List<Setting> list = getAll();
        for (Setting l : list) {
            delete(l);
        }
    }
}
