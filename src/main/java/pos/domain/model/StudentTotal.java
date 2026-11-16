package pos.domain.model;

import java.util.Map;

/**
 * Value Object: итог по студенту — результат расчёта, а не хранимая «цифра наугад».
 * Содержит детализацию, чтобы консоль могла показать, из чего сложился итог.
 */
public record StudentTotal(
        StudentId studentId,
        double value,                 // итоговый балл по выбранной стратегии
        String strategyName,          // какое правило применено
        int gradedCriteria,           // сколько критериев оценено
        int totalCriteria,            // сколько критериев в рубрикаторе
        Map<CriterionId, Double> weightedParts  // вклад каждого оценённого критерия
) {
    public StudentTotal {
        weightedParts = Map.copyOf(weightedParts);
    }

    public boolean isComplete() {
        return gradedCriteria == totalCriteria;
    }
}
