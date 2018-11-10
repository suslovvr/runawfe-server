package ru.runa.wfe.service.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wf.logic.bot.BotStationResources;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.invoker.BotInvokerFactory;
import ru.runa.wfe.service.BotInvokerService;
import ru.runa.wfe.service.delegate.Delegates;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@WebService(name = "BotInvokerAPI", serviceName = "BotInvokerWebService")
@SOAPBinding
@CommonsLog
public class BotInvokerServiceBean implements BotInvokerService {
    @Resource
    private TimerService timerService;
    private boolean firstInvocation;

    @Override
    public synchronized void startPeriodicBotsInvocation(BotStation botStation) {
        if (!isRunning()) {
            log.info("Starting periodic bot execution...");
            firstInvocation = true;
            timerService.createTimer(0, BotStationResources.getBotInvocationPeriod(), botStation.getId());
        } else {
            log.info("BotRunner is running. skipping start...");
        }
    }

    @Override
    public boolean isRunning() {
        return timerService.getTimers().size() > 0;
    }

    @Override
    public synchronized void cancelPeriodicBotsInvocation() {
        if (isRunning()) {
            log.info("Canceling periodic bot execution...");
            for (Timer timer : timerService.getTimers()) {
                timer.cancel();
            }
        } else {
            log.info("BotRunner is not running. skipping cancel...");
        }
    }

    @Override
    public void invokeBots(BotStation botStation) {
        invokeBotsImpl(botStation, false);
    }

    @WebMethod(exclude = true)
    @Timeout
    public void timeOutHandler(Timer timer) {
        try {
            // refresh version and check that bot station exists
            BotStation botStation = Delegates.getBotService().getBotStation((Long) timer.getInfo());
            invokeBotsImpl(botStation, firstInvocation);
            firstInvocation = false;
        } catch (BotStationDoesNotExistException e) {
            log.warn("Cancelling periodic invocation due to: " + e);
            timer.cancel();
        }
    }

    private static void invokeBotsImpl(BotStation botStation, boolean resetFailedDelay) {
        try {
            log.debug("Invoking bots... resetFailedDelay = " + resetFailedDelay);
            BotInvokerFactory.getBotInvoker().invokeBots(botStation, resetFailedDelay);
        } catch (Throwable th) {
            log.error("Unable to invoke bots", th);
        }
    }

}
