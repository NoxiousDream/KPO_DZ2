package pos.application;

import pos.application.usecase.AddCriterionUseCase;
import pos.application.usecase.AddStudentUseCase;
import pos.application.usecase.CalculateTotalUseCase;
import pos.application.usecase.CancelEvaluationUseCase;
import pos.application.usecase.CatalogQueries;
import pos.application.usecase.ExportResultsUseCase;
import pos.application.usecase.GradeStudentUseCase;
import pos.domain.model.CriterionId;
import pos.domain.model.StudentId;
import pos.domain.model.Weight;
import pos.domain.service.TotalCalculator;
import pos.domain.service.TotalScoreStrategy;
import pos.domain.service.strategy.WeightedSumStrategy;
import pos.testdoubles.FakeEvaluationRepository;
import pos.testdoubles.FakeRubricRepository;
import pos.testdoubles.FakeStudentRepository;
import pos.testdoubles.RecordingExporterStub;

/**
 * Сборка сценариев на fake-ах и stub-е.
 * Правило итога подставляется снаружи — так тестируется сменяемость Strategy.
 */
final class ApplicationFixture {
    final FakeStudentRepository students = new FakeStudentRepository();
    final FakeRubricRepository rubrics = new FakeRubricRepository();
    final FakeEvaluationRepository evaluations = new FakeEvaluationRepository();
    final RecordingExporterStub exporter = new RecordingExporterStub();

    final AddStudentUseCase addStudent = new AddStudentUseCase(students);
    final AddCriterionUseCase addCriterion = new AddCriterionUseCase(rubrics);
    final GradeStudentUseCase grade = new GradeStudentUseCase(students, rubrics, evaluations);
    final CancelEvaluationUseCase cancel = new CancelEvaluationUseCase(evaluations);
    final CatalogQueries queries = new CatalogQueries(students, rubrics, evaluations);

    final CalculateTotalUseCase totals;
    final ExportResultsUseCase export;
    final PosFacade facade;

    ApplicationFixture() {
        this(new WeightedSumStrategy());
    }

    ApplicationFixture(TotalScoreStrategy strategy) {
        totals = new CalculateTotalUseCase(students, rubrics, evaluations, new TotalCalculator(strategy));
        export = new ExportResultsUseCase(totals, students, exporter);
        facade = new PosFacade(addStudent, addCriterion, grade, cancel, queries, totals, export);
    }

    /** HW: max 10, вес 0.4; EXAM: max 20, вес 0.4. */
    void seedRubric() {
        addCriterion.execute(new CriterionId("HW"), "Домашние задания", 10, new Weight(0.4));
        addCriterion.execute(new CriterionId("EXAM"), "Экзамен", 20, new Weight(0.4));
    }

    void seedStudent(String id) {
        addStudent.execute(new StudentId(id), "Студент " + id, "БПИ231");
    }
}
