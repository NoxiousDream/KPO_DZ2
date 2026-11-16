package pos.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pos.application.port.out.TotalRow;
import pos.infrastructure.export.CsvResultExporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvResultExporterTest {
    @TempDir
    Path tempDir;

    @Test
    void createsDirectoriesWritesHeaderAndUsesDotAsDecimalSeparator() throws IOException {
        Path target = tempDir.resolve("exports/totals.csv");

        String destination = new CsvResultExporter(target).export(List.of(
                new TotalRow("231001", "Иванов Иван", "БПИ231", 9.2, "Взвешенная сумма", 2, 2, true)));

        String content = Files.readString(target);
        assertEquals(target.toAbsolutePath().toString(), destination);
        assertTrue(content.startsWith("student_id,full_name,group,total"));
        assertTrue(content.contains("231001,Иванов Иван,БПИ231,9.20"));
        assertTrue(content.contains(",yes"));
    }

    @Test
    void escapesCommasAndQuotesAndMarksIncomplete() throws IOException {
        Path target = tempDir.resolve("totals.csv");

        new CsvResultExporter(target).export(List.of(
                new TotalRow("231001", "Иванов, \"И\"", "БПИ231", 0, "s", 0, 1, false)));

        String content = Files.readString(target);
        assertTrue(content.contains("\"Иванов, \"\"И\"\"\""));
        assertTrue(content.contains(",no"));
    }
}
