package co.com.bancolombia.api;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.com.bancolombia.api.dto.request.CreateUserRecord;
import co.com.bancolombia.api.mapper.UserDTOMapper;
import co.com.bancolombia.usecase.user.UserUseCase;
import reactor.core.publisher.Mono;

/**
 * <b>Descripción:</b> Clase que determina la gestión de usuarios del sistema CrediYa.
 * Maneja las operaciones HTTP relacionadas con usuarios, incluyendo registro
 * y verificación de existencia por email. Implementa validaciones reactivas
 * y control de acceso basado en roles.
 * <br>
 * <b>HU01:</b> Registrar usuarios en el sistema
 *
 * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    private final RequestValidator requestValidator;
    private  final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;
    private final TransactionalOperator transactionalOperator;

    /**
     * Método encargado de registrar un nuevo usuario en el sistema.
     * Este endpoint permite a administradores y asesores registrar nuevos usuarios.
     * Valida los datos de entrada, procesa el registro y retorna la información
     * del usuario creado.
     * <br>
     * <b>HU01-HU03:</b> Registrar usuarios en el sistema
     *
     * @param serverRequest, solicitud HTTP que contiene los datos del usuario
     * @return Mono&lt;ServerResponse&gt; Respuesta con el usuario registrado o error
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public Mono<ServerResponse> saveUseCase(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserRecord.class)
                .doOnNext(request -> log.info("Starting user creation with email: {}", request.email()))
                .flatMap(requestValidator::validate)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::register)
                .doOnNext(savedUser -> log.info("User successfully created with ID: {}", savedUser.getUserId()))
                .flatMap(saveUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(saveUser))).as(transactionalOperator::transactional)
                .doOnError(error -> log.error("Error creating user", error.getMessage()));
    }

    /**
     * Método encargado de verificar si existe un usuario con el email especificado.
     * <br>
     * <b>Caso de Uso:</b>
     *
     * @param serverRequest
     * @return
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     */
    public Mono<ServerResponse> existsByEmailUseCase(ServerRequest serverRequest) {
        String email = serverRequest.pathVariable("email");
        log.info("Checking if a user exists with email: {}", email);
        return userUseCase.existsUserByEmail(email)
                .flatMap(exists -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"existsUser\": " + exists + "}"));
    }

}
