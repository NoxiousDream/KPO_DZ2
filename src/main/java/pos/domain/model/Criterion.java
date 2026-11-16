package pos.domain.model;

import pos.domain.exception.DomainException;

import java.util.Objects;

/**
 * Entity внутри агрегата Rubric: за что ставят балл.
 * Снаружи агрегата создаётся только через Rubric.addCriterion(...).
 */
public final class Criterion {
    private final CriterionId id;
    private final String title;
    private final double maxScore;
    private final Weight weight;

    Criterion(CriterionId id, String title, double maxScore, Weight weight) {
        this.id = Objects.requireNonNull(id, "id");
        if (title == null || title.isBlank()) {
            throw new DomainException("Название критерия не может быть пустым");
        }
        if (!Double.isFinite(maxScore) || maxScore <= 0) {
            throw new DomainException("Максимум критерия должен быть положительным: " + maxScore);
        }
        this.title = title.trim();
        this.maxScore = maxScore;
        this.weight = Objects.requireNonNull(weight, "weight");
    }

    public CriterionId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public double maxScore() {
        return maxScore;
    }

    public Weight weight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Criterion other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + " «" + title + "» (макс " + maxScore + ", вес " + weight + ")";
    }
}
