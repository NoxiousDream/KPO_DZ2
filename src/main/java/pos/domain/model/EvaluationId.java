package pos.domain.model;

import java.util.UUID;

/**
 * Value Object: идентификатор факта оценивания.
 */
public record EvaluationId(UUID value) {
    public EvaluationId {
        if (value == null) {
            throw new IllegalArgumentException("EvaluationId не может быть null");
        }
    }

    public static EvaluationId newId() {
        return new EvaluationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString().substring(0, 8);
    }
}
