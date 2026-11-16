package pos.application.port.out;

import java.util.List;

/**
 * Исходящий порт выгрузки итогов (Ports & Adapters).
 * Application не знает, CSV это, JSON или отправка по сети:
 * реализация живёт в infrastructure/export.
 */
public interface ResultExporter {

    /**
     * Выгружает строки итогов.
     *
     * @return человекочитаемое место выгрузки (путь файла / имя канала) для сообщения в UI
     */
    String export(List<TotalRow> rows);
}
