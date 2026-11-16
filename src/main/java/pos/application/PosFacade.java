package pos.application;

import pos.application.usecase.AddCriterionUseCase;
import pos.application.usecase.AddStudentUseCase;
import pos.application.usecase.CalculateTotalUseCase;
import pos.application.usecase.CancelEvaluationUseCase;
import pos.application.usecase.CatalogQueries;
import pos.application.usecase.ExportResultsUseCase;
import pos.application.usecase.GradeStudentUseCase;
import pos.domain.model.Criterion;
import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.Score;
import pos.domain.model.Student;
import pos.domain.model.StudentId;
import pos.domain.model.StudentTotal;
import pos.domain.model.Weight;

import java.util.List;
import java.util.Objects;

/**
 * GoF (структурный): Facade — единая точка сценариев для консоли.
 *
 * Боль, которую закрывает: без фасада ConsoleMenu знала бы про семь use cases,
 * сама собирала бы VO из строк и оркестровывала вызовы — сценарии «размазались» бы по UI.
 * Здесь UI говорит на языке примитивов («выстави 8 по HW студенту 231001»),
 * а фасад мапит примитивы в VO и делегирует сценариям.
 * Замена консоли на другой UI не потребует трогать use cases.
 */
public final class PosFacade {
    private final AddStudentUseCase addStudentUseCase;
    private final AddCriterionUseCase addCriterionUseCase;
    private final GradeStudentUseCase gradeStudentUseCase;
    private final CancelEvaluationUseCase cancelEvaluationUseCase;
    private final CatalogQueries queries;
    private final CalculateTotalUseCase calculateTotalUseCase;
    private final ExportResultsUseCase exportResultsUseCase;

    public PosFacade(AddStudentUseCase addStudentUseCase,
                     AddCriterionUseCase addCriterionUseCase,
                     GradeStudentUseCase gradeStudentUseCase,
                     CancelEvaluationUseCase cancelEvaluationUseCase,
                     CatalogQueries queries,
                     CalculateTotalUseCase calculateTotalUseCase,
                     ExportResultsUseCase exportResultsUseCase) {
        this.addStudentUseCase = Objects.requireNonNull(addStudentUseCase);
        this.addCriterionUseCase = Objects.requireNonNull(addCriterionUseCase);
        this.gradeStudentUseCase = Objects.requireNonNull(gradeStudentUseCase);
        this.cancelEvaluationUseCase = Objects.requireNonNull(cancelEvaluationUseCase);
        this.queries = Objects.requireNonNull(queries);
        this.calculateTotalUseCase = Objects.requireNonNull(calculateTotalUseCase);
        this.exportResultsUseCase = Objects.requireNonNull(exportResultsUseCase);
    }

    // --- студенты и рубрикатор -------------------------------------------------

    public Student addStudent(String id, String fullName, String group) {
        return addStudentUseCase.execute(new StudentId(id), fullName, group);
    }

    public List<Student> students() {
        return queries.students();
    }

    public Criterion addCriterion(String code, String title, double maxScore, double weight) {
        return addCriterionUseCase.execute(new CriterionId(code), title, maxScore, new Weight(weight));
    }

    public List<Criterion> criteria() {
        return queries.criteria();
    }

    // --- оценки ----------------------------------------------------------------

    public Evaluation grade(String studentId, String criterionCode, double score, String comment) {
        return gradeStudentUseCase.execute(
                new StudentId(studentId), new CriterionId(criterionCode), new Score(score), comment);
    }

    public List<Evaluation> evaluationsOf(String studentId) {
        return queries.evaluationsOf(new StudentId(studentId));
    }

    public List<Evaluation> allEvaluations() {
        return queries.allEvaluations();
    }

    public void cancelGrade(String studentId, String criterionCode) {
        cancelEvaluationUseCase.execute(new StudentId(studentId), new CriterionId(criterionCode));
    }

    // --- итог и выгрузка ---------------------------------------------------------

    public StudentTotal totalOf(String studentId) {
        return calculateTotalUseCase.forStudent(new StudentId(studentId));
    }

    public List<StudentTotal> allTotals() {
        return calculateTotalUseCase.forAllStudents();
    }

    public String exportTotals() {
        return exportResultsUseCase.execute();
    }
}
