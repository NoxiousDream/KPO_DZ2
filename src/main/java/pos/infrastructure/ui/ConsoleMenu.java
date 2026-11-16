package pos.infrastructure.ui;

import pos.application.PosFacade;
import pos.domain.exception.DomainException;

import java.io.UncheckedIOException;
import java.util.Scanner;

/** Интерактивный консольный UI. Все сценарии — через один PosFacade. */
public final class ConsoleMenu {
    private final PosFacade facade;
    private final ConsoleView view;
    private final DemoScenario demo;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(PosFacade facade, ConsoleView view, DemoScenario demo) {
        this.facade = facade;
        this.view = view;
        this.demo = demo;
    }

    public void run() {
        view.header("ПОС — Преподавательская Оценка Студентов");
        while (true) {
            printMenu();
            String choice = readLine();
            if (choice == null) {
                view.line("Ввод недоступен — завершение работы.");
                return;
            }
            switch (choice) {
                case "1" -> guard(() -> view.line("Добавлен: " + facade.addStudent(
                        ask("id студента: "), ask("ФИО: "), ask("группа: "))));
                case "2" -> guard(() -> view.students(facade.students()));
                case "3" -> guard(() -> view.line("Добавлен критерий: " + facade.addCriterion(
                        ask("код критерия: "), ask("название: "),
                        askDouble("max_score: "), askDouble("вес: "))));
                case "4" -> guard(() -> view.criteria(facade.criteria()));
                case "5" -> guard(() -> view.line("Оценка: " + facade.grade(
                        ask("id студента: "), ask("код критерия: "),
                        askDouble("балл: "), ask("комментарий (можно пусто): "))));
                case "6" -> guard(() -> view.evaluations(facade.evaluationsOf(ask("id студента: "))));
                case "7" -> guard(() -> view.evaluations(facade.allEvaluations()));
                case "8" -> guard(() -> {
                    facade.cancelGrade(ask("id студента: "), ask("код критерия: "));
                    view.line("Оценка отменена.");
                });
                case "9" -> guard(() -> view.total(facade.totalOf(ask("id студента: ")), facade.criteria()));
                case "10" -> guard(() -> view.totals(facade.allTotals()));
                case "11" -> guard(() -> view.line("Итоги выгружены: " + facade.exportTotals()));
                case "12" -> guard(demo::run);
                case "0" -> {
                    view.line("До встречи.");
                    return;
                }
                default -> view.line("Неизвестный пункт меню: " + choice);
            }
        }
    }

    private void printMenu() {
        view.line("");
        view.line(" 1) Добавить студента          2) Список студентов");
        view.line(" 3) Добавить критерий          4) Список критериев");
        view.line(" 5) Выставить/исправить оценку 6) Оценки студента");
        view.line(" 7) Все оценки                 8) Отменить оценку");
        view.line(" 9) Итог по студенту          10) Итоги по всем");
        view.line("11) Экспорт итогов (CSV)      12) Демо-сценарий");
        view.line(" 0) Выход");
    }

    private void guard(Runnable action) {
        try {
            action.run();
        } catch (DomainException e) {
            view.line("[ОТКАЗ ДОМЕНА] " + e.getMessage());
        } catch (NumberFormatException e) {
            view.line("[ОШИБКА ВВОДА] ожидалось число (например 8 или 0.4)");
        } catch (UncheckedIOException e) {
            view.line("[ОШИБКА ВЫГРУЗКИ] " + e.getMessage());
        }
    }

    private String readLine() {
        System.out.print("> ");
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    private double askDouble(String prompt) {
        return Double.parseDouble(ask(prompt).replace(',', '.'));
    }
}
