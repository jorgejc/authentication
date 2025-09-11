package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entities.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MyReactiveRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {


    Mono<Void> deleteByUserId(String id);
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByDocumentId(String documentId);
}
