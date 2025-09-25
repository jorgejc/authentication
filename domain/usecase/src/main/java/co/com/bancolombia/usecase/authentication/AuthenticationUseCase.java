package co.com.bancolombia.usecase.authentication;

import co.com.bancolombia.model.authenticateuser.gateways.AuthenticateUserRepository;
import co.com.bancolombia.model.authenticateuser.gateways.PasswordService;
import co.com.bancolombia.model.authenticateuser.gateways.TokenProvider;
import co.com.bancolombia.model.exception.InvalidCredentialsException;
import co.com.bancolombia.usecase.authentication.interfaces.IAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthenticationUseCase implements IAuthenticationUseCase {

    private final AuthenticateUserRepository authenticateUserRepository;
    private final PasswordService passwordService;
    private final TokenProvider tokenProvider;
    @Override
    public Mono<AuthenticationResult> authenticate(String email, String password) {
        return authenticateUserRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(userAuth ->
                        passwordService.matches(password, userAuth.getEncodedPassword())
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .flatMap(ignored ->
                                    tokenProvider.generateToken(userAuth)
                                            .map(token -> new AuthenticationResult(
                                                    token,
                                                    tokenProvider.expirationTime(),
                                                    userAuth
                                            ))
                                )
                );
    }
}
