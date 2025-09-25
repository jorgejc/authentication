/**
 * UseCaseConfig.java
 */
package co.com.bancolombia.api.config;

import co.com.bancolombia.model.authenticateuser.gateways.AuthenticateUserRepository;
import co.com.bancolombia.model.authenticateuser.gateways.PasswordService;
import co.com.bancolombia.model.authenticateuser.gateways.TokenProvider;
import co.com.bancolombia.usecase.authentication.AuthenticationUseCase;
import co.com.bancolombia.usecase.authentication.interfaces.IAuthenticationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * <b>Descripción:</b> Clase que determina la configuración de casos de uso para el módulo de autenticación.
 * Esta clase configura los beans necesarios para la inyección de dependencias
 * de los casos de uso relacionados con autenticación y registro de usuarios.
 * <br>
 * <b>HU03:</b>Agregar Autenticación al sistema
 *
 * @author Jorge Armando Julio Cruz
 */
@Configuration
public class UseCaseConfig {

    /**
     * Método encargado de configurar el bean del caso de uso de autenticación.
     * <br>
     * <b>HU03:</b>Agregar Autenticación al sistema
     *
     * @param userRepository, repositorio para buscar usuarios para autenticación
     * @param passwordService, servicio para verificar contraseñas
     * @param tokenProvider, proveedor de tokens JWT
     * @return IAuthenticationUseCase Instancia del caso de uso de autenticación
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     */
    @Bean("crediyaAuthenticationUseCase")
    @Primary
    public IAuthenticationUseCase authenticationUseCase(
            AuthenticateUserRepository userRepository,
            PasswordService passwordService,
            TokenProvider tokenProvider) {
        return new AuthenticationUseCase(userRepository, passwordService, tokenProvider);
    }
}
