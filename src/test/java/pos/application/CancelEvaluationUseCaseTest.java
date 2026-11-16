package pos.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.CriterionId;
import pos.domain.model.Score;
import pos.domain.model.StudentId;

import static org.junit.jupiter.api.Assertions.*;

class CancelEvaluationUseCaseTest {
    private final ApplicationFixture f = new ApplicationFixture();
    private final StudentId student = new StudentId("231001");
    private final CriterionId hw = new CriterionId("HW");

    @BeforeEach
    void setUp() {
        f.seedRubric();
        f.seedStudent("231001");
    }

    @Test
    void cancelsExistingEvaluation() {
        f.grade.execute(student, hw, new Score(8), null);

        f.cancel.execute(student, hw);

        assertTrue(f.evaluations.findAll().isEmpty());
    }

    @Test
    void cancellingMissingEvaluationIsRejected() {
        DomainException ex = assertThrows(DomainException.class, () -> f.cancel.execute(student, hw));

        assertTrue(ex.getMessage().contains("нет оценки"));
    }
}
