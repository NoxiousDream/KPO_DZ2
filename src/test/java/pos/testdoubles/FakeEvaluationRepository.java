package pos.testdoubles;

import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.EvaluationId;
import pos.domain.model.StudentId;
import pos.domain.repository.EvaluationRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Fake хранилища оценок. */
public final class FakeEvaluationRepository implements EvaluationRepository {
    private final Map<EvaluationId, Evaluation> byId = new LinkedHashMap<>();

    @Override
    public void save(Evaluation evaluation) {
        byId.put(evaluation.id(), evaluation);
    }

    @Override
    public Optional<Evaluation> findById(EvaluationId id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Evaluation> findByStudentAndCriterion(StudentId studentId, CriterionId criterionId) {
        return byId.values().stream()
                .filter(e -> e.studentId().equals(studentId) && e.criterionId().equals(criterionId))
                .findFirst();
    }

    @Override
    public List<Evaluation> findByStudent(StudentId studentId) {
        return byId.values().stream()
                .filter(e -> e.studentId().equals(studentId))
                .toList();
    }

    @Override
    public List<Evaluation> findAll() {
        return List.copyOf(byId.values());
    }

    @Override
    public void deleteById(EvaluationId id) {
        byId.remove(id);
    }
}
