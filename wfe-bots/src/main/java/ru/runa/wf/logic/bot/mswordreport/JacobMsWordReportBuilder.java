package ru.runa.wf.logic.bot.mswordreport;

import ru.runa.wfe.var.VariableProvider;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * 
 * Created on 23.11.2006
 * 
 */
public class JacobMsWordReportBuilder extends MsWordReportBuilder {

    public JacobMsWordReportBuilder(MsWordReportTaskSettings settings, VariableProvider variableProvider) {
        super(settings, variableProvider);
    }

    @Override
    public void build(String reportTemporaryFileName) {
        ActiveXComponent wordApplication = null;
        Dispatch wordDocument = null;
        try {
            wordApplication = new ActiveXComponent("Word.Application");
            wordApplication.setProperty("DisplayAlerts", new Variant(false));
            wordApplication.setProperty("Visible", new Variant(false));
            Dispatch wordDocuments = wordApplication.getProperty("Documents").toDispatch();
            wordDocument = Dispatch.call(wordDocuments, "Open", settings.getTemplateFilePath()).toDispatch();
            replaceBookmarksWithValues(wordDocument, variableProvider, settings);
            Dispatch.call(wordDocument, "SaveAs", reportTemporaryFileName);
        } catch (Exception e) {
            log.error("", e);
            if (wordApplication != null && wordDocument == null) {
                throw new MsWordReportException(MsWordReportException.OPEN_TEMPLATE_DOCUMENT_FAILED, settings.getTemplateFilePath());
            }
            throw new MsWordReportException(MsWordReportException.MSWORD_APP_COMM_ERROR);
        } finally {
            if (wordDocument != null) {
                try {
                    Dispatch.call(wordDocument, "Close", new Variant(Boolean.FALSE));
                } finally {
                    if (wordApplication != null) {
                        wordApplication.invoke("Quit", new Variant[] {});
                        wordApplication.release();
                    }
                }
            }
        }

    }

    private void replaceBookmarksWithValues(Dispatch wordDocument, VariableProvider variableProvider, MsWordReportTaskSettings settings) {
        Dispatch bookmarks = Dispatch.get(wordDocument, "Bookmarks").toDispatch();
        for (BookmarkVariableMapping mapping : settings.getMappings()) {
            String value = getVariableValue(mapping.getVariableName(), settings.isStrictMode());
            if (value != null) {
                try {
                    Dispatch bookmark = Dispatch.call(bookmarks, "Item", mapping.getBookmarkName()).toDispatch();
                    Dispatch range = Dispatch.get(bookmark, "Range").toDispatch();
                    Dispatch.put(range, "Text", value);
                } catch (Exception e) {
                    if (settings.isStrictMode()) {
                        log.error("", e);
                        throw new MsWordReportException(MsWordReportException.BOOKMARK_NOT_FOUND_IN_TEMPLATE, mapping.getBookmarkName());
                    }
                    log.warn("No bookmark found in template document by name '" + mapping.getBookmarkName() + "'");
                }
            }
        }
        int bookmarksCount = Dispatch.get(bookmarks, "Count").toInt();
        int bookmarkIndex = 1;
        for (int i = 0; i < bookmarksCount; i++) {
            Dispatch bookmark = Dispatch.call(bookmarks, "Item", new Integer(bookmarkIndex)).toDispatch();
            String bookmarkName = Dispatch.get(bookmark, "Name").getString();
            log.warn("Bookmark exists in result document: '" + bookmarkName + "'");
            String value = getVariableValue(bookmarkName, settings.isStrictMode());
            if (value != null) {
                Dispatch range = Dispatch.get(bookmark, "Range").toDispatch();
                Dispatch.put(range, "Text", value);
            } else {
                bookmarkIndex++;
            }
        }
    }
}
