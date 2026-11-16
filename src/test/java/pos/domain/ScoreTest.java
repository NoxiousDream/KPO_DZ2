package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.Score;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    void zeroScoreIsAllowed() {
        assertEquals(0.0, new Score(0).value(), 1e-9);
    }

    @Test
    void negativeScoreIsRejected() {
        DomainException ex = assertThrows(DomainException.class, () -> new Score(-0.5));
        assertTrue(ex.getMessage().contains("отрицательным"));
    }

    @Test
    void nonFiniteScoreIsRejected() {
        assertThrows(DomainException.class, () -> new Score(Double.NaN));
        assertThrows(DomainException.class, () -> new Score(Double.POSITIVE_INFINITY));
    }

    @Test
    void scoreEqualToMaxIsAllowed() {
        assertDoesNotThrow(() -> new Score(10).requireWithinMax(10));
    }

    @Test
    void scoreAboveMaxIsRejected() {
        DomainException ex = assertThrows(DomainException.class, () -> new Score(12).requireWithinMax(10));
        assertTrue(ex.getMessage().contains("превышает"));
    }

    @Test
    void scoresAreComparable() {
        assertTrue(new Score(7).compareTo(new Score(8)) < 0);
    }
}
