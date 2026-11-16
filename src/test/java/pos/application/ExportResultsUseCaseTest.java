package pos.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pos.application.port.out.TotalRow;
import pos.domain.model.CriterionId;
import pos.domain.model.Score;
import pos.domain.model.StudentId;

import static org.junit.jupiter.api.Assertions.*;

class ExportResultsUseCaseTest {
    private final ApplicationFixture f = new ApplicationFixture();
    private final StudentId student = new StudentId("231001");
    private final CriterionId hw = new CriterionId("HW");
    private final CriterionId exam = new CriterionId("EXAM");

    @BeforeEach
    void setUp() {
        f.seedRubric();
        f.addStudent.execute(student, "Иванов Иван", "БПИ231");
    }

    private TotalRow rowOf(String id) {
        return f.exporter.lastRows().stream()
                .filter(r -> r.studentId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void exportsOneRowPerStudentWithDomainData() {
        f.addStudent.execute(new StudentId("231002"), "Петрова Анна", "БПИ232");
        f.grade.execute(student, hw, new Score(8), null);
        f.grade.execute(student, exam, new Score(15), null);

        String destination = f.export.execute();

        assertEquals("stub://totals", destination);
        assertEquals(1, f.exporter.callCount());
        assertEquals(2, f.exporter.lastRows().size());

        TotalRow row = rowOf("231001");
        assertEquals("Иванов Иван", row.fullName());
        assertEquals("БПИ231", row.group());
        assertEquals(9.2, row.total(), 1e-9);
        assertTrue(row.complete());

        TotalRow empty = rowOf("231002");
        assertEquals(0.0, empty.total(), 1e-9);
        assertFalse(empty.complete());
    }

    @Test
    void exportAlwaysUsesLatestEvaluations() {
        f.grade.execute(student, hw, new Score(8), null);
        f.export.execute();
        assertEquals(3.2, rowOf("231001").total(), 1e-9);

        f.grade.execute(student, hw, new Score(9), null);
        f.export.execute();

        assertEquals(2, f.exporter.callCount());
        assertEquals(3.6, rowOf("231001").total(), 1e-9);
    }

    @Test
    void emptyCourseStillCallsPortWithNoRows() {
        ApplicationFixture empty = new ApplicationFixture();

        String destination = empty.export.execute();

        assertEquals("stub://totals", destination);
        assertEquals(1, empty.exporter.callCount());
        assertTrue(empty.exporter.lastRows().isEmpty());
    }
}
