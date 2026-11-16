package pos;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pos.application.PosFacade;
import pos.domain.repository.StudentRepository;
import pos.domain.service.TotalScoreStrategy;
import pos.domain.service.strategy.NormalizedToTenStrategy;
import pos.domain.service.strategy.WeightedSumStrategy;
import pos.infrastructure.di.AppModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompositionRootTest {
    @TempDir
    Path tempDir;

    private Injector injectorWith(TotalScoreStrategy strategy) {
        return Guice.createInjector(new AppModule(strategy, tempDir.resolve("nested/totals.csv")));
    }

    private void seedCourse(PosFacade facade) {
        facade.addCriterion("HW", "Домашние задания", 10, 0.4);
        facade.addCriterion("EXAM", "Экзамен", 20, 0.4);
        facade.addStudent("231001", "Иванов Иван", "БПИ231");
        facade.grade("231001", "HW", 8, null);
        facade.grade("231001", "EXAM", 15, null);
    }

    @Test
    void diAssemblesWorkingGraphIncludingCsvAdapter() throws IOException {
        Path target = tempDir.resolve("nested/totals.csv");
        PosFacade facade = injectorWith(new WeightedSumStrategy()).getInstance(PosFacade.class);

        seedCourse(facade);
        assertEquals(9.2, facade.totalOf("231001").value(), 1e-9);

        String destination = facade.exportTotals();

        assertTrue(Files.exists(target));
        assertEquals(target.toAbsolutePath().toString(), destination);
        String content = Files.readString(target);
        assertTrue(content.contains("231001,Иванов Иван,БПИ231,9.20"));
    }

    @Test
    void strategyBindingChangesTotalsWithoutTouchingDomainOrApplication() {
        PosFacade weighted = injectorWith(new WeightedSumStrategy()).getInstance(PosFacade.class);
        PosFacade normalized = injectorWith(new NormalizedToTenStrategy()).getInstance(PosFacade.class);

        seedCourse(weighted);
        seedCourse(normalized);

        assertEquals(9.2, weighted.totalOf("231001").value(), 1e-9);
        assertEquals(7.6666666667, normalized.totalOf("231001").value(), 1e-6);
    }

    @Test
    void repositoriesAreSingletonsInsideContainer() {
        Injector injector = injectorWith(new WeightedSumStrategy());

        assertSame(injector.getInstance(StudentRepository.class), injector.getInstance(StudentRepository.class));
    }
}
