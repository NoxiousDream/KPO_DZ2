package pos.infrastructure.ui;

import pos.domain.model.Criterion;
import pos.domain.model.CriterionId;
import pos.domain.model.Evaluation;
import pos.domain.model.Student;
import pos.domain.model.StudentTotal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Форматирование вывода. Единственное место, где знают, как выглядит отчёт в консоли. */
public final class ConsoleView {

    public void header(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    public void line(String text) {
        System.out.println(text);
    }

    public void students(List<Student> students) {
        header("Студенты (" + students.size() + ")");
        if (students.isEmpty()) {
            line("(пусто)");
            return;
        }
        students.forEach(s -> line(" - " + s));
    }

    public void criteria(List<Criterion> criteria) {
        header("Рубрикатор (" + criteria.size() + ")");
        if (criteria.isEmpty()) {
            line("(пусто)");
            return;
        }
        criteria.forEach(c -> line(" - " + c));
    }

    public void evaluations(List<Evaluation> evaluations) {
        header("Оценки (" + evaluations.size() + ")");
        if (evaluations.isEmpty()) {
            line("(пусто)");
            return;
        }
        evaluations.forEach(e -> line(" - " + e + e.comment().map(c -> " — " + c).orElse("")));
    }

    public void total(StudentTotal total, List<Criterion> criteria) {
        header("Итог студента " + total.studentId());
        Map<CriterionId, String> titles = criteria.stream()
                .collect(Collectors.toMap(Criterion::id, Criterion::title));

        total.weightedParts().forEach((id, part) ->
                line("   " + id + " «" + titles.getOrDefault(id, "?") + "»: вклад " + fmt(part)));

        line("   ИТОГ: " + fmt(total.value()) + "   [правило: " + total.strategyName() + "]");
        line("   Оценено критериев: " + total.gradedCriteria() + " из " + total.totalCriteria()
                + (total.isComplete() ? "" : "   (итог по неполному набору оценок)"));
    }

    public void totals(List<StudentTotal> totals) {
        header("Итоги по всем студентам (" + totals.size() + ")");
        if (totals.isEmpty()) {
            line("(пусто)");
            return;
        }
        totals.forEach(t -> line(" - " + t.studentId() + ": " + fmt(t.value())
                + "  (" + t.gradedCriteria() + "/" + t.totalCriteria()
                + (t.isComplete() ? "" : ", неполно") + ")"));
    }

    public static String fmt(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
