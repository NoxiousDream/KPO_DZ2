package pos.infrastructure.persistence;

import pos.domain.model.Student;
import pos.domain.model.StudentId;
import pos.domain.repository.StudentRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Адаптер порта StudentRepository: хранение в памяти, стабильный порядок по id. */
public final class InMemoryStudentRepository implements StudentRepository {
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
        return byId.values().stream()
                .sorted(Comparator.comparing(s -> s.id().value()))
                .toList();
    }

    @Override
    public boolean existsById(StudentId id) {
        return byId.containsKey(id);
    }
}
