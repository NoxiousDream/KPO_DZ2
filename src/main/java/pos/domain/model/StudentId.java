package pos.domain.model;

import pos.domain.exception.DomainException;

/**
 * Value Object: типизированный идентификатор студента (зачётная книжка / номер).
 */
public record StudentId(String value) {
    public StudentId {
        if (value == null || value.isBlank()) {
            throw new DomainException("Идентификатор студента не может быть пустым");
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
