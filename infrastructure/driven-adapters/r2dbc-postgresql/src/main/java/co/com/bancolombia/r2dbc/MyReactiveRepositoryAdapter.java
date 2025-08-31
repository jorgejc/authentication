package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.r2dbc.entities.UserEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations
        < User, UserEntity, String, MyReactiveRepository > implements UserRepository {

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.mapBuilder(d, User.UserBuilder.class).build());
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<Boolean> existsByDocumentId(String documentId) {
        return repository.existsByDocumentId(documentId);
    }
}
