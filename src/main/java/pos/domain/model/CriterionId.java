package pos.domain.model;

import pos.domain.exception.DomainException;

/**
 * Value Object: код критерия в рубрикаторе, например "HW" или "EXAM".
 */
public record CriterionId(String value) {
    public CriterionId {
        if (value == null || value.isBlank()) {
            throw new DomainException("Код критерия не может быть пустым");
        }
        value = value.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return value;
    }
}
