package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.Criterion;
import pos.domain.model.CriterionId;
import pos.domain.model.Rubric;
import pos.domain.model.Weight;

import static org.junit.jupiter.api.Assertions.*;

class RubricTest {

    private Rubric rubricWithHw() {
        Rubric rubric = new Rubric();
        rubric.addCriterion(new CriterionId("HW"), "Домашние задания", 10, new Weight(0.4));
        return rubric;
    }

    @Test
    void addCriterionStoresAllAttributes() {
        Rubric rubric = rubricWithHw();
        Criterion hw = rubric.require(new CriterionId("HW"));

        assertEquals("Домашние задания", hw.title());
        assertEquals(10, hw.maxScore(), 1e-9);
        assertEquals(0.4, hw.weight().value(), 1e-9);
        assertTrue(rubric.contains(new CriterionId("hw")));
    }

    @Test
    void duplicateCriterionIsRejected() {
        Rubric rubric = rubricWithHw();
        DomainException ex = assertThrows(DomainException.class,
                () -> rubric.addCriterion(new CriterionId("HW"), "Дубль", 5, new Weight(0.1)));
        assertTrue(ex.getMessage().contains("уже есть"));
    }

    @Test
    void unknownCriterionCannotBeRequired() {
        Rubric rubric = rubricWithHw();
        DomainException ex = assertThrows(DomainException.class,
                () -> rubric.require(new CriterionId("ORAL")));
        assertTrue(ex.getMessage().contains("нет в рубрикаторе"));
    }

    @Test
    void invalidMaxScoreIsRejected() {
        Rubric rubric = new Rubric();
        assertThrows(DomainException.class,
                () -> rubric.addCriterion(new CriterionId("HW"), "ДЗ", 0, new Weight(0.4)));
        assertThrows(DomainException.class,
                () -> rubric.addCriterion(new CriterionId("EX"), "Экзамен", -10, new Weight(0.6)));
    }

    @Test
    void blankTitleIsRejected() {
        Rubric rubric = new Rubric();
        assertThrows(DomainException.class,
                () -> rubric.addCriterion(new CriterionId("HW"), "   ", 10, new Weight(0.4)));
    }

    @Test
    void criteriaListIsImmutableSnapshot() {
        Rubric rubric = rubricWithHw();
        var snapshot = rubric.criteria();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(null));

        rubric.addCriterion(new CriterionId("EXAM"), "Экзамен", 10, new Weight(0.6));
        assertEquals(1, snapshot.size());
        assertEquals(2, rubric.criteria().size());
    }
}
