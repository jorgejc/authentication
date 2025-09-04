package co.com.bancolombia.api.exceptions;

import co.com.bancolombia.api.dto.response.ErrorResponse;
import co.com.bancolombia.model.exception.DuplicateEmailException;
import co.com.bancolombia.model.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse;
        HttpStatus status;

        if (ex instanceof DuplicateEmailException) {
            status = HttpStatus.CONFLICT;
            errorResponse = new ErrorResponse(409, "Email is not available");
            log.warn("duplicate email: {}", ex.getMessage());

        } else if (ex instanceof DataIntegrityViolationException) {
            status = HttpStatus.BAD_REQUEST;
            String message = ex.getMessage();

            if (message.contains("document_id") && message.contains("not null")) {
                errorResponse = new ErrorResponse(400, "Document ID is required");
            } else if (message.contains("duplicate") || message.contains("unique")) {
                errorResponse = new ErrorResponse(409, "Duplicate value detected");
            } else {
                errorResponse = new ErrorResponse(400, "Data integrity violation");
            }
            log.warn("Error de integridad de datos: {}", ex.getMessage());

        } else if (ex instanceof ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            status = HttpStatus.BAD_REQUEST;
            String message = ex.getMessage();
            log.warn("validation error: {}", ex.getMessage());

            if (message.contains("Validation errors:")) {
                String errorList = message.replace("Validation errors:", "");
                String[] errors = errorList.split(",");
                errorResponse = new ErrorResponse(400, "Validation failed", List.of(errors));
            } else {
                errorResponse = new ErrorResponse(400, message);
                log.warn("validation error: {}", ex.getMessage());
            }

        } else if (ex.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = new ErrorResponse(400, "Invalid JSON format");
            log.warn("Invalid JSON format: {}", ex.getMessage());

        } else if (ex instanceof WebExchangeBindException webEx) {
            status = HttpStatus.BAD_REQUEST;
            String validationMessage = webEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid data provided");
            errorResponse = new ErrorResponse(400, validationMessage);
            log.warn("Request validation error: {}", validationMessage);

        } else if (ex instanceof jakarta.validation.ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = new ErrorResponse(400, "Invalid data provided");
            log.warn("Jakarta Validation Error: {}", ex.getMessage());

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new ErrorResponse();
            log.error("Internal server error: ", ex);
        }

        response.setStatusCode(status);

        String responseBody;
        try {
            responseBody = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            responseBody = "{\"status\":500,\"error\":\"Internal server error\",\"timestamp\":\"" +
                    java.time.Instant.now().toString() + "\"}";
        }

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.just(bufferFactory.wrap(responseBody.getBytes())));
    }
}
