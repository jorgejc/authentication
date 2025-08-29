package co.com.bancolombia.api.exceptions;

import co.com.bancolombia.api.dto.response.ErrorResponse;
import co.com.bancolombia.model.exception.DuplicateEmailException;
import co.com.bancolombia.model.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            log.warn("Email duplicado: {}", ex.getMessage());

        } else if (ex instanceof ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = new ErrorResponse(400, ex.getMessage());
            log.warn("Error de validación: {}", ex.getMessage());

        } else if (ex instanceof WebExchangeBindException webEx) {
            status = HttpStatus.BAD_REQUEST;
            String validationMessage = webEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid data provided");
            errorResponse = new ErrorResponse(400, validationMessage);
            log.warn("Error de validación de request: {}", validationMessage);

        } else if (ex instanceof jakarta.validation.ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = new ErrorResponse(400, "Invalid data provided");
            log.warn("Error de validación Jakarta: {}", ex.getMessage());

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new ErrorResponse();
            log.error("Error interno del servidor: ", ex);
        }

        response.setStatusCode(status);

        String responseBody;
        try {
            responseBody = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializando respuesta de error", e);
            responseBody = "{\"status\":500,\"error\":\"Internal server error\",\"timestamp\":\"" +
                    java.time.Instant.now().toString() + "\"}";
        }

        DataBufferFactory bufferFactory = response.bufferFactory();
        return response.writeWith(Mono.just(bufferFactory.wrap(responseBody.getBytes())));
    }
}
