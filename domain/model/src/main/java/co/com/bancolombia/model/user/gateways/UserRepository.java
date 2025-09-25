package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> save(User user);
    Mono<User> findById(String id);
    Flux<User>  findAll();
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByDocumentId(String documentId);

}
