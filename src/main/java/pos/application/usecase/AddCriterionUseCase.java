package pos.application.usecase;

import pos.domain.model.Criterion;
import pos.domain.model.CriterionId;
import pos.domain.model.Rubric;
import pos.domain.model.Weight;
import pos.domain.repository.RubricRepository;

import java.util.Objects;

/**
 * Сценарий «добавить критерий в рубрикатор».
 * Инварианты (дубликаты, положительный максимум) охраняет агрегат Rubric.
 */
public final class AddCriterionUseCase {
    private final RubricRepository rubrics;

    public AddCriterionUseCase(RubricRepository rubrics) {
        this.rubrics = Objects.requireNonNull(rubrics, "rubrics");
    }

    public Criterion execute(CriterionId id, String title, double maxScore, Weight weight) {
        Rubric rubric = rubrics.load();
        Criterion criterion = rubric.addCriterion(id, title, maxScore, weight);
        rubrics.save(rubric);
        return criterion;
    }
}
