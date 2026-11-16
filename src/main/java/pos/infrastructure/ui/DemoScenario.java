package pos.infrastructure.ui;

import pos.application.PosFacade;
import pos.domain.exception.DomainException;

/**
 * Сквозной сценарий из ФТ-4:
 * студенты + критерии → оценки → (попытки нарушить инварианты) → итог →
 * исправление/отмена → пересчёт → экспорт.
 * Повторный запуск безопасен: дубликаты на этапе заводки пропускаются.
 */
public final class DemoScenario {
    private final PosFacade facade;
    private final ConsoleView view;

    public DemoScenario(PosFacade facade, ConsoleView view) {
        this.facade = facade;
        this.view = view;
    }

    public void run() {
        view.header("ПОС: сквозной демонстрационный сценарий");

        view.line("Шаг 1. Заводим студентов и критерии");
        tryStep("студент 231001", () -> facade.addStudent("231001", "Иванов Иван", "БПИ231"));
        tryStep("студент 231002", () -> facade.addStudent("231002", "Петрова Анна", "БПИ232"));
        tryStep("критерий HW", () -> facade.addCriterion("HW", "Домашние задания", 10, 0.4));
        tryStep("критерий PROJECT", () -> facade.addCriterion("PROJECT", "Проект", 10, 0.2));
        tryStep("критерий EXAM", () -> facade.addCriterion("EXAM", "Экзамен", 20, 0.4));
        view.students(facade.students());
        view.criteria(facade.criteria());

        view.line("");
        view.line("Шаг 2. Выставляем оценки");
        facade.grade("231001", "HW", 8, "стабильно");
        facade.grade("231001", "PROJECT", 9, null);
        facade.grade("231001", "EXAM", 15, null);
        facade.grade("231002", "HW", 6, null);
        facade.grade("231002", "EXAM", 12, null);
        view.evaluations(facade.allEvaluations());

        view.line("");
        view.line("Шаг 3. Пробуем нарушить правила домена");
        expectRejection("12 баллов из 10", () -> facade.grade("231001", "HW", 12, null));
        expectRejection("критерий вне рубрикатора", () -> facade.grade("231001", "ORAL", 5, null));
        expectRejection("оценка несуществующему студенту", () -> facade.grade("999999", "HW", 5, null));

        view.line("");
        view.line("Шаг 4. Итоги (взвешенная сумма по умолчанию)");
        view.total(facade.totalOf("231001"), facade.criteria());
        view.total(facade.totalOf("231002"), facade.criteria());

        view.line("");
        view.line("Шаг 5. Исправляем и отменяем — итог пересчитывается сам");
        facade.grade("231001", "HW", 9, "исправлено после перепроверки");
        view.line("Иванов: HW исправлен 8 → 9");
        view.total(facade.totalOf("231001"), facade.criteria());

        facade.cancelGrade("231002", "EXAM");
        view.line("Петрова: оценка EXAM отменена");
        view.total(facade.totalOf("231002"), facade.criteria());

        facade.grade("231002", "EXAM", 14, "пересдача");
        view.line("Петрова: EXAM выставлен заново — 14");
        view.totals(facade.allTotals());

        view.line("");
        view.line("Шаг 6. Экспорт итогов");
        String destination = facade.exportTotals();
        view.line("Итоги выгружены: " + destination);

        view.header("Сквозной сценарий завершён");
    }

    private void tryStep(String label, Runnable action) {
        try {
            action.run();
        } catch (DomainException e) {
            view.line("[ПРОПУСК] " + label + ": " + e.getMessage());
        }
    }

    private void expectRejection(String label, Runnable action) {
        try {
            action.run();
            view.line("[ОШИБКА МОДЕЛИ] " + label + " — должно было быть запрещено!");
        } catch (DomainException e) {
            view.line("[ИНВАРИАНТ СРАБОТАЛ] " + label + ": " + e.getMessage());
        }
    }
}
