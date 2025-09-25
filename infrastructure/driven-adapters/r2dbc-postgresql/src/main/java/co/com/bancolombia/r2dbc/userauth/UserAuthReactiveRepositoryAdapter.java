/**
 * UserAuthReactiveRepositoryAdapter.java
 */
package co.com.bancolombia.r2dbc.userauth;

import co.com.bancolombia.model.authenticateuser.AuthenticateUser;
import co.com.bancolombia.model.authenticateuser.gateways.AuthenticateUserRepository;
import co.com.bancolombia.model.exception.InvalidCredentialsException;
import co.com.bancolombia.r2dbc.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserAuthReactiveRepositoryAdapter implements AuthenticateUserRepository {

    private final UserAuthReactiveRepository userAuthRepository;
    @Override
    public Mono<AuthenticateUser> findByEmail(String email) {
        log.debug("User searching for authentication: {}", email);

        return userAuthRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .map(this::toDomain)
                .doOnSuccess(userAuth ->
                        log.debug("Usuario encontrado para autenticación: {}", userAuth.getEmail()));
    }

    private AuthenticateUser toDomain(UserEntity entity) {
        return AuthenticateUser.builder()
                .id(entity.getUserId())
                .email(entity.getEmail())
                .encodedPassword(entity.getPassword())
                .role(entity.getRolId() == 1 ? "ADMIN" : entity.getRolId() == 2 ? "ASESOR" : "CLIENT")
                .permissions(List.of())
                .build();
    }
}
