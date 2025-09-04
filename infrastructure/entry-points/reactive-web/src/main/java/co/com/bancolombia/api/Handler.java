package co.com.bancolombia.api;

import co.com.bancolombia.model.exception.ValidationException;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.com.bancolombia.api.dto.request.CreateUserRecord;
import co.com.bancolombia.api.mapper.UserDTOMapper;
import co.com.bancolombia.usecase.user.UserUseCase;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Valid
public class Handler {

    private final RequestValidator requestValidator;
    private  final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;
    private final TransactionalOperator transactionalOperator;

    public Mono<ServerResponse> saveUseCase(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserRecord.class)
                .doOnNext(request -> log.info("Starting user creation with email: {}", request.email()))
                .flatMap(this::validateRequest)
                .flatMap(requestValidator::validateUser)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::register)
                .doOnNext(savedUser -> log.info("User successfully created with ID: {}", savedUser.getUserId()))
                .flatMap(saveUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(saveUser))).as(transactionalOperator::transactional)
                .doOnError(error -> log.error("Error creating user", error.getMessage()));
    }

    private Mono<CreateUserRecord> validateRequest(CreateUserRecord request) {
        Set<ConstraintViolation<CreateUserRecord>> violations = Validation.buildDefaultValidatorFactory()
                .getValidator().validate(request);
        if (!violations.isEmpty()) {
            List<String> errorMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
            return Mono.error(new ValidationException("Validation errors:" + String.join(",", errorMessages)));
        }
        return Mono.just(request);
    }


}
