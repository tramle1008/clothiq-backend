package commerce.sbEcommerce.exceptioons;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
