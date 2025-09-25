package co.com.bancolombia.model.authenticateuser.gateways;

import reactor.core.publisher.Mono;

public interface PasswordService {
    Mono<String> encode(String rawPassword);
    Mono<Boolean> matches(String rawPassword, String encodedPassword);
}
