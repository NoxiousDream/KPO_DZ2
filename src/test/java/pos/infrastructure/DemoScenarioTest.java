package pos.infrastructure;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pos.domain.service.strategy.WeightedSumStrategy;
import pos.infrastructure.di.AppModule;
import pos.infrastructure.ui.DemoScenario;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DemoScenarioTest {
    @TempDir
    Path tempDir;

    private Injector injector() {
        return Guice.createInjector(new AppModule(new WeightedSumStrategy(), tempDir.resolve("totals.csv")));
    }

    private String capture(Runnable action) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void demoShowsInvariantsTotalsAndExport() {
        String output = capture(() -> injector().getInstance(DemoScenario.class).run());

        assertTrue(output.contains("[ИНВАРИАНТ СРАБОТАЛ]"));
        assertTrue(output.contains("11.40"));   // Иванов после исправления HW 8 → 9
        assertTrue(output.contains("8.00"));    // Петрова после повторного EXAM = 14
        assertTrue(output.contains("Итоги выгружены"));
        assertTrue(Files.exists(tempDir.resolve("totals.csv")));
    }

    @Test
    void repeatedRunIsSafeAndSkipsDuplicates() {
        DemoScenario demo = injector().getInstance(DemoScenario.class);

        capture(demo::run);
        String second = capture(demo::run);

        assertTrue(second.contains("[ПРОПУСК]"));
        assertTrue(second.contains("11.40"));
    }
}
