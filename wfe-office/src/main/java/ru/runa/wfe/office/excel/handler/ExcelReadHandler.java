package ru.runa.wfe.office.excel.handler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.apache.poi.ss.usermodel.Workbook;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.excel.ExcelDataStore;
import ru.runa.wfe.office.excel.ExcelStorable;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ExcelReadHandler extends OfficeFilesSupplierHandler<ExcelBindings> {

    @Override
    protected FilesSupplierConfigParser<ExcelBindings> createParser() {
        return new ExcelBindingsParser();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) throws Exception {
        val result = new HashMap<String, Object>();
        ExcelDataStore dataStore = new ExcelDataStore();
        InputStream templateInputStream = config.getFileInputStream(variableProvider, fileDataProvider, true);
        Workbook workbook = dataStore.loadWorkbook(templateInputStream, config.isInputFileXLSX(variableProvider, false));
        for (ExcelBinding binding : config.getBindings()) {
            WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
            binding.getConstraints().applyPlaceholders(variableProvider);
            ExcelStorable storable = dataStore.create(binding.getConstraints());
            storable.setFormat(variable.getDefinition().getFormatNotNull());
            storable.load(workbook);
            result.put(binding.getVariableName(), storable.getData());
        }
        return result;
    }
}
