package pos.domain.model;

import pos.domain.exception.DomainException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate Root: рубрикатор курса — набор критериев.
 * Охраняет инвариант «оценку можно выставить только по критерию из рубрикатора»:
 * снаружи получить Criterion можно только через require(...).
 */
public final class Rubric {
    private final Map<CriterionId, Criterion> criteria = new LinkedHashMap<>();

    public Criterion addCriterion(CriterionId id, String title, double maxScore, Weight weight) {
        if (criteria.containsKey(id)) {
            throw new DomainException("Критерий с кодом " + id + " уже есть в рубрикаторе");
        }
        Criterion criterion = new Criterion(id, title, maxScore, weight);
        criteria.put(id, criterion);
        return criterion;
    }

    /** Единственная точка доступа к критерию; бросает DomainException, если его нет. */
    public Criterion require(CriterionId id) {
        Criterion criterion = criteria.get(id);
        if (criterion == null) {
            throw new DomainException("Критерия " + id + " нет в рубрикаторе — оценить по нему нельзя");
        }
        return criterion;
    }

    public boolean contains(CriterionId id) {
        return criteria.containsKey(id);
    }

    public List<Criterion> criteria() {
        return List.copyOf(criteria.values());
    }

    public boolean isEmpty() {
        return criteria.isEmpty();
    }
}
