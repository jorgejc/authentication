/**
 * InvalidCredentialsException.java
 */
package co.com.bancolombia.model.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("invalid credentials");
    }
}
