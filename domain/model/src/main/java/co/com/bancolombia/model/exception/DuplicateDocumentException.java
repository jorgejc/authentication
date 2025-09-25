package co.com.bancolombia.model.exception;

public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(String documentId) {
        super("The document is already registered: " + documentId);
    }
}
