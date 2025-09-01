package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exception.DuplicateDocumentException;
import co.com.bancolombia.model.exception.DuplicateEmailException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserUseCaseTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserUseCase userUseCase;

    User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .documentId("123456789")
                .name("Juan")
                .lastname("Pérez")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("Calle 123")
                .email("juan@gmail.com")
                .phone("3001234567")
                .baseSalary(BigDecimal.valueOf(2000000))
                .build();
    }

    /**
     *  Test: register successfull
     */
    @Test
    void register_shouldCreateUserSuccessfully() {
        when(userRepository.existsByEmail("juan@gmail.com"))
                .thenReturn(Mono.just(false));
        when(userRepository.existsByDocumentId("123456789"))
                .thenReturn(Mono.just(false));
        when(userRepository.save(testUser))
                .thenReturn(Mono.just(testUser));

        Mono<User> result = userUseCase.register(testUser);

        StepVerifier.create(result)
                .expectNext(testUser)
                .verifyComplete();

        verify(userRepository).existsByEmail("juan@gmail.com");
        verify(userRepository).existsByDocumentId("123456789");
        verify(userRepository).save(testUser);
        verifyNoMoreInteractions(userRepository);
    }

    /**
     *  Test: duplicate email
     */
    @Test
    void register_shouldThrowDuplicateEmailException_whenEmailExists() {
        when(userRepository.existsByEmail("juan@gmail.com"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.register(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateEmailException &&
                                throwable.getMessage().contains("juan@gmail.com"))
                .verify();

        verify(userRepository).existsByEmail("juan@gmail.com");
        verify(userRepository, never()).existsByDocumentId(any());
        verify(userRepository, never()).save(any());
    }

    /**
     *  Test: duplicate document
     */
    @Test
    void register_shouldThrowDuplicateDocumentException_whenDocumentExists() {
        when(userRepository.existsByEmail("juan@gmail.com"))
                .thenReturn(Mono.just(false));
        when(userRepository.existsByDocumentId("123456789"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.register(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateDocumentException &&
                                throwable.getMessage().contains("123456789"))
                .verify();

        verify(userRepository).existsByEmail("juan@gmail.com");
        verify(userRepository).existsByDocumentId("123456789");
        verify(userRepository, never()).save(any());
    }

    /**
     *  Test: getById successful
     */
    @Test
    void getById_shouldReturnUser_whenUserExists() {
        String userId = "user123";
        when(userRepository.findById(userId))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(userUseCase.getById(userId))
                .expectNext(testUser)
                .verifyComplete();

        verify(userRepository).findById(userId);
    }

    /**
     *  Test: User not found
     */
    @Test
    void getById_shouldThrowValidationException_whenUserNotFound() {
        String userId = "nonexistent";
        when(userRepository.findById(userId))
                .thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.getById(userId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().contains("User not found!: " + userId))
                .verify();

        verify(userRepository).findById(userId);
    }

    /**
     *  Test: getAll
     */
    @Test
    void getAll_shouldReturnAllUsers() {
        User user2 = User.builder()
                .documentId("987654321")
                .email("maria@gmail.com")
                .name("Maria")
                .build();

        when(userRepository.findAll())
                .thenReturn(Flux.just(testUser, user2));

        StepVerifier.create(userUseCase.getAll())
                .expectNext(testUser)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyFlux_whenNoUsers() {
        when(userRepository.findAll())
                .thenReturn(Flux.empty());

        StepVerifier.create(userUseCase.getAll())
                .verifyComplete();

        verify(userRepository).findAll();
    }

}
