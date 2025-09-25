package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exception.DuplicateDocumentException;
import co.com.bancolombia.model.exception.DuplicateEmailException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.interfaces.IUserUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class UserUseCase implements IUserUseCase {

    private final UserRepository userRepository;


    @Override
    public Mono<User> register(User user) {
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(emailExists -> {
                    if (Boolean.TRUE.equals(emailExists)) {
                        return Mono.error(new DuplicateEmailException(user.getEmail()));
                    }
                    return userRepository.existsByDocumentId(user.getDocumentId());
                })
                .flatMap(docExist -> {
                    if (Boolean.TRUE.equals(docExist)) {
                        return Mono.error(new DuplicateDocumentException("Document ID already exists: " + user.getDocumentId()));
                    }
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

    public Mono<Boolean> existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
