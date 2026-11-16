package pos.domain.service;

import pos.domain.model.Criterion;
import pos.domain.model.Evaluation;

/** Оценка вместе с её критерием из рубрикатора — вход для правила итога. */
public record GradedCriterion(Evaluation evaluation, Criterion criterion) {
}
