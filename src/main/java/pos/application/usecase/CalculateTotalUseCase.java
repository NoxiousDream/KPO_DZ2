package pos.application.usecase;

import pos.domain.exception.DomainException;
import pos.domain.model.Rubric;
import pos.domain.model.StudentId;
import pos.domain.model.StudentTotal;
import pos.domain.repository.EvaluationRepository;
import pos.domain.repository.RubricRepository;
import pos.domain.repository.StudentRepository;
import pos.domain.service.TotalCalculator;

import java.util.List;
import java.util.Objects;

/**
 * Сценарий «посчитать итог». Итог НИГДЕ не хранится —
 * каждый раз вычисляется Domain Service'ом из текущих оценок и рубрикатора.
 */
public final class CalculateTotalUseCase {
    private final StudentRepository students;
    private final RubricRepository rubrics;
    private final EvaluationRepository evaluations;
    private final TotalCalculator calculator;

    public CalculateTotalUseCase(StudentRepository students,
                                 RubricRepository rubrics,
                                 EvaluationRepository evaluations,
                                 TotalCalculator calculator) {
        this.students = Objects.requireNonNull(students, "students");
        this.rubrics = Objects.requireNonNull(rubrics, "rubrics");
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
        this.calculator = Objects.requireNonNull(calculator, "calculator");
    }

    public StudentTotal forStudent(StudentId studentId) {
        if (!students.existsById(studentId)) {
            throw new DomainException("Студента " + studentId + " нет на курсе");
        }
        return calculator.calculate(studentId, evaluations.findByStudent(studentId), rubrics.load());
    }

    public List<StudentTotal> forAllStudents() {
        Rubric rubric = rubrics.load();
        return students.findAll().stream()
                .map(student -> calculator.calculate(
                        student.id(), evaluations.findByStudent(student.id()), rubric))
                .toList();
    }
}
