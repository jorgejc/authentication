/**
 * PasswordHashServiceImp.java
 */
package co.com.bancolombia.security;

import co.com.bancolombia.model.authenticateuser.gateways.PasswordService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@Profile({"dev","local"})
public class PasswordHashServiceImp implements PasswordService {


    @Override
    public Mono<String> encode(String rawPassword) {
        return Mono.just(rawPassword);
    }

    @Override
    public Mono<Boolean> matches(String rawPassword, String encodedPassword) {
        return Mono.just(Objects.equals(rawPassword, encodedPassword));
    }
}
