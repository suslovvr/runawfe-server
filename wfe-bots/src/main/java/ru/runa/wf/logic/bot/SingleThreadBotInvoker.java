package ru.runa.wf.logic.bot;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.invoker.BotInvoker;
import ru.runa.wfe.commons.CoreErrorProperties;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

@CommonsLog
public class SingleThreadBotInvoker implements BotInvoker {
    private List<WorkflowBotExecutor> botExecutors;
    private long configurationVersion = -1;
    private BotStation botStation;

    @Override
    public synchronized void invokeBots(BotStation botStation, boolean resetFailedDelay) {
        this.botStation = Delegates.getBotService().getBotStation(botStation.getId());
        logBotsActivites();
        configure();
        for (WorkflowBotExecutor botExecutor : botExecutors) {
            try {
                if (resetFailedDelay) {
                    botExecutor.resetFailedDelay();
                }
                Set<WfTask> tasks = botExecutor.getNewTasks();
                for (WfTask task : tasks) {
                    WorkflowBotTaskExecutor botTaskExecutor = botExecutor.createBotTaskExecutor(task);
                    botTaskExecutor.run();
                }
            } catch (AuthenticationException e) {
                configurationVersion = -1;
                log.error("BotRunner execution failed. Will recreate botstation settings and bots.", e);
            } catch (Exception e) {
                log.error("BotRunner execution failed.", e);
            }
        }
    }

    private void configure() {
        String botStationErrorMessage = CoreErrorProperties.getMessage(CoreErrorProperties.BOT_STATION_CONFIGURATION_ERROR, botStation.getName());
        try {
            if (botStation.getVersion() != configurationVersion) {
                botExecutors = Lists.newArrayList();
                log.info("Will update bots configuration.");
                String username = BotStationResources.getSystemUsername();
                String password = BotStationResources.getSystemPassword();
                User botStationUser = Delegates.getAuthenticationService().authenticateByLoginPassword(username, password);
                List<Bot> bots = Delegates.getBotService().getBots(botStationUser, botStation.getId());
                for (Bot bot : bots) {
                    String botErrorMessage = CoreErrorProperties.getMessage(CoreErrorProperties.BOT_CONFIGURATION_ERROR, bot.getUsername());
                    try {
                        log.info("Configuring " + bot.getUsername());
                        User user = Delegates.getAuthenticationService().authenticateByLoginPassword(bot.getUsername(), bot.getPassword());
                        List<BotTask> tasks = Delegates.getBotService().getBotTasks(user, bot.getId());
                        botExecutors.add(new WorkflowBotExecutor(user, bot, tasks));
                        Errors.removeSystemError(botErrorMessage);
                    } catch (Throwable th) {
                        log.error("Unable to configure " + bot, th);
                        Errors.addSystemError(new ConfigurationException(botErrorMessage, th));
                    }
                }
                configurationVersion = botStation.getVersion();
            } else {
                log.debug("bots configuration is up to date, version = " + botStation.getVersion());
            }
            Errors.removeSystemError(botStationErrorMessage);
        } catch (Throwable th) {
            log.error("Botstation configuration error", th);
            Errors.addSystemError(new ConfigurationException(botStationErrorMessage, th));
        }
    }

    private void logBotsActivites() {
        BotLogger botLogger = BotStationResources.createBotLogger();
        if (botLogger == null) {
            return;
        }
        botLogger.logActivity();
    }
}
