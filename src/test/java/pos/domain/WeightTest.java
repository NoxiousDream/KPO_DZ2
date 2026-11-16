package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.Weight;

import static org.junit.jupiter.api.Assertions.*;

class WeightTest {

    @Test
    void positiveWeightIsAllowed() {
        assertEquals(0.4, new Weight(0.4).value(), 1e-9);
    }

    @Test
    void zeroWeightIsRejected() {
        assertThrows(DomainException.class, () -> new Weight(0));
    }

    @Test
    void negativeWeightIsRejected() {
        assertThrows(DomainException.class, () -> new Weight(-0.1));
    }

    @Test
    void nonFiniteWeightIsRejected() {
        assertThrows(DomainException.class, () -> new Weight(Double.NaN));
    }
}
