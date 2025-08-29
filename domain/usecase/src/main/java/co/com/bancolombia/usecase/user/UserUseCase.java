package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exception.DuplicateEmailException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.interfaces.IUserUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;


    @Override
    public Mono<User> register(User user) {

        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
            if (Boolean.TRUE.equals(exists))
                return Mono.error(new DuplicateEmailException(user.getEmail()));

            return userRepository.save(user);
        });
    }

    @Override
    public Mono<User> getById(String id) {
        return userRepository.findById(id).switchIfEmpty(Mono.error(new ValidationException("User not found!: " + id)));
    }

    @Override
    public Flux<User> getAll() {
        return userRepository.findAll();
    }

}
