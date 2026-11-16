package pos;

import com.google.inject.Guice;
import com.google.inject.Injector;
import pos.domain.service.TotalScoreStrategy;
import pos.domain.service.strategy.NormalizedToTenStrategy;
import pos.domain.service.strategy.WeightedSumStrategy;
import pos.infrastructure.di.AppModule;
import pos.infrastructure.ui.ConsoleMenu;
import pos.infrastructure.ui.DemoScenario;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Composition root: собирает граф через Guice и запускает UI.
 *
 * Аргументы запуска:
 *   --demo        прогнать сквозной сценарий и выйти (без интерактива)
 *   --normalized  правило итога «нормализация к 10» вместо взвешенной суммы
 */
public final class Main {
    public static void main(String[] args) {
        boolean demoOnly = Arrays.asList(args).contains("--demo");
        boolean normalized = Arrays.asList(args).contains("--normalized");

        TotalScoreStrategy strategy = normalized ? new NormalizedToTenStrategy() : new WeightedSumStrategy();

        Injector injector = Guice.createInjector(
                new AppModule(strategy, Path.of("exports", "totals.csv")));

        System.out.println("ПОС — Преподавательская Оценка Студентов");
        System.out.println("Правило итога: " + strategy.name());

        if (demoOnly) {
            injector.getInstance(DemoScenario.class).run();
        } else {
            injector.getInstance(ConsoleMenu.class).run();
        }
    }
}
