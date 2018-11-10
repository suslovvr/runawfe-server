package ru.runa.af.web.action;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.BotTasksForm;
import ru.runa.af.web.system.TaskHandlerClassesInformation;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/update_bot_tasks" name="botTasksForm" validate="true"
 *                input = "/WEB-INF/wf/bot.jsp"
 */
public class UpdateBotTasksAction extends ActionBase {
    public static final String UPDATE_BOT_TASKS_ACTION_PATH = "/update_bot_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        BotTasksForm form = (BotTasksForm) actionForm;
        try {
            List<BotTask> tasks = Delegates.getBotService().getBotTasks(getLoggedUser(request), form.getId());
            Set<Long> checkedIdSet = Sets.newHashSet(form.getIds());
            List<BotTask> tasksToDelete = Lists.newArrayList();
            List<BotTask> tasksToUpdate = Lists.newArrayList();
            for (BotTask task : tasks) {
                if (!checkedIdSet.contains(task.getId())) {
                    tasksToDelete.add(task);
                } else {
                    BotTasksForm.BotTaskForm updatedTask = form.getBotTaskNotNull(task.getId());
                    task.setName(updatedTask.getName());
                    if (TaskHandlerClassesInformation.isValid(updatedTask.getHandler())) {
                        task.setTaskHandlerClassName(updatedTask.getHandler());
                    }
                    task.setConfiguration(updatedTask.getConfigFile().getFileData());
                    task.setSequentialExecution(updatedTask.isSequential());
                    tasksToUpdate.add(task);
                }
            }
            for (BotTask task : tasksToDelete) {
                Delegates.getBotService().removeBotTask(getLoggedUser(request), task.getId());
            }
            for (BotTask task : tasksToUpdate) {
                Delegates.getBotService().updateBotTask(getLoggedUser(request), task);
            }
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/bot.do?botId=" + form.getId());
    }
}
