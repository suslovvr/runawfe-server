package ru.runa.wfe.security;

import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.execution.QArchivedProcess;
import ru.runa.wfe.execution.QCurrentProcess;
import ru.runa.wfe.report.QReportDefinition;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.QExecutor;

/**
 * Incapsulates all methods of SecuredObject construction polymorphic by SecuredObjectType.
 * <p>
 * Supports permission system extensibility (adding new SecuredObjectType and Permission pseudo-enum items), by using pluggable lambda loaders.
 *
 * @see SecuredObjectType
 * @see ru.runa.wfe.security.Permission
 * @see ru.runa.wfe.security.ApplicablePermissions
 * @see ru.runa.wfe.security.PermissionSubstitutions
 */
@Component
public class SecuredObjectFactory {

    private static SecuredObjectFactory instance;

    public static SecuredObjectFactory getInstance() {
        return instance;
    }

    public SecuredObjectFactory() {
        instance = this;
    }

    // When I added @Transactional annotation, @Autowired queryFactory field became null.
    private static HibernateQueryFactory getQueryFactory() {
        return HibernateQueryFactory.getInstance();
    }


    public static abstract class Loader {
        protected final SecuredObjectType type;

        public Loader(SecuredObjectType type) {
            this.type = type;
        }

        abstract SecuredObject findById(Long id);

//        abstract SecuredObject getByName(String name);

        /**
         * @return Each tuple contains 2 fields: Long id and String name.
         */
        List<Tuple> getByNames(Set<String> names) {
            throw new NotImplementedException("Not implemented for type " + type.getName());
        }
    }

    private static class SingletonLoader extends Loader {
        private final SecuredObject object;

        SingletonLoader(SecuredObject object) {
            super(object.getSecuredObjectType());
            this.object = object;
        }

        @Override
        public SecuredObject findById(Long id) {
            if (id != null && id != 0) {
                throw new RuntimeException("id = " + id.toString() + ", expected 0 or null");
            }
            return object;
        }
//        @Override
//        SecuredObject getByName(String name) {
//            if (name != null) {
//                throw new RuntimeException("name = \"" + name + "\", expected null");
//            }
//            return instance;
//        }
        @Override
        List<Tuple> getByNames(Set<String> names) {
            throw new UnsupportedOperationException("Called for singleton type " + object.getSecuredObjectType().getName());
        }
    }

    private static HashMap<SecuredObjectType, Loader> loaders = new HashMap<>();

    private static Loader getLoader(SecuredObjectType type) {
        Loader loader = loaders.get(type);
        if (loader == null) {
            throw new RuntimeException("Loader does not exist for type " + type);
        }
        return loader;
    }


    /**
     * Loads entity or returns singleton.
     *
     * @param id
     *            Boxed Long because it is met in many places in JSP tags. Must be null or 0 if requesting singleton.
     * @return Null if object does not exist.
     * @throws RuntimeException
     *             If type is unknown, or singleton is requested with non-empty id, or exception from object loader.
     */
    public SecuredObject findById(SecuredObjectType type, Long id) {
        return getLoader(type).findById(id);
    }

//    /**
//     * Loads entity or returns singleton.
//     *
//     * @param name Must be null if singleton is requested.
//     * @return Null if object does not exist.
//     * @throws RuntimeException If type is unknown, or singleton is requested with non-null name, or exception from object loader.
//     */
//    public static SecuredObject getByName(User user, SecuredObjectType type, String name) {
//        return getLoader(type).getByName(user, name);
//    }

    /**
     * @param names No partitioning is done, so pass small sets.
     * @return Not null, ids are not null and unique (because names is set), collection size equals to names set size.
     * @throws RuntimeException If type is unknown or singleton, or if some of the names could not be found.
     */
    public List<Long> getIdsByNames(SecuredObjectType type, Set<String> names) {
        List<Tuple> tt = getLoader(type).getByNames(names);
        List<Long> foundIds = new ArrayList<>(tt.size());
        Set<String> missingNames = new HashSet<>(names);
        for (Tuple t : tt) {
            foundIds.add(t.get(0, Long.class));
            missingNames.remove(t.get(1, String.class));
        }
        if (!missingNames.isEmpty()) {
            List<String> sorted = new ArrayList<>(missingNames);
            Collections.sort(sorted);
            throw new RuntimeException("Could not find objects of type " + type.getName() + " by name(s): " + String.join(", ", sorted));
        }
        return foundIds;
    }

    public static void add(SecuredObjectType type, Loader loader) {
        if (loaders.put(type, loader) != null) {
            throw new RuntimeException("Loader already existed for type " + type);
        }
    }

