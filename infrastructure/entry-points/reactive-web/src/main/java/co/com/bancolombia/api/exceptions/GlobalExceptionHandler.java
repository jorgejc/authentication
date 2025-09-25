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

/**
 * <b>Descripción:</b> Clase que determina el manejador global de excepciones para aplicaciones Spring WebFlux que
 * intercepta y procesa todas las excepciones no manejadas en la aplicación, proporcionando
 * respuestas HTTP estructuradas y consistentes para diferentes tipos de errores.
 * <br>
 * <b>HU01 - HU03:</b>Agregar Autenticación al sistema - Registrar usuarios en el sistema
 *
 * @author Jorge Armando Julio Cruz
 */
@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * Atributo que determina el mapper de Jackson para serialización JSON de las respuestas de error.
     * Se inicializa con configuración por defecto.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Maneja todas las excepciones no capturadas en la aplicación y las convierte en
     * respuestas HTTP apropiadas con formato JSON estándar.
     * * <p><b>Tipos de Excepciones Manejadas:</b></p>
     * <ul>
     * <li><b>Seguridad:</b> AccessDeniedException, InvalidBearerTokenException, AuthenticationException</li>
     * <li><b>Credenciales:</b> InvalidCredentialsException</li>
     * <li><b>Validación:</b> ValidationException, WebExchangeBindException, Jakarta ValidationException</li>
     * <li><b>Duplicados:</b> DuplicateEmailException, DuplicateDocumentException</li>
     * <li><b>Base de Datos:</b> DataIntegrityViolationException</li>
     * <li><b>JSON:</b> JsonParseException</li>
     * <li><b>Genéricas:</b> Cualquier otra excepción no especificada</li>
     * </ul>
     *
     * @param exchange, contexto del intercambio web reactivo que contiene request y response.
     * @param ex, ex Excepción que será procesada y convertida en respuesta HTTP.
     * @return Mono&lt;Void&gt; Mono vacío que representa la escritura completa de la respuesta HTTP.
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse;
        HttpStatus status;

        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            errorResponse = new ErrorResponse(403, "Acceso denegado - No tienes permisos suficientes");
            log.warn("Acceso denegado: {}", ex.getMessage());

        } else if (ex instanceof org.springframework.security.oauth2.server.resource.InvalidBearerTokenException) {
            status = HttpStatus.UNAUTHORIZED;
            errorResponse = new ErrorResponse(401, "Token inválido o expirado");
            log.warn("Token inválido: {}", ex.getMessage());

        } else if (ex instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            errorResponse = new ErrorResponse(401, "Credenciales inválidas");
            log.warn("Error de autenticación: {}", ex.getMessage());

        } else if (ex instanceof co.com.bancolombia.model.exception.InvalidCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            errorResponse = new ErrorResponse(401, "Credenciales inválidas");
            log.warn("Intento de login con credenciales inválidas: {}", ex.getMessage());

        } else if (ex instanceof co.com.bancolombia.model.exception.ValidationException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new ErrorResponse(500, "Error interno de datos");
            log.error("Inconsistencia de datos: {}", ex.getMessage(), ex);

        } if (ex instanceof DuplicateEmailException) {
            status = HttpStatus.CONFLICT;
            errorResponse = new ErrorResponse(409, "Email is not available");
            log.warn("duplicate email: {}", ex.getMessage());

        } else if (ex instanceof co.com.bancolombia.model.exception.DuplicateDocumentException) {
                status = HttpStatus.CONFLICT;
                errorResponse = new ErrorResponse(409, "The identity document is already registered");
                log.warn("Duplicate document: {}", ex.getMessage());

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
