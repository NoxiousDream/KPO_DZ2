package pos.infrastructure.persistence;

import pos.domain.model.Rubric;
import pos.domain.repository.RubricRepository;

import java.util.Objects;

/** Адаптер порта RubricRepository: курс один — рубрикатор один. */
public final class InMemoryRubricRepository implements RubricRepository {
    private Rubric rubric = new Rubric();

    @Override
    public Rubric load() {
        return rubric;
    }

    @Override
    public void save(Rubric rubric) {
        this.rubric = Objects.requireNonNull(rubric, "rubric");
    }
}
