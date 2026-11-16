package pos.application.usecase;

import pos.domain.model.Criterion;
import pos.domain.model.Evaluation;
import pos.domain.model.Student;
import pos.domain.model.StudentId;
import pos.domain.repository.EvaluationRepository;
import pos.domain.repository.RubricRepository;
import pos.domain.repository.StudentRepository;

import java.util.List;
import java.util.Objects;

/**
 * Read-only сценарии «посмотреть списки».
 * Объединены в один класс сознательно: три отдельных класса-проходника
 * были бы overengineering ради галочки.
 */
public final class CatalogQueries {
    private final StudentRepository students;
    private final RubricRepository rubrics;
    private final EvaluationRepository evaluations;

    public CatalogQueries(StudentRepository students,
                          RubricRepository rubrics,
                          EvaluationRepository evaluations) {
        this.students = Objects.requireNonNull(students, "students");
        this.rubrics = Objects.requireNonNull(rubrics, "rubrics");
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
    }

    public List<Student> students() {
        return students.findAll();
    }

    public List<Criterion> criteria() {
        return rubrics.load().criteria();
    }

    public List<Evaluation> evaluationsOf(StudentId studentId) {
        return evaluations.findByStudent(studentId);
    }

    public List<Evaluation> allEvaluations() {
        return evaluations.findAll();
    }
}
