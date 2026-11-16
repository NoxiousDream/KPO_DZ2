package pos.application.usecase;

import pos.domain.exception.DomainException;
import pos.domain.model.Student;
import pos.domain.model.StudentId;
import pos.domain.repository.StudentRepository;

import java.util.Objects;

/** Сценарий «зачислить студента на курс». Уникальность id — правило приложения. */
public final class AddStudentUseCase {
    private final StudentRepository students;

    public AddStudentUseCase(StudentRepository students) {
        this.students = Objects.requireNonNull(students, "students");
    }

    public Student execute(StudentId id, String fullName, String group) {
        if (students.existsById(id)) {
            throw new DomainException("Студент с id " + id + " уже есть на курсе");
        }
        Student student = new Student(id, fullName, group);
        students.save(student);
        return student;
    }
}
