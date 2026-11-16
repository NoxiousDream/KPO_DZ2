package pos.domain.model;

import pos.domain.exception.DomainException;

/**
 * Value Object: вклад критерия в итог. Вес строго положительный;
 * нормализация (если нужна) — забота стратегии итога, а не веса.
 */
public record Weight(double value) {
    public Weight {
        if (!Double.isFinite(value) || value <= 0) {
            throw new DomainException("Вес критерия должен быть положительным числом: " + value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
