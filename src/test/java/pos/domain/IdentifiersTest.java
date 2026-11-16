package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.CriterionId;
import pos.domain.model.EvaluationId;
import pos.domain.model.StudentId;

import static org.junit.jupiter.api.Assertions.*;

class IdentifiersTest {

    @Test
    void studentIdIsTrimmed() {
        assertEquals("231001", new StudentId("  231001  ").value());
    }

    @Test
    void blankStudentIdIsRejected() {
        assertThrows(DomainException.class, () -> new StudentId("   "));
        assertThrows(DomainException.class, () -> new StudentId(null));
    }

    @Test
    void criterionIdIsNormalizedToUpperCase() {
        assertEquals("HW", new CriterionId(" hw ").value());
        assertEquals(new CriterionId("hw"), new CriterionId("HW"));
    }

    @Test
    void blankCriterionIdIsRejected() {
        assertThrows(DomainException.class, () -> new CriterionId(""));
    }

    @Test
    void evaluationIdsAreUnique() {
        assertNotEquals(EvaluationId.newId(), EvaluationId.newId());
    }
}
