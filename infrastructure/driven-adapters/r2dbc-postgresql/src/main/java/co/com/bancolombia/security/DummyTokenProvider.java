/**
 * DummyTokenProvider.java
 */
package co.com.bancolombia.security;

import co.com.bancolombia.model.authenticateuser.AuthenticateUser;
import co.com.bancolombia.model.authenticateuser.gateways.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * <b>Descripción:</b> Clase que determina la implementación de prueba del proveedor de tokens JWT que genera tokens ficticios
 * para entornos de desarrollo y testing local. Esta clase NO debe usarse en producción ya que
 * genera tokens falsos sin validación de seguridad real.
 * <br>
 * <b>HU03:</b> Agregar Autenticación al sistema
 * <br>
 * <b>Patrón:</b> Strategy Pattern - Impelemnta la interfaz TokenProvider
 * @author Jorge Armando Julio Cruz
 */
@Component
@Profile({"dev","local"})
@Slf4j
public class DummyTokenProvider implements TokenProvider {

    /**
     * Atributo que determina el tiempo de expiración por defecto para los tokens ficticios en segundos (1 hora)
     */
    private static final long EXP_SECONDS = 3600L;

    /**
     * Genera un token ficticio para el usuario proporcionado. Este token no contiene
     * información real del usuario ni implementa seguridad JWT estándar.
     * @param user
     * @return Mono<String> Token ficticio con formato "FAKE-{UUID}". El Mono nunca será vacío.
     */
    @Override
    public Mono<String> generateToken(AuthenticateUser user) {
        log.debug("Generating fake token for user: {}", user.getEmail());
        return Mono.just("FAKE-" + UUID.randomUUID());
    }

    /**
     * Retorna el tiempo de expiración configurado para los tokens ficticios.
     * @return long Tiempo de expiración en segundos (3600 = 1 hora)
     */
    @Override
    public long expirationTime() {
        return EXP_SECONDS;
    }
}
