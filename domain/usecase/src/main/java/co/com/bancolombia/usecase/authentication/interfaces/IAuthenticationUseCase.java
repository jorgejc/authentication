package co.com.bancolombia.usecase.authentication.interfaces;

import co.com.bancolombia.usecase.authentication.AuthenticationResult;
import reactor.core.publisher.Mono;

public interface IAuthenticationUseCase {
    Mono<AuthenticationResult> authenticate(String email, String password);
}
