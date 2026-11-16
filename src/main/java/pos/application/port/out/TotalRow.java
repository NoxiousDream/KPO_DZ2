package pos.application.port.out;

/**
 * Outbound DTO: плоская строка итога для выгрузки.
 * Application отдаёт наружу данные, а не доменные объекты —
 * адаптер (CSV/JSON/что угодно) мапит это в свой формат.
 */
public record TotalRow(
        String studentId,
        String fullName,
        String group,
        double total,
        String strategyName,
        int gradedCriteria,
        int totalCriteria,
        boolean complete
) {
}
