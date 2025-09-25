package co.com.bancolombia.r2dbc.userauth;

import co.com.bancolombia.r2dbc.entities.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserAuthReactiveRepository extends ReactiveCrudRepository<UserEntity, String> {
    Mono<UserEntity> findByEmail(String email);
}
