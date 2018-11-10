package ru.runa.wfe.office.doc;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * @author Alekseev Vitaly
 */
@CommonsLog
public class DocxFileChangerLoopsWithGroovyTest extends DocxFileChangerTest {

    public static final List<?> getList(Long arg) {
        log.info("getList: " + arg);
        if (arg == 0) {
            List<Map<String, String>> result = Lists.newArrayList();
            for (int i = 0; i < 3; i++) {
                Map<String, String> entry = Maps.newHashMap();
                entry.put("key", "key" + i);
                entry.put("value", "value" + i);
                result.add(entry);
            }
            return result;
        }
        List<Map.Entry<String, String>> result = Lists.newArrayList();
        result.add(Maps.immutableEntry("key0", "value0"));
        result.add(Maps.immutableEntry("key1", "value1"));
        result.add(Maps.immutableEntry("key2", "value2"));
        return result;
    }

    @Test
    public void testChange() throws IOException {
        log.info("testChange: ");
        Map<String, Object> data = Maps.newHashMap();
        testDocx(true, "items_groovy.docx", data);
    }
}