    public static void add(SecuredSingleton singleton) {
        add(singleton.getSecuredObjectType(), new SingletonLoader(singleton));
    }

    static {
        add(SecuredObjectType.ACTOR, new Loader(SecuredObjectType.ACTOR) {
            @Override
            public SecuredObject findById(Long id) {
                val e = QExecutor.executor;
                Executor o = getQueryFactory().selectFrom(e).where(e.id.eq(id)).fetchFirst();
                Assert.isTrue(o == null || o instanceof Actor);
                return o;
            }
//            @Override
//            SecuredObject getByName(String name) {
//                Executor o = instance.executorLogic.getExecutor(user, name);
//                Assert.isTrue(o == null || o instanceof Actor);
//                return o;
//            }
            @Override
            List<Tuple> getByNames(Set<String> names) {
                throw new UnsupportedOperationException("Called for ACTOR, but applicable only to fake EXECUTOR type: no subtype checks are done");
            }
        });

        add(SecuredSingleton.BOTSTATIONS);
        add(SecuredSingleton.DATAFILE);
        add(SecuredSingleton.DEFINITIONS);

        add(SecuredObjectType.DEFINITION, new Loader(SecuredObjectType.DEFINITION) {
            @Override
            public SecuredObject findById(Long id) {
                QProcessDefinition d = QProcessDefinition.processDefinition;
                return getQueryFactory().selectFrom(d).where(d.id.eq(id)).fetchFirst();
            }
//            @Override
//            SecuredObject getByName(User user, String name) {
//                return instance.definitionLogic.getLatestProcessDefinition(user, name);
//            }
            @Override
            List<Tuple> getByNames(Set<String> names) {
                QProcessDefinition d = QProcessDefinition.processDefinition;
                return getQueryFactory().select(d.id, d.name).from(d).where(d.name.in(names)).fetch();
            }
        });

        add(SecuredSingleton.ERRORS);

        add(SecuredObjectType.EXECUTOR, new Loader(SecuredObjectType.EXECUTOR) {
            @Override
            public SecuredObject findById(Long id) {
                val e = QExecutor.executor;
                return getQueryFactory().selectFrom(e).where(e.id.eq(id)).fetchFirst();
            }
//            @Override
//            SecuredObject getByName(User user, String name) {
//                Executor o = getInstance().executorLogic.getExecutor(user, name);
//                Assert.isTrue(o == null || o instanceof Actor);
//                return o;
//            }
            @Override
            List<Tuple> getByNames(Set<String> names) {
                val e = QExecutor.executor;
                return getQueryFactory().select(e.id, e.name).from(e).where(e.name.in(names)).fetch();
            }
        });

        add(SecuredSingleton.EXECUTORS);

        add(SecuredObjectType.GROUP, new Loader(SecuredObjectType.GROUP) {
            @Override
            public SecuredObject findById(Long id) {
                val e = QExecutor.executor;
                Executor o = getQueryFactory().selectFrom(e).where(e.id.eq(id)).fetchFirst();
                Assert.isTrue(o == null || o instanceof Group);
                return o;
            }
//            @Override
//            SecuredObject getByName(User user, String name) {
//                Executor o = getInstance().executorLogic.getExecutor(user, name);
//                Assert.isTrue(o == null || o instanceof Group);
//                return o;
//            }
            @Override
            List<Tuple> getByNames(Set<String> names) {
                throw new UnsupportedOperationException("Called for GROUP, but applicable only to fake EXECUTOR type: no subtype checks are done");
            }
        });

        add(SecuredSingleton.LOGS);

        add(SecuredObjectType.PROCESS, new Loader(SecuredObjectType.PROCESS) {
            @Override
            public SecuredObject findById(Long id) {
                val cp = QCurrentProcess.currentProcess;
                val result = getQueryFactory().selectFrom(cp).where(cp.id.eq(id)).fetchFirst();
                if (result != null) {
                    return result;
                }
                val ap = QArchivedProcess.archivedProcess;
                return getQueryFactory().selectFrom(ap).where(ap.id.eq(id)).fetchFirst();
            }
        });

        add(SecuredSingleton.PROCESSES);
        add(SecuredSingleton.RELATIONS);
        add(SecuredSingleton.REPORTS);

        add(SecuredObjectType.REPORT, new Loader(SecuredObjectType.REPORT) {
            @Override
            public SecuredObject findById(Long id) {
                val rd = QReportDefinition.reportDefinition;
                return new WfReport(getQueryFactory().selectFrom(rd).where(rd.id.eq(id)).fetchFirst());
            }
        });

        add(SecuredSingleton.SCRIPTS);
        add(SecuredSingleton.SUBSTITUTION_CRITERIAS);
        add(SecuredSingleton.SYSTEM);
    }
}
