package pos.domain.service.strategy;

import pos.domain.service.GradedCriterion;
import pos.domain.service.TotalScoreStrategy;

import java.util.List;

/**
 * Альтернативное правило: итог приводится к шкале 0..10
 * как средневзвешенная доля от максимумов: 10 × Σ(балл×вес) / Σ(макс×вес).
 * Считается по ВСЕМ критериям рубрикатора? Нет — по оценённым,
 * но с учётом полного набора максимумов переданных пар.
 */
public final class NormalizedToTenStrategy implements TotalScoreStrategy {
    @Override
    public String name() {
        return "Нормализация к 10 (10 × Σ балл·вес / Σ макс·вес)";
    }

    @Override
    public double calculate(List<GradedCriterion> graded) {
        double weightedSum = graded.stream().mapToDouble(this::part).sum();
        double weightedMax = graded.stream()
                .mapToDouble(g -> g.criterion().maxScore() * g.criterion().weight().value())
                .sum();
        return weightedMax == 0 ? 0 : 10.0 * weightedSum / weightedMax;
    }

    @Override
    public double part(GradedCriterion g) {
        return g.evaluation().score().value() * g.criterion().weight().value();
    }
}
