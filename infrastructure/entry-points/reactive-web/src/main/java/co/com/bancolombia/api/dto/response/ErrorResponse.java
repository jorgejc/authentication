package co.com.bancolombia.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private int status;
    private String error;
    private String timestamp;

    public ErrorResponse() {
        this.status = 500;
        this.error = "Internal Server Error";
        this.timestamp = java.time.Instant.now().toString();
    }

    public ErrorResponse(int status, String error) {
        this.status = status;
        this.error = error;
        this.timestamp = java.time.Instant.now().toString();
    }
}
