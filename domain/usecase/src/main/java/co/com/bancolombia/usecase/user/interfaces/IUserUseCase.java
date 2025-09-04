package co.com.bancolombia.usecase.user.interfaces;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import co.com.bancolombia.model.user.User;

public interface IUserUseCase {

    Mono<User> register(User user);

    Mono<User> getById(String id);

    Flux<User> getAll();

}
