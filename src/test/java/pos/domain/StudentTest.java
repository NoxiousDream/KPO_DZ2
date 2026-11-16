package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.Student;
import pos.domain.model.StudentId;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void identityIsBasedOnIdNotOnName() {
        StudentId id = new StudentId("231001");
        Student a = new Student(id, "Иванов Иван", "БПИ231");
        Student b = new Student(id, "Иванов И. И.", "БПИ232");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void renameKeepsIdentity() {
        Student student = new Student(new StudentId("231001"), "Иванов Иван", "БПИ231");
        student.rename("Иванов Иван Иванович");

        assertEquals("Иванов Иван Иванович", student.fullName());
        assertEquals(new StudentId("231001"), student.id());
    }

    @Test
    void blankNameOrGroupIsRejected() {
        StudentId id = new StudentId("231001");
        assertThrows(DomainException.class, () -> new Student(id, "  ", "БПИ231"));
        assertThrows(DomainException.class, () -> new Student(id, "Иванов", " "));

        Student student = new Student(id, "Иванов", "БПИ231");
        assertThrows(DomainException.class, () -> student.rename(null));
        assertThrows(DomainException.class, () -> student.changeGroup(""));
    }
}
