package pos.domain.repository;

import pos.domain.model.Rubric;

/** Порт хранения рубрикатора. В этой версии курс один — рубрикатор один. */
public interface RubricRepository {
    Rubric load();

    void save(Rubric rubric);
}
