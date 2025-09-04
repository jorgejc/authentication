package co.com.bancolombia.model.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("The email is already registered: " + email);
    }
}
