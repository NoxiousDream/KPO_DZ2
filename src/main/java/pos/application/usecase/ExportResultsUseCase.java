package pos.application.usecase;

import pos.application.port.out.ResultExporter;
import pos.application.port.out.TotalRow;
import pos.domain.model.Student;
import pos.domain.model.StudentId;
import pos.domain.repository.StudentRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сценарий «выгрузить итоги»: считает итоги по всем студентам,
 * собирает плоские строки (домен + данные студента) и отдаёт в исходящий порт.
 * Про формат выгрузки не знает ничего.
 */
public final class ExportResultsUseCase {
    private final CalculateTotalUseCase totals;
    private final StudentRepository students;
    private final ResultExporter exporter;

    public ExportResultsUseCase(CalculateTotalUseCase totals,
                                StudentRepository students,
                                ResultExporter exporter) {
        this.totals = Objects.requireNonNull(totals, "totals");
        this.students = Objects.requireNonNull(students, "students");
        this.exporter = Objects.requireNonNull(exporter, "exporter");
    }

    public String execute() {
        Map<StudentId, Student> byId = students.findAll().stream()
                .collect(Collectors.toMap(Student::id, Function.identity()));

        List<TotalRow> rows = totals.forAllStudents().stream()
                .map(total -> {
                    Student student = byId.get(total.studentId());
                    return new TotalRow(
                            total.studentId().value(),
                            student.fullName(),
                            student.group(),
                            total.value(),
                            total.strategyName(),
                            total.gradedCriteria(),
                            total.totalCriteria(),
                            total.isComplete()
                    );
                })
                .toList();

        return exporter.export(rows);
    }
}
