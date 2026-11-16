package pos.infrastructure.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import pos.application.PosFacade;
import pos.application.port.out.ResultExporter;
import pos.application.usecase.AddCriterionUseCase;
import pos.application.usecase.AddStudentUseCase;
import pos.application.usecase.CalculateTotalUseCase;
import pos.application.usecase.CancelEvaluationUseCase;
import pos.application.usecase.CatalogQueries;
import pos.application.usecase.ExportResultsUseCase;
import pos.application.usecase.GradeStudentUseCase;
import pos.domain.repository.EvaluationRepository;
import pos.domain.repository.RubricRepository;
import pos.domain.repository.StudentRepository;
import pos.domain.service.TotalCalculator;
import pos.domain.service.TotalScoreStrategy;
import pos.domain.service.strategy.WeightedSumStrategy;
import pos.infrastructure.export.CsvResultExporter;
import pos.infrastructure.persistence.InMemoryEvaluationRepository;
import pos.infrastructure.persistence.InMemoryRubricRepository;
import pos.infrastructure.persistence.InMemoryStudentRepository;
import pos.infrastructure.ui.ConsoleMenu;
import pos.infrastructure.ui.ConsoleView;
import pos.infrastructure.ui.DemoScenario;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Composition root: ЕДИНСТВЕННОЕ место, где известны конкретные классы.
 * Домен и application не имеют ни одной Guice-аннотации.
 *
 * Смена правила итога = одна строка в totalScoreStrategy() (или аргумент запуска).
 */
public final class AppModule extends AbstractModule {
    private final TotalScoreStrategy strategy;
    private final Path exportPath;

    public AppModule() {
        this(new WeightedSumStrategy(), Path.of("exports", "totals.csv"));
    }

    public AppModule(TotalScoreStrategy strategy, Path exportPath) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
        this.exportPath = Objects.requireNonNull(exportPath, "exportPath");
    }

    // --- порты домена → адаптеры ------------------------------------------------

    @Provides
    @Singleton
    StudentRepository studentRepository() {
        return new InMemoryStudentRepository();
    }

    @Provides
    @Singleton
    RubricRepository rubricRepository() {
        return new InMemoryRubricRepository();
    }

    @Provides
    @Singleton
    EvaluationRepository evaluationRepository() {
        return new InMemoryEvaluationRepository();
    }

    // --- доменный сервис и сменяемое правило -------------------------------------

    @Provides
    @Singleton
    TotalScoreStrategy totalScoreStrategy() {
        return strategy;
    }

    @Provides
    @Singleton
    TotalCalculator totalCalculator(TotalScoreStrategy strategy) {
        return new TotalCalculator(strategy);
    }

    // --- исходящий порт → адаптер -------------------------------------------------

    @Provides
    @Singleton
    ResultExporter resultExporter() {
        return new CsvResultExporter(exportPath);
    }

    // --- use cases ----------------------------------------------------------------

    @Provides
    @Singleton
    AddStudentUseCase addStudentUseCase(StudentRepository students) {
        return new AddStudentUseCase(students);
    }

    @Provides
    @Singleton
    AddCriterionUseCase addCriterionUseCase(RubricRepository rubrics) {
        return new AddCriterionUseCase(rubrics);
    }

    @Provides
    @Singleton
    GradeStudentUseCase gradeStudentUseCase(StudentRepository students,
                                            RubricRepository rubrics,
                                            EvaluationRepository evaluations) {
        return new GradeStudentUseCase(students, rubrics, evaluations);
    }

    @Provides
    @Singleton
    CancelEvaluationUseCase cancelEvaluationUseCase(EvaluationRepository evaluations) {
        return new CancelEvaluationUseCase(evaluations);
    }

    @Provides
    @Singleton
    CatalogQueries catalogQueries(StudentRepository students,
                                  RubricRepository rubrics,
                                  EvaluationRepository evaluations) {
        return new CatalogQueries(students, rubrics, evaluations);
    }

    @Provides
    @Singleton
    CalculateTotalUseCase calculateTotalUseCase(StudentRepository students,
                                                RubricRepository rubrics,
                                                EvaluationRepository evaluations,
                                                TotalCalculator calculator) {
        return new CalculateTotalUseCase(students, rubrics, evaluations, calculator);
    }

    @Provides
    @Singleton
    ExportResultsUseCase exportResultsUseCase(CalculateTotalUseCase totals,
                                              StudentRepository students,
                                              ResultExporter exporter) {
        return new ExportResultsUseCase(totals, students, exporter);
    }

    // --- фасад и UI -----------------------------------------------------------------

    @Provides
    @Singleton
    PosFacade posFacade(AddStudentUseCase addStudent,
                        AddCriterionUseCase addCriterion,
                        GradeStudentUseCase grade,
                        CancelEvaluationUseCase cancel,
                        CatalogQueries queries,
                        CalculateTotalUseCase totals,
                        ExportResultsUseCase export) {
        return new PosFacade(addStudent, addCriterion, grade, cancel, queries, totals, export);
    }

    @Provides
    @Singleton
    ConsoleView consoleView() {
        return new ConsoleView();
    }

    @Provides
    @Singleton
    DemoScenario demoScenario(PosFacade facade, ConsoleView view) {
        return new DemoScenario(facade, view);
    }

    @Provides
    @Singleton
    ConsoleMenu consoleMenu(PosFacade facade, ConsoleView view, DemoScenario demo) {
        return new ConsoleMenu(facade, view, demo);
    }
}
