package co.com.bancolombia.api;

import co.com.bancolombia.model.user.User;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.com.bancolombia.api.dto.request.CreateUserRecord;
import co.com.bancolombia.api.mapper.UserDTOMapper;
import co.com.bancolombia.usecase.user.UserUseCase;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private final RequestValidator requestValidator;
    private  final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;
//private  final UseCase2 useCase2;

    public Mono<ServerResponse> saveUseCase(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(CreateUserRecord.class)
                .flatMap(requestValidator::validateUser)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::saveUser)
                .flatMap(saveUser -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(saveUser)));
    }

    public Mono<ServerResponse> listenGETUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenGETOtherUseCase(ServerRequest serverRequest) {
        // useCase2.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }
}
