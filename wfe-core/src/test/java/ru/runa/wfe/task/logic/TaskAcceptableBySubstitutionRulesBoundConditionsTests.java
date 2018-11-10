package ru.runa.wfe.task.logic;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Test
@ContextConfiguration(locations = { "classpath:ru/runa/wfe/task/logic/test.context.xml" })
@CommonsLog
public class TaskAcceptableBySubstitutionRulesBoundConditionsTests extends AbstractTestNGSpringContextTests {

    @Autowired
    ITaskListBuilderTestProvider taskListBuilder;

    @DataProvider(name = "testcases")
    public Object[][] getTestcases() {
        return new Object[][] { { "terminate after unsatisfied substitution", false, new TaskAcceptableTestCaseDataSet() {
            @Override
            public void mockRules(SubstitutionLogic substitutionLogic) {
                when(substitutionLogic.getSubstitutors(assignedActor)).thenReturn(mapOfSubstitionRule);
                createUnsatisfiedSubstitutionRuleEntry(1L, mapOfSubstitionRule, actors);
                setActorInactive(1L);
                createSatisfiedTerminatorSubstitutionRuleEntry(2L, mapOfSubstitionRule, actors);
            }

        } }, { "terminate after satisfied substitution", true, new TaskAcceptableTestCaseDataSet() {
            @Override
            public void mockRules(SubstitutionLogic substitutionLogic) {
                when(substitutionLogic.getSubstitutors(assignedActor)).thenReturn(mapOfSubstitionRule);
                createSatisfiedSubstitutionRuleEntry(1L, mapOfSubstitionRule, actors);
                createSatisfiedTerminatorSubstitutionRuleEntry(2L, mapOfSubstitionRule, actors);
                setSubstituteActor(1L);
                when(substitutorActor.isActive()).thenReturn(true);
            }

        } }, { "terminate before satisfied substitution", false, new TaskAcceptableTestCaseDataSet() {
            @Override
            public void mockRules(SubstitutionLogic substitutionLogic) {
                when(substitutionLogic.getSubstitutors(assignedActor)).thenReturn(mapOfSubstitionRule);
                createSatisfiedTerminatorSubstitutionRuleEntry(1L, mapOfSubstitionRule, actors);
                createSatisfiedSubstitutionRuleEntry(2L, mapOfSubstitionRule, actors);
                setSubstituteActor(2L);
                when(substitutorActor.isActive()).thenReturn(true);
            }

        } }, { "try unsatisfied terminate before satisfied substitution", true, new TaskAcceptableTestCaseDataSet() {
            @Override
            public void mockRules(SubstitutionLogic substitutionLogic) {
                when(substitutionLogic.getSubstitutors(assignedActor)).thenReturn(mapOfSubstitionRule);
                createUnsatisfiedTerminatorSubstitutionRuleEntry(1L, mapOfSubstitionRule, actors);
                createSatisfiedSubstitutionRuleEntry(2L, mapOfSubstitionRule, actors);
                setSubstituteActor(2L);
                when(substitutorActor.isActive()).thenReturn(true);
            }

        } } };
    }

    @Test(dataProvider = "testcases")
    void runTests(String testName, boolean expected, TestCaseDataSet testCase) {

        log.info(String.format("start test: %s", testName));

        TaskLogicMockFactory.getFactory().setContextRules(testCase);

        boolean res = taskListBuilder.isTaskAcceptableBySubstitutionRules(testCase.getExeContext(), testCase.getTask(), testCase.getAssignedActor(),
                testCase.getSubstitutorActor());

        Assert.assertEquals(res, expected);

        TaskLogicMockFactory.getFactory().setContextRules(null);
    }

    public static class TaskAcceptableTestCaseDataSet extends TestCaseDataSet {
        Task task = mock(Task.class);
        Actor assignedActor = mock(Actor.class);
        Actor substitutorActor = mock(Actor.class);
        TreeMap<Substitution, Set<Long>> mapOfSubstitionRule = new TreeMap<>();
        Map<Long, Actor> actors = Maps.newHashMap();

        @Override
        public void mockRules(ExecutorDao executorDao) {
            for (Map.Entry<Long, Actor> entry : actors.entrySet()) {
                when(executorDao.getActor(entry.getKey())).thenReturn(entry.getValue());
            }
        }

        @Override
        public Task getTask() {
            return task;
        }

        @Override
        public Actor getAssignedActor() {
            return assignedActor;
        }

        @Override
        public Actor getSubstitutorActor() {
            return substitutorActor;
        }

        public void setActorInactive(Long id) {
            Actor actor = actors.get(id);
            if (actor == null) {
                return;
            }
            reset(actor);
            when(actor.isActive()).thenReturn(false);
        }

        public void setSubstituteActor(Long id) {
            actors.put(id, substitutorActor);
        }
    }

    public static void createSatisfiedSubstitutionRuleEntry(Long id, TreeMap<Substitution, Set<Long>> out, Map<Long, Actor> actors) {
        Substitution substitution = mock(Substitution.class);
        SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
        when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), any(Actor.class))).thenReturn(true);
        when(substitution.getCriteria()).thenReturn(criteria);
        Actor actor = mock(Actor.class);
        when(actor.isActive()).thenReturn(true);
        Set<Long> ids = Sets.newHashSet();
        ids.add(id);
        actors.put(id, actor);
        out.put(substitution, ids);
    }

    public static void createUnsatisfiedSubstitutionRuleEntry(Long id, TreeMap<Substitution, Set<Long>> out, Map<Long, Actor> actors) {
        Substitution substitution = mock(Substitution.class);
        SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
        when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), any(Actor.class))).thenReturn(false);
        when(substitution.getCriteria()).thenReturn(criteria);
        Actor actor = mock(Actor.class);
        when(actor.isActive()).thenReturn(true);
        Set<Long> ids = Sets.newHashSet();
        ids.add(id);
        actors.put(id, actor);
        out.put(substitution, ids);
    }

    public static void createSatisfiedTerminatorSubstitutionRuleEntry(Long id, TreeMap<Substitution, Set<Long>> out, Map<Long, Actor> actors) {
        Substitution substitution = mock(TerminatorSubstitution.class);
        SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
        when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), any(Actor.class))).thenReturn(true);
        when(substitution.getCriteria()).thenReturn(criteria);
        Actor actor = mock(Actor.class);
        when(actor.isActive()).thenReturn(true);
        Set<Long> ids = Sets.newHashSet();
        ids.add(id);
        actors.put(id, actor);
        out.put(substitution, ids);
    }

    public static void createUnsatisfiedTerminatorSubstitutionRuleEntry(Long id, TreeMap<Substitution, Set<Long>> out, Map<Long, Actor> actors) {
        Substitution substitution = mock(TerminatorSubstitution.class);
        SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
        when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), any(Actor.class))).thenReturn(false);
        when(substitution.getCriteria()).thenReturn(criteria);
        Actor actor = mock(Actor.class);
        when(actor.isActive()).thenReturn(true);
        Set<Long> ids = Sets.newHashSet();
        ids.add(id);
        actors.put(id, actor);
        out.put(substitution, ids);
    }
}
