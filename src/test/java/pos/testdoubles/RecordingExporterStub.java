package pos.testdoubles;

import pos.application.port.out.ResultExporter;
import pos.application.port.out.TotalRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub исходящего порта: ничего не пишет на диск, но запоминает,
 * какие строки и сколько раз в него передали.
 */
public final class RecordingExporterStub implements ResultExporter {
    private final List<List<TotalRow>> calls = new ArrayList<>();
    private List<TotalRow> lastRows = List.of();

    @Override
    public String export(List<TotalRow> rows) {
        lastRows = List.copyOf(rows);
        calls.add(lastRows);
        return "stub://totals";
    }

    public List<TotalRow> lastRows() {
        return lastRows;
    }

    public int callCount() {
        return calls.size();
    }
}
