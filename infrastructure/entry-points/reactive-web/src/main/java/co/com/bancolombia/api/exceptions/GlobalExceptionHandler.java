package co.com.bancolombia.api.exceptions;

public class ErrorResponse {

    protected static final String ERROR_DESCRIPTION = "Error unexpected";
    protected static final int ERROR_CODE = 500;

    private int status;
    private String error;

    public ErrorResponse() {
        this.status = ERROR_CODE;
        this.error = ERROR_DESCRIPTION;
    }

    public ErrorResponse(int status, String error) {
        this.status = status;
        this.error = error;
    }
}
