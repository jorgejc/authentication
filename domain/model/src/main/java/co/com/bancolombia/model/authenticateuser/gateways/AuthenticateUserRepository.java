package co.com.bancolombia.model.authenticateuser.gateways;

import co.com.bancolombia.model.authenticateuser.AuthenticateUser;
import reactor.core.publisher.Mono;

public interface AuthenticateUserRepository {
    Mono<AuthenticateUser> findByEmail(String email);
}
