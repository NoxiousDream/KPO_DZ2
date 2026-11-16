package pos.application;

import org.junit.jupiter.api.Test;
import pos.domain.model.Criterion;
import pos.domain.model.Evaluation;
import pos.domain.model.Student;

import static org.junit.jupiter.api.Assertions.*;

class PosFacadeTest {
    private final ApplicationFixture f = new ApplicationFixture();

    @Test
    void facadeMapsPrimitivesToValueObjects() {
        Student student = f.facade.addStudent("  231001  ", "  Иванов Иван  ", "  БПИ231  ");
        Criterion criterion = f.facade.addCriterion("hw", "Домашние задания", 10, 0.4);
        Evaluation evaluation = f.facade.grade("231001", "hw", 8, null);

        assertEquals("231001", student.id().value());
        assertEquals("Иванов Иван", student.fullName());
        assertEquals("HW", criterion.id().value());
        assertEquals("HW", evaluation.criterionId().value());
    }

    @Test
    void wholeScenarioWorksThroughSingleFacade() {
        f.facade.addStudent("231001", "Иванов Иван", "БПИ231");
        f.facade.addCriterion("HW", "Домашние задания", 10, 0.4);
        f.facade.addCriterion("EXAM", "Экзамен", 20, 0.4);
        f.facade.grade("231001", "HW", 8, null);
        f.facade.grade("231001", "EXAM", 15, null);

        assertEquals(1, f.facade.students().size());
        assertEquals(2, f.facade.criteria().size());
        assertEquals(2, f.facade.evaluationsOf("231001").size());
        assertEquals(9.2, f.facade.totalOf("231001").value(), 1e-9);
        assertEquals(1, f.facade.allTotals().size());

        f.facade.grade("231001", "HW", 9, null);
        assertEquals(9.6, f.facade.totalOf("231001").value(), 1e-9);

        f.facade.cancelGrade("231001", "EXAM");
        assertEquals(3.6, f.facade.totalOf("231001").value(), 1e-9);

        assertEquals("stub://totals", f.facade.exportTotals());
        assertEquals(1, f.exporter.callCount());
    }
}
