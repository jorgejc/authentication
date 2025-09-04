package co.com.bancolombia.model.exception;

public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(String documentId) {
        super("The email is already registered: " + documentId);
    }
}
