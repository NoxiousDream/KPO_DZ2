package pos.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.Score;
import pos.domain.model.StudentId;

import static org.junit.jupiter.api.Assertions.*;

class GradeStudentUseCaseTest {
    private final ApplicationFixture f = new ApplicationFixture();
    private final StudentId student = new StudentId("231001");
    private final CriterionId hw = new CriterionId("HW");

    @BeforeEach
    void setUp() {
        f.seedRubric();
        f.seedStudent("231001");
    }

    @Test
    void createsEvaluationThroughDomainFactory() {
        Evaluation evaluation = f.grade.execute(student, hw, new Score(8), "стабильно");

        assertEquals(8, evaluation.score().value(), 1e-9);
        assertEquals("стабильно", evaluation.comment().orElseThrow());
        assertEquals(1, f.evaluations.findAll().size());
    }

    @Test
    void repeatedGradeRescoresSameEvaluationInsteadOfDuplicating() {
        Evaluation first = f.grade.execute(student, hw, new Score(8), "стабильно");
        Evaluation second = f.grade.execute(student, hw, new Score(9), "исправлено");

        assertEquals(first.id(), second.id());
        assertEquals(1, f.evaluations.findAll().size());
        assertEquals(9, f.evaluations.findByStudent(student).get(0).score().value(), 1e-9);
        assertEquals("исправлено", second.comment().orElseThrow());
    }

    @Test
    void blankCommentClearsPreviousOne() {
        f.grade.execute(student, hw, new Score(8), "стабильно");
        Evaluation corrected = f.grade.execute(student, hw, new Score(9), "  ");

        assertTrue(corrected.comment().isEmpty());
    }

    @Test
    void scoreAboveCriterionMaxIsRejectedAndNothingSaved() {
        DomainException ex = assertThrows(DomainException.class,
                () -> f.grade.execute(student, hw, new Score(12), null));

        assertTrue(ex.getMessage().contains("превышает"));
        assertTrue(f.evaluations.findAll().isEmpty());
    }

    @Test
    void criterionOutsideRubricIsRejected() {
        DomainException ex = assertThrows(DomainException.class,
                () -> f.grade.execute(student, new CriterionId("ORAL"), new Score(5), null));

        assertTrue(ex.getMessage().contains("нет в рубрикаторе"));
        assertTrue(f.evaluations.findAll().isEmpty());
    }

    @Test
    void unknownStudentIsRejected() {
        DomainException ex = assertThrows(DomainException.class,
                () -> f.grade.execute(new StudentId("999999"), hw, new Score(5), null));

        assertTrue(ex.getMessage().contains("нет на курсе"));
        assertTrue(f.evaluations.findAll().isEmpty());
    }

    @Test
    void failedCorrectionKeepsPreviousScore() {
        f.grade.execute(student, hw, new Score(8), null);

        assertThrows(DomainException.class, () -> f.grade.execute(student, hw, new Score(12), null));

        assertEquals(8, f.evaluations.findByStudent(student).get(0).score().value(), 1e-9);
    }
}
