package pos.domain.service.strategy;

import pos.domain.service.GradedCriterion;
import pos.domain.service.TotalScoreStrategy;

import java.util.List;

/**
 * Базовое правило: итог = Σ (балл × вес критерия).
 * Веса задаёт преподаватель; сумма весов не обязана быть 1.
 */
public final class WeightedSumStrategy implements TotalScoreStrategy {
    @Override
    public String name() {
        return "Взвешенная сумма (Σ балл × вес)";
    }

    @Override
    public double calculate(List<GradedCriterion> graded) {
        return graded.stream().mapToDouble(this::part).sum();
    }

    @Override
    public double part(GradedCriterion g) {
        return g.evaluation().score().value() * g.criterion().weight().value();
    }
}
