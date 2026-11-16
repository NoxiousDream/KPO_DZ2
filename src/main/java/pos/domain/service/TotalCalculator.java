package pos.domain.service;

import pos.domain.exception.DomainException;
import pos.domain.model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain Service: итог не принадлежит ни одной сущности — он собирается
 * из оценок студента и рубрикатора. Инвариант «итог согласован с текущим
 * набором оценок» обеспечивается тем, что итог всегда ВЫЧИСЛЯЕТСЯ здесь
 * и нигде не хранится.
 */
public final class TotalCalculator {
    private final TotalScoreStrategy strategy;

    public TotalCalculator(TotalScoreStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    public StudentTotal calculate(StudentId studentId, List<Evaluation> evaluations, Rubric rubric) {
        Objects.requireNonNull(studentId, "studentId");
        Objects.requireNonNull(rubric, "rubric");

        List<GradedCriterion> graded = new ArrayList<>();
        Map<CriterionId, Double> parts = new LinkedHashMap<>();

        for (Evaluation evaluation : evaluations) {
            if (!evaluation.studentId().equals(studentId)) {
                throw new DomainException("Передана оценка чужого студента: " + evaluation.studentId());
            }
            Criterion criterion = rubric.require(evaluation.criterionId());
            GradedCriterion pair = new GradedCriterion(evaluation, criterion);
            graded.add(pair);
            parts.put(criterion.id(), strategy.part(pair));
        }

        double value = strategy.calculate(graded);

        return new StudentTotal(
                studentId, value, strategy.name(),
                graded.size(), rubric.criteria().size(), parts
        );
    }
}
