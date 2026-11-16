package pos.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.CriterionId;
import pos.domain.model.Score;
import pos.domain.model.StudentId;
import pos.domain.model.StudentTotal;
import pos.domain.service.strategy.NormalizedToTenStrategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculateTotalUseCaseTest {
    private final ApplicationFixture f = new ApplicationFixture();
    private final StudentId student = new StudentId("231001");
    private final CriterionId hw = new CriterionId("HW");
    private final CriterionId exam = new CriterionId("EXAM");

    @BeforeEach
    void setUp() {
        f.seedRubric();
        f.seedStudent("231001");
    }

    @Test
    void totalIsConsistentWithCurrentEvaluations() {
        f.grade.execute(student, hw, new Score(8), null);
        f.grade.execute(student, exam, new Score(15), null);

        StudentTotal total = f.totals.forStudent(student);

        assertEquals(9.2, total.value(), 1e-9);      // 8*0.4 + 15*0.4
        assertTrue(total.isComplete());
        assertEquals(2, total.gradedCriteria());
    }

    @Test
    void rescoreIsImmediatelyReflectedInTotal() {
        f.grade.execute(student, hw, new Score(8), null);
        f.grade.execute(student, exam, new Score(15), null);
        assertEquals(9.2, f.totals.forStudent(student).value(), 1e-9);

        f.grade.execute(student, hw, new Score(9), null);

        assertEquals(9.6, f.totals.forStudent(student).value(), 1e-9);
    }

    @Test
    void cancelIsImmediatelyReflectedInTotal() {
        f.grade.execute(student, hw, new Score(8), null);
        f.grade.execute(student, exam, new Score(15), null);

        f.cancel.execute(student, exam);

        StudentTotal total = f.totals.forStudent(student);
        assertEquals(3.2, total.value(), 1e-9);
        assertFalse(total.isComplete());
        assertEquals(1, total.gradedCriteria());
        assertEquals(2, total.totalCriteria());
    }

    @Test
    void unknownStudentIsRejected() {
        DomainException ex = assertThrows(DomainException.class,
                () -> f.totals.forStudent(new StudentId("999999")));

        assertTrue(ex.getMessage().contains("нет на курсе"));
    }

    @Test
    void forAllStudentsReturnsTotalPerStudent() {
        f.seedStudent("231002");
        f.grade.execute(student, hw, new Score(8), null);

        List<StudentTotal> totals = f.totals.forAllStudents();

        assertEquals(2, totals.size());
        assertEquals(3.2, totals.get(0).value(), 1e-9);
        assertEquals(0.0, totals.get(1).value(), 1e-9);
        assertFalse(totals.get(1).isComplete());
    }

    @Test
    void injectedStrategyDefinesTheRule() {
        ApplicationFixture normalized = new ApplicationFixture(new NormalizedToTenStrategy());
        normalized.seedRubric();
        normalized.seedStudent("231001");
        normalized.grade.execute(student, hw, new Score(8), null);
        normalized.grade.execute(student, exam, new Score(15), null);

        StudentTotal total = normalized.totals.forStudent(student);

        // 10 * 9.2 / (10*0.4 + 20*0.4) = 7.666...
        assertEquals(7.6666666667, total.value(), 1e-6);
        assertTrue(total.strategyName().contains("Нормализация"));
    }
}
