package pos.infrastructure.export;

import pos.application.port.out.ResultExporter;
import pos.application.port.out.TotalRow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Адаптер исходящего порта: пишет итоги в CSV.
 * Знание про формат/файлы живёт ТОЛЬКО здесь.
 * Числа форматируются в Locale.ROOT, чтобы локальная запятая не ломала CSV.
 */
public final class CsvResultExporter implements ResultExporter {
    private static final String HEADER =
            "student_id,full_name,group,total,strategy,graded_criteria,total_criteria,complete";

    private final Path target;

    public CsvResultExporter(Path target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public String export(List<TotalRow> rows) {
        StringBuilder csv = new StringBuilder(HEADER).append(System.lineSeparator());
        for (TotalRow row : rows) {
            csv.append(escape(row.studentId())).append(',')
                    .append(escape(row.fullName())).append(',')
                    .append(escape(row.group())).append(',')
                    .append(String.format(Locale.ROOT, "%.2f", row.total())).append(',')
                    .append(escape(row.strategyName())).append(',')
                    .append(row.gradedCriteria()).append(',')
                    .append(row.totalCriteria()).append(',')
                    .append(row.complete() ? "yes" : "no")
                    .append(System.lineSeparator());
        }

        try {
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(target, csv.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать выгрузку: " + target, e);
        }
        return target.toAbsolutePath().toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        boolean needsQuotes = escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r");
        return needsQuotes ? '"' + escaped + '"' : escaped;
    }
}
