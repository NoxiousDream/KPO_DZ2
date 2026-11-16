package pos.testdoubles;

import pos.domain.model.Student;
import pos.domain.model.StudentId;
import pos.domain.repository.StudentRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Fake хранилища студентов для тестов application (не продакшен-адаптер). */
public final class FakeStudentRepository implements StudentRepository {
    private final Map<StudentId, Student> byId = new LinkedHashMap<>();

    @Override
    public void save(Student student) {
        byId.put(student.id(), student);
    }

    @Override
    public Optional<Student> findById(StudentId id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Student> findAll() {
        return List.copyOf(byId.values());
    }

    @Override
    public boolean existsById(StudentId id) {
        return byId.containsKey(id);
    }
}
