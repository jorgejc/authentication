package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.AuthenticateRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * <b>Descripción:</b> Clase que determina el validador reactivo de solicitudes HTTP.
 * Esta clase proporciona validación de objetos DTO usando Bean Validation
 * en un contexto reactivo, transformando errores de validación en excepciones
 * apropiadas para el manejo de errores de la aplicación.
 * <br>
 * <b>HU01: Registrar usuarios en el sistema</b>
 *
 * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    /**
     * Método encargado de validar un objeto DTO usando Bean Validation de forma reactiva.
     * Este método genérico puede validar cualquier objeto que tenga anotaciones
     * de validación de Jakarta Validation (como @NotBlank, @Email, etc.).
     * Si encuentra errores, lanza una ValidationException con los mensajes
     * de error concatenados.
     * <br>
     * <b>HU01: Registrar usuarios en el sistema</b>
     *
     * @param dto Objeto DTO que contiene las anotaciones de validación
     * @param <T> Tipo del objeto a validar
     * @return Mono<T> El mismo objeto validado si no hay errores
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     */
    public <T> Mono<T> validate(T dto) {
        return Mono.fromCallable(() -> {
            var errors = new BeanPropertyBindingResult(dto, dto.getClass().getName());
            validator.validate(dto, errors);
            if (errors.hasErrors()) {
                String detail = errors.getAllErrors().stream()
                        .map(e -> e.getDefaultMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("application submitted");
                throw new ValidationException(errors.toString());
            }
            return dto;
        });
    }
}