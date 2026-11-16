package pos.domain.exception;

/**
 * Нарушение правила предметной области.
 * Бросается моделью, перехватывается на границе (консоль) и показывается человеку.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
