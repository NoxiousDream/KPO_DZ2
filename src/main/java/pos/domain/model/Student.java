package pos.domain.model;

import pos.domain.exception.DomainException;

import java.util.Objects;

/**
 * Entity: студент — кого оценивают. Идентичность по StudentId, имя можно исправить.
 */
public final class Student {
    private final StudentId id;
    private String fullName;
    private String group;

    public Student(StudentId id, String fullName, String group) {
        this.id = Objects.requireNonNull(id, "id");
        rename(fullName);
        changeGroup(group);
    }

    public StudentId id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String group() {
        return group;
    }

    public void rename(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainException("ФИО студента не может быть пустым");
        }
        this.fullName = fullName.trim();
    }

    public void changeGroup(String group) {
        if (group == null || group.isBlank()) {
            throw new DomainException("Группа студента не может быть пустой");
        }
        this.group = group.trim();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Student other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return fullName + " [" + id + ", " + group + "]";
    }
}
