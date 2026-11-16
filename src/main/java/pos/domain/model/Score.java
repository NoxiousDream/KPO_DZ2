package pos.domain.model;

import pos.domain.exception.DomainException;

/**
 * Value Object: балл. Инвариант «нельзя 12 из 10» охраняется здесь,
 * при создании значения, а не где-то в UI.
 */
public record Score(double value) implements Comparable<Score> {
    public Score {
        if (!Double.isFinite(value)) {
            throw new DomainException("Балл должен быть конечным числом");
        }
        if (value < 0) {
            throw new DomainException("Балл не может быть отрицательным: " + value);
        }
    }

    /** Проверка верхней границы конкретным критерием. */
    public void requireWithinMax(double maxScore) {
        if (value > maxScore) {
            throw new DomainException(
                    "Балл " + value + " превышает максимум критерия " + maxScore
            );
        }
    }

    @Override
    public int compareTo(Score other) {
        return Double.compare(value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
