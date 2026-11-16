package pos.domain.repository;

import pos.domain.model.Student;
import pos.domain.model.StudentId;

import java.util.List;
import java.util.Optional;

/** Порт хранения студентов. Реализация — в infrastructure/persistence. */
public interface StudentRepository {
    void save(Student student);

    Optional<Student> findById(StudentId id);

    List<Student> findAll();

    boolean existsById(StudentId id);
}
