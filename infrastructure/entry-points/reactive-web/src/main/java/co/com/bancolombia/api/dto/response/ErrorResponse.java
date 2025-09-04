package co.com.bancolombia.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ErrorResponse {

    private int status;
    private String error;
    private List<String> errors;
    private String timestamp;

    public ErrorResponse() {
        this.status = 500;
        this.error = "Internal Server Error";
        this.timestamp = java.time.Instant.now().toString();
    }

    public ErrorResponse(int status, String error) {
        this.status = status;
        this.error = error;
        this.errors = new ArrayList<>();
        this.timestamp = java.time.Instant.now().toString();
    }

    public ErrorResponse(int status, String error, List<String> errors) {
        this.status = status;
        this.error = error;
        this.errors = errors;
        this.timestamp = timestamp;
    }

}
