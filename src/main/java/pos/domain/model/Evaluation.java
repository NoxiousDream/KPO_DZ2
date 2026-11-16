package pos.domain.model;

import pos.domain.exception.DomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Entity: факт «этому студенту по этому критерию поставили такой балл».
 *
 * GoF (порождающий): Factory Method — статический фабричный метод create(...)
 * является ЕДИНСТВЕННОЙ точкой создания оценки. Конструктор приватный,
 * поэтому оценку физически нельзя собрать в обход рубрикатора и границ балла.
 */
public final class Evaluation {
    private final EvaluationId id;
    private final StudentId studentId;
    private final CriterionId criterionId;
    private Score score;
    private final LocalDate date;
    private String comment;

    private Evaluation(EvaluationId id, StudentId studentId, CriterionId criterionId,
                       Score score, LocalDate date, String comment) {
        this.id = id;
        this.studentId = studentId;
        this.criterionId = criterionId;
        this.score = score;
        this.date = date;
        this.comment = comment;
    }

    /**
     * Фабрика: проверяет принадлежность критерия рубрикатору и границы балла.
     * Рубрикатор передаётся явно — оценка не может «сама знать» про хранилище.
     */
    public static Evaluation create(Rubric rubric, StudentId studentId, CriterionId criterionId,
                                    Score score, LocalDate date, String comment) {
        Objects.requireNonNull(rubric, "rubric");
        Objects.requireNonNull(studentId, "studentId");
        Objects.requireNonNull(score, "score");

        Criterion criterion = rubric.require(criterionId);   // инвариант №2
        score.requireWithinMax(criterion.maxScore());        // инвариант №1

        return new Evaluation(
                EvaluationId.newId(), studentId, criterion.id(),
                score, date == null ? LocalDate.now() : date, normalizeComment(comment)
        );
    }

    /** Исправление балла: те же инварианты, что и при создании. */
    public void rescore(Rubric rubric, Score newScore) {
        Objects.requireNonNull(newScore, "newScore");
        Criterion criterion = rubric.require(criterionId);
        newScore.requireWithinMax(criterion.maxScore());
        this.score = newScore;
    }

    public void comment(String comment) {
        this.comment = normalizeComment(comment);
    }

    private static String normalizeComment(String comment) {
        return comment == null || comment.isBlank() ? null : comment.trim();
    }

    public EvaluationId id() {
        return id;
    }

    public StudentId studentId() {
        return studentId;
    }

    public CriterionId criterionId() {
        return criterionId;
    }

    public Score score() {
        return score;
    }

    public LocalDate date() {
        return date;
    }

    public Optional<String> comment() {
        return Optional.ofNullable(comment);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Evaluation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "[" + id + "] " + studentId + " / " + criterionId + " = " + score + " (" + date + ")";
    }
}
