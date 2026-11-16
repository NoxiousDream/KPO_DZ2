package pos.application;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.Criterion;
import pos.domain.model.CriterionId;
import pos.domain.model.Weight;

import static org.junit.jupiter.api.Assertions.*;

class AddCriterionUseCaseTest {
    private final ApplicationFixture f = new ApplicationFixture();

    @Test
    void addsCriterionAndSavesRubric() {
        Criterion criterion = f.addCriterion.execute(new CriterionId("HW"), "Домашние задания", 10, new Weight(0.4));

        assertEquals("HW", criterion.id().value());
        assertTrue(f.rubrics.load().contains(new CriterionId("HW")));
        assertEquals(1, f.rubrics.saveCalls());
    }

    @Test
    void duplicateCriterionIsRejected() {
        f.seedRubric();

        DomainException ex = assertThrows(DomainException.class,
                () -> f.addCriterion.execute(new CriterionId("HW"), "Дубль", 5, new Weight(0.1)));

        assertTrue(ex.getMessage().contains("уже есть"));
        assertEquals(2, f.rubrics.load().criteria().size());
    }

    @Test
    void nonPositiveMaxScoreIsRejected() {
        assertThrows(DomainException.class,
                () -> f.addCriterion.execute(new CriterionId("HW"), "ДЗ", 0, new Weight(0.4)));

        assertTrue(f.rubrics.load().isEmpty());
    }
}
