package pos.domain.service;

import java.util.List;

/**
 * GoF (поведенческий): Strategy — сменяемое правило пересчёта итога.
 */
public interface TotalScoreStrategy {
    String name();

    double calculate(List<GradedCriterion> graded);

    /** Вклад одного критерия в итог — для детализации. */
    double part(GradedCriterion graded);
}
