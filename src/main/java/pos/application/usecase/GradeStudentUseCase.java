package pos.application.usecase;

import pos.domain.exception.DomainException;
import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.Rubric;
import pos.domain.model.Score;
import pos.domain.model.StudentId;
import pos.domain.repository.EvaluationRepository;
import pos.domain.repository.RubricRepository;
import pos.domain.repository.StudentRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Сценарий «выставить оценку» с семантикой upsert:
 * на пару (студент, критерий) существует не более одной оценки —
 * повторное выставление ИСПРАВЛЯЕТ прежнюю (балл и комментарий), а не плодит дубли.
 * Границы балла и принадлежность критерию рубрикатору проверяет домен
 * (Evaluation.create / Evaluation.rescore), здесь только оркестровка.
 */
public final class GradeStudentUseCase {
    private final StudentRepository students;
    private final RubricRepository rubrics;
    private final EvaluationRepository evaluations;

    public GradeStudentUseCase(StudentRepository students,
                               RubricRepository rubrics,
                               EvaluationRepository evaluations) {
        this.students = Objects.requireNonNull(students, "students");
        this.rubrics = Objects.requireNonNull(rubrics, "rubrics");
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
    }

    public Evaluation execute(StudentId studentId, CriterionId criterionId, Score score, String comment) {
        if (!students.existsById(studentId)) {
            throw new DomainException("Студента " + studentId + " нет на курсе — оценивать некого");
        }

        Rubric rubric = rubrics.load();
        Optional<Evaluation> existing = evaluations.findByStudentAndCriterion(studentId, criterionId);

        Evaluation evaluation;
        if (existing.isPresent()) {
            evaluation = existing.get();
            evaluation.rescore(rubric, score);
            evaluation.comment(comment);   // пустой комментарий очищает прежний
        } else {
            evaluation = Evaluation.create(rubric, studentId, criterionId, score, null, comment);
        }

        evaluations.save(evaluation);
        return evaluation;
    }
}
