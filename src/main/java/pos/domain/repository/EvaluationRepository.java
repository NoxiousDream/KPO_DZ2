package pos.domain.repository;

import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.EvaluationId;
import pos.domain.model.StudentId;

import java.util.List;
import java.util.Optional;

/** Порт хранения оценок. */
public interface EvaluationRepository {
    void save(Evaluation evaluation);

    Optional<Evaluation> findById(EvaluationId id);

    /** Не более одной оценки на пару (студент, критерий) — исправление заменяет прежнюю. */
    Optional<Evaluation> findByStudentAndCriterion(StudentId studentId, CriterionId criterionId);

    List<Evaluation> findByStudent(StudentId studentId);

    List<Evaluation> findAll();

    void deleteById(EvaluationId id);
}
