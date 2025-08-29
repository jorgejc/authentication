package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.exception.DuplicateEmailException;
import co.com.bancolombia.model.user.exception.ValidationException;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.intarfaces.IUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUseCase implements IUserUseCase {<

    private final UserRepository userRepository;


    @Override
    public Mono<User> saveUser(User user) {
        log.info("Iniciando registro de usuario con email: {}", user.getEmail());

        return validateEmailNotExists(user.getEmail())
                .then(validateBusinessRules(user))
                .then(userRepository.save(user))
                .doOnSucces(savedUser ->
                        log.info("Usuario registrado exitosamente con ID: {}", savedUser.getUserId()))
                .doOnError(Erro > log.error("Error al registrar usuario: {}", error.getMessage()));
    }

    private Mono<Void> validateEmailNotExists(String email) {
        log.debug("Validando que el email no exista: {}", email);

        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Intento de registro con email duplicado: {}", email);
                        return Mono.error(new DuplicateEmailException(
                                "El correo electrónico ya está registrado"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<User> validateBusinessRules(User user) {
        log.debug("Validando reglas de negocio para usuario: {}", user.getEmail());

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return Mono.error(new ValidationException("El nombre es obligatorio"));
        }

        if (user.getLastname() == null || user.getLastname().trim().isEmpty()) {
            return Mono.error(new ValidationException("El apellido es obligatorio"));
        }

        if (user.getUserId() == null) {
            user.setUserId(java.util.UUID.randomUUID().toString());
        }

        return Mono.just(user);
    }
}
