package pos.application;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.Student;
import pos.domain.model.StudentId;

import static org.junit.jupiter.api.Assertions.*;

class AddStudentUseCaseTest {
    private final ApplicationFixture f = new ApplicationFixture();

    @Test
    void addsStudentAndPersistsIt() {
        Student student = f.addStudent.execute(new StudentId("231001"), "Иванов Иван", "БПИ231");

        assertEquals(student, f.students.findById(new StudentId("231001")).orElseThrow());
        assertEquals(1, f.queries.students().size());
    }

    @Test
    void duplicateIdIsRejected() {
        f.addStudent.execute(new StudentId("231001"), "Иванов Иван", "БПИ231");

        DomainException ex = assertThrows(DomainException.class,
                () -> f.addStudent.execute(new StudentId("231001"), "Другой Человек", "БПИ232"));

        assertTrue(ex.getMessage().contains("уже есть"));
        assertEquals(1, f.queries.students().size());
    }

    @Test
    void invalidStudentIsNotPersisted() {
        assertThrows(DomainException.class,
                () -> f.addStudent.execute(new StudentId("231002"), "   ", "БПИ231"));

        assertTrue(f.queries.students().isEmpty());
    }
}
