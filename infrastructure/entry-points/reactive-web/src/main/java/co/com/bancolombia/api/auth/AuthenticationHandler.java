/**
 * AuthenticationHandler.java
 */
package co.com.bancolombia.api.auth;

import co.com.bancolombia.api.RequestValidator;
import co.com.bancolombia.api.dto.request.AuthenticateRequest;
import co.com.bancolombia.api.mapper.AuthMapper;
import co.com.bancolombia.usecase.authentication.interfaces.IAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * <b>Descripción:</b> Clase que determina: Handler reactivo para el proceso de autenticación de usuarios
 * Este componente maneja las solicitudes de autenticación, validando las credenciales
 * del usuario y generando tokens JWT para acceso a recursos protegidos.
 * <br>
 * <b>HU03: Agregar Autenticación al sistema</b>
 *
 * @author Jorge Armando Julio Cruz>
 * @Version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationHandler {

    private final RequestValidator requestValidator;
    private final IAuthenticationUseCase authenticationUseCase;
    private final AuthMapper authMapper;


    /**
     * Método encargado de procesar la autenticación de usuarios en el sistema.
     * Recibe las credenciales del usuario (email y contraseña), las valida
     * usando Bean Validation, verifica su autenticidad contra la base de datos
     * y genera un token JWT si la autenticación es exitosa.
     * <br>
     * <b>HU03: </b> Agregar Autenticación al sistema
     *
     * @param serverRequest Solicitud del servidor que contiene las credenciales
     * del usuario en formato JSON (AuthenticateRequest)
     * @return Mono&lt;ServerResponse&gt; Respuesta HTTP reactiva que contiene:
     * - 200 OK: Token JWT y datos del usuario si auth exitosa
     * - 400 Bad Request: Si hay errores de validación
     * - 401 Unauthorized: Si las credenciales son incorrectas
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     * @see AuthenticateRequest
     * @see co.com.bancolombia.api.dto.response.AuthenticateResponse
     */
    public Mono<ServerResponse> authenticate(ServerRequest serverRequest) {
        log.debug("Starting authentication process");

        return serverRequest.bodyToMono(AuthenticateRequest.class)
                .doOnNext(request -> log.debug("Authentication request received for email: {}", request.email()))
                .flatMap(requestValidator::validate)
                .flatMap(authenticateRequest -> {
                    log.info("Authentication attempt: {}", authenticateRequest.email());
                    return authenticationUseCase.authenticate(authenticateRequest.email(), authenticateRequest.password())
                            .doOnSuccess(result ->
                                    log.info("Authentication successful: {}", result.user().getEmail()))
                            .doOnError(error ->
                                    log.warn("Authentication failed for: {} - Reason: {}",
                                            authenticateRequest.email(), error.getMessage()));
                })
                .map(authMapper::toAuthenticateResponse)
                .flatMap(response -> {
                    log.debug("Generating successful authentication response");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}
