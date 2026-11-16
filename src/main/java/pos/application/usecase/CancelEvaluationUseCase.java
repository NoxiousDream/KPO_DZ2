package pos.application.usecase;

import pos.domain.exception.DomainException;
import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.StudentId;
import pos.domain.repository.EvaluationRepository;

import java.util.Objects;

/** Сценарий «отменить оценку»: удаляет факт оценивания, итог пересчитается автоматически. */
public final class CancelEvaluationUseCase {
    private final EvaluationRepository evaluations;

    public CancelEvaluationUseCase(EvaluationRepository evaluations) {
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
    }

    public void execute(StudentId studentId, CriterionId criterionId) {
        Evaluation evaluation = evaluations.findByStudentAndCriterion(studentId, criterionId)
                .orElseThrow(() -> new DomainException(
                        "У студента " + studentId + " нет оценки по критерию " + criterionId));
        evaluations.deleteById(evaluation.id());
    }
}
