package ru.runa.wfe.definition.par;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.NonNull;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;

public class ProcessArchive {
    public static final List<String> UNSECURED_FILE_NAMES = new ArrayList<String>() {{
        add(FileDataProvider.START_IMAGE_FILE_NAME);
        add(FileDataProvider.START_DISABLED_IMAGE_FILE_NAME);
        add(FileDataProvider.BOTS_XML_FILE);
    }};

    private static List<ProcessArchiveParser> processArchiveParsers = new ArrayList<ProcessArchiveParser>() {{
        add(ApplicationContextFactory.autowireBean(new FileArchiveParser()));
        add(ApplicationContextFactory.autowireBean(new ProcessDefinitionParser()));
        add(ApplicationContextFactory.autowireBean(new VariableDefinitionParser()));
        add(ApplicationContextFactory.autowireBean(new InteractionsParser()));
        add(ApplicationContextFactory.autowireBean(new TaskSubsitutionParser()));
        add(ApplicationContextFactory.autowireBean(new GraphXmlParser()));
        add(ApplicationContextFactory.autowireBean(new CommentsParser()));
    }};

    private static final Pattern SUBPROCESS_DEFINITION_PATTERN = Pattern.compile(
            FileDataProvider.SUBPROCESS_DEFINITION_PREFIX + "(\\d*)." + FileDataProvider.PROCESSDEFINITION_XML_FILE_NAME
    );

    private final ProcessDefinition processDefinition;
    private final ProcessDefinitionVersion processDefinitionVersion;
    private final Map<String, byte[]> fileData = Maps.newHashMap();

    public ProcessArchive(@NonNull ProcessDefinition d, @NonNull ProcessDefinitionVersion dv) {
        try {
            this.processDefinition = d;
            this.processDefinitionVersion = dv;
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(dv.getContent()));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                byte[] bytes = ByteStreams.toByteArray(zis);
                if (bytes != null) {
                    fileData.put(entryName, bytes);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.close();
        } catch (IOException e) {
            throw new DefinitionArchiveFormatException(e);
        }
    }

    public ParsedProcessDefinition parseProcessDefinition() {
        ParsedProcessDefinition parsedProcessDefinition = new ParsedProcessDefinition(processDefinition, processDefinitionVersion);

        for (ProcessArchiveParser processArchiveParser : processArchiveParsers) {
            processArchiveParser.readFromArchive(this, parsedProcessDefinition);
        }

        for (Map.Entry<String, byte[]> entry : parsedProcessDefinition.getProcessFiles().entrySet()) {
            Matcher matcher = SUBPROCESS_DEFINITION_PATTERN.matcher(entry.getKey());
            if (matcher.matches()) {
                int subprocessIndex = Integer.parseInt(matcher.group(1));
                ParsedSubprocessDefinition subprocessDefinition = new ParsedSubprocessDefinition(parsedProcessDefinition);
                subprocessDefinition.setNodeId(FileDataProvider.SUBPROCESS_DEFINITION_PREFIX + subprocessIndex);
                for (ProcessArchiveParser processArchiveParser : processArchiveParsers) {
                    if (processArchiveParser.isApplicableToEmbeddedSubprocess()) {
                        processArchiveParser.readFromArchive(this, subprocessDefinition);
                    }
                }
                parsedProcessDefinition.addEmbeddedSubprocess(subprocessDefinition);
            }
        }
        parsedProcessDefinition.mergeWithEmbeddedSubprocesses();
        return parsedProcessDefinition;
    }

    public Map<String, byte[]> getFileData() {
        return fileData;
    }
}
