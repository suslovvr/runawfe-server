package ru.runa.wfe.office.storage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class SqlServerStoreService extends JdbcStoreService {

    @SuppressWarnings({ "serial" })
    private static final Map<Class<? extends VariableFormat>, String> typeMap = new HashMap<Class<? extends VariableFormat>, String>() {
        {
            put(StringFormat.class, "nvarchar(4000)");
            put(TextFormat.class, "nvarchar(4000)");
            put(FormattedTextFormat.class, "nvarchar(4000)");
            put(LongFormat.class, "int");
            put(DoubleFormat.class, "numeric(38,19)");
            put(BooleanFormat.class, "nvarchar(5)");
            put(BigDecimalFormat.class, "decimal(38,19)");
            put(DateTimeFormat.class, "datetime2");
            put(DateFormat.class, "date");
            put(TimeFormat.class, "time");
            put(ExecutorFormat.class, "nvarchar(4000)");
            put(ActorFormat.class, "nvarchar(4000)");
            put(GroupFormat.class, "nvarchar(4000)");
            put(ProcessIdFormat.class, "int");
            put(FileFormat.class, "nvarchar(4000)");
        }
    };

    public SqlServerStoreService(VariableProvider variableProvider) {
        super(variableProvider);
    }

    @Override
    protected String tableExistsSql() {
        return "select null from information_schema.tables where table_type = ''BASE TABLE'' and table_name = N''{0}''";
    }

    @Override
    protected Map<Class<? extends VariableFormat>, String> typeMap() {
        return typeMap;
    }

    @Override
    protected Object adjustValue(Object value) {
        if (value != null) {
            if (value instanceof BigDecimal) {
                String strValue = ((BigDecimal) value).toPlainString();
                if (strValue.indexOf('.') > 0) {
                    value = new BigDecimal(StringUtils.trimTrailingCharacter(StringUtils.trimTrailingCharacter(strValue, '0'), '.'));
                }
            }
        }
        return super.adjustValue(value);
    }
}
