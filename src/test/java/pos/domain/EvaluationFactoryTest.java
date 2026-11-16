package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationFactoryTest {

    private final Rubric rubric = rubric();
    private final StudentId student = new StudentId("231001");

    private static Rubric rubric() {
        Rubric r = new Rubric();
        r.addCriterion(new CriterionId("HW"), "Домашние задания", 10, new Weight(0.4));
        r.addCriterion(new CriterionId("EXAM"), "Экзамен", 10, new Weight(0.6));
        return r;
    }

    @Test
    void factoryCreatesEvaluationWithDefaults() {
        Evaluation e = Evaluation.create(rubric, student, new CriterionId("HW"),
                new Score(8), null, "  стабильно  ");

        assertEquals(student, e.studentId());
        assertEquals(new CriterionId("HW"), e.criterionId());
        assertEquals(8, e.score().value(), 1e-9);
        assertEquals(LocalDate.now(), e.date());
        assertEquals("стабильно", e.comment().orElseThrow());
        assertNotNull(e.id());
    }

    @Test
    void blankCommentBecomesEmpty() {
        Evaluation e = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, "   ");
        assertTrue(e.comment().isEmpty());
    }

    @Test
    void scoreAboveCriterionMaxIsRejected() {
        DomainException ex = assertThrows(DomainException.class,
                () -> Evaluation.create(rubric, student, new CriterionId("HW"), new Score(12), null, null));
        assertTrue(ex.getMessage().contains("превышает"));
    }

    @Test
    void criterionOutsideRubricIsRejected() {
        DomainException ex = assertThrows(DomainException.class,
                () -> Evaluation.create(rubric, student, new CriterionId("ORAL"), new Score(5), null, null));
        assertTrue(ex.getMessage().contains("нет в рубрикаторе"));
    }

    @Test
    void rescoreUpdatesScoreWithinBounds() {
        Evaluation e = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, null);
        e.rescore(rubric, new Score(10));
        assertEquals(10, e.score().value(), 1e-9);
    }

    @Test
    void failedRescoreKeepsPreviousScore() {
        Evaluation e = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, null);

        assertThrows(DomainException.class, () -> e.rescore(rubric, new Score(12)));

        assertEquals(8, e.score().value(), 1e-9);
    }

    @Test
    void explicitDateIsKept() {
        LocalDate date = LocalDate.of(2026, 5, 1);
        Evaluation e = Evaluation.create(rubric, student, new CriterionId("EXAM"), new Score(7), date, null);
        assertEquals(date, e.date());
    }
}
