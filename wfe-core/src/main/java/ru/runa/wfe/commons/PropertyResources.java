package ru.runa.wfe.commons;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.dao.SettingDao;

public class PropertyResources {
    private static final Log log = LogFactory.getLog(PropertyResources.class);
    private final String fileName;
    private final Properties properties;
    private final boolean useDatabase;
    private static boolean databaseAvailable = false;

    private static Map<String, String> propertiesCache = Maps.newHashMap();

    private SettingDao settingDao = null;

    public static void setDatabaseAvailable(boolean available) {
        databaseAvailable = available;
    }

    public PropertyResources(String fileName) {
        this(fileName, true, true);
    }

    public PropertyResources(String fileName, boolean required) {
        this(fileName, required, true);
    }

    public PropertyResources(String fileName, boolean required, boolean useDatabase) {
        this.useDatabase = useDatabase;
        this.fileName = fileName;
        properties = ClassLoaderUtil.getProperties(fileName, required);
    }

    public Set<String> getAllPropertyNames() {
        return properties.stringPropertyNames();
    }

    public Map<String, String> getAllProperties() {
        Map<String, String> map = Maps.newHashMap();
        for (String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return map;
    }

    public static void renewCachedProperty(String fileName, String name, String value) {
        String fullName = fileName + '#' + name;
        synchronized (propertiesCache) {
            propertiesCache.put(fullName, value);
        }
    }
    
    public void removeCachedProperty (String name) {
        String fullName = fileName + '#' + name;
        synchronized (propertiesCache) {
            propertiesCache.remove(fullName);
        }
    }

    public static void clearPropertiesCache() {
        synchronized (propertiesCache) {
            propertiesCache.clear();
        }
    }

    public String getStringProperty(String name) {
        if (databaseAvailable && useDatabase) {
            if (settingDao == null) {
                try {
                    settingDao = ApplicationContextFactory.getSettingDAO();
                } catch (Exception e) {
                    log.error("No SettingDAO available", e);
                }
            }
            if (settingDao != null) {
                // TODO ineffective implementation
                synchronized (propertiesCache) {
                    String fullName = fileName + '#' + name;
                    if (propertiesCache.containsKey(fullName)) {
                        return propertiesCache.get(fullName);
                    }
                    try {
                        String value = settingDao.getValue(fileName, name);
                        if (value == null) {
                            value = properties.getProperty(name);
                        }
                        if (value != null) {
                            value = value.trim();
                        }
                        propertiesCache.put(fullName, value);
                        return value;
                    } catch (Exception e) {
                        log.error("Database error", e);
                    }
                }
            }
        }
        String value = properties.getProperty(name);
        if (value != null) {
            return value.trim();
        }
        return null;
    }
    
    public Long getIdentifier (String name) {
        if (settingDao == null) {
            try {
                settingDao = ApplicationContextFactory.getSettingDAO();
            } catch (Exception e) {
                log.error("No SettingDAO available", e);
            }
        }
        if (settingDao != null) {
            return settingDao.getIdentifier(fileName, name);
        }
        return null;
    }
    
    public boolean isSettingStoredInDatabase(String name) {
        if (databaseAvailable && useDatabase) {
            if (settingDao == null) {
                try {
                    settingDao = ApplicationContextFactory.getSettingDAO();
                } catch (Exception e) {
                    log.error("No SettingDAO available", e);
                }
            }
            if (settingDao != null && settingDao.getValue(fileName, name) != null) {
                return true;
            }
        }
        return false;
    }

    public String getStringPropertyNotNull(String name) {
        String string = getStringProperty(name);
        if (string != null) {
            return string;
        }
        throw new InternalApplicationException("No property '" + name + "' was found in '" + fileName + "'");
    }

    public String getStringProperty(String name, String defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public List<String> getMultipleStringProperty(String name) {
        String string = getStringProperty(name);
        if (string != null) {
            return Splitter.on(";").omitEmptyStrings().trimResults().splitToList(string);
        }
        return Lists.newArrayList();
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(result);
    }

    public int getIntegerProperty(String name, int defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Integer.parseInt(result);
    }

    public long getLongProperty(String name, long defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Long.parseLong(result);
    }

    public Date getDateProperty(String name, Date defaultValue) {
        String result = getStringProperty(name);
        try {
            if (result != null) {
                return new SimpleDateFormat(SystemProperties.getDateFormatPattern()).parse(result);
            }
        } catch (ParseException e) {
            log.error("Date '" + result + "' parsing error", e);
        }
        return defaultValue;
    }

}
