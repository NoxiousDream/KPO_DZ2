package pos.testdoubles;

import pos.domain.model.Rubric;
import pos.domain.repository.RubricRepository;

import java.util.Objects;

/** Fake рубрикатора; счётчик save() позволяет проверить, что сценарий сохранил агрегат. */
public final class FakeRubricRepository implements RubricRepository {
    private Rubric rubric = new Rubric();
    private int saveCalls;

    @Override
    public Rubric load() {
        return rubric;
    }

    @Override
    public void save(Rubric rubric) {
        this.rubric = Objects.requireNonNull(rubric, "rubric");
        saveCalls++;
    }

    public int saveCalls() {
        return saveCalls;
    }
}
