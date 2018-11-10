package ru.runa.wfe.bot.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.bot.QBotTask;
import ru.runa.wfe.commons.dao.GenericDao;

/**
 * DAO level interface for managing bot tasks.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 * @since 2.0
 */
@Component
public class BotTaskDao extends GenericDao<BotTask> {

    public BotTaskDao() {
        super(BotTask.class);
    }

    @Override
    protected void checkNotNull(BotTask entity, Object identity) {
        if (entity == null) {
            throw new BotTaskDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * Load {@linkplain BotTask} from database by bot and name.
     * 
     * @return loaded {@linkplain BotTask} or <code>null</code>
     */
    public BotTask get(Bot bot, String name) {
        val bt = QBotTask.botTask;
        return queryFactory.selectFrom(bt).where(bt.bot.eq(bot).and(bt.name.eq(name))).fetchFirst();
    }

    /**
     * Load {@linkplain BotTask} from database by bot and name.
     * 
     * @return loaded {@linkplain BotTask}, not <code>null</code>
     */
    public BotTask getNotNull(Bot bot, String name) {
        BotTask botTask = get(bot, name);
        checkNotNull(botTask, name);
        return botTask;
    }

    /**
     * Load all {@linkplain BotTask}s defined for {@linkplain Bot}.
     * 
     * @return list, not <code>null</code>.
     */
    public List<BotTask> getAll(Bot bot) {
        val bt = QBotTask.botTask;
        return queryFactory.selectFrom(bt).where(bt.bot.eq(bot)).fetch();
    }
}
