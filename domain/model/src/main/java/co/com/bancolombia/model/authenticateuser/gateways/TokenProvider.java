package co.com.bancolombia.model.authenticateuser.gateways;

import co.com.bancolombia.model.authenticateuser.AuthenticateUser;
import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> generateToken(AuthenticateUser user);
    long expirationTime();
}
