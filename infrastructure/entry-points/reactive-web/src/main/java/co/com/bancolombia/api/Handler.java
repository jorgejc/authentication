package co.com.bancolombia.api;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    private final RequestValidator requestValidator;
    private  final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;
    private final TransactionalOperator transactionalOperator;

    public Mono<ServerResponse> saveUseCase(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserRecord.class)
                .doOnNext(request -> log.info("Starting user creation with email: {}", request.email()))
                .flatMap(requestValidator::validateUser)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::register)
                .doOnNext(savedUser -> log.info("User successfully created with ID: {}", savedUser.getUserId()))
                .flatMap(saveUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(saveUser))).as(transactionalOperator::transactional)
                .doOnError(error -> log.error("Error creating user", error.getMessage()));
    }


}
