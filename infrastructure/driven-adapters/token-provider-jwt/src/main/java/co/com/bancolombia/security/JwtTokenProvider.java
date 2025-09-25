/**
 * JwtTokenProvider.java
 */
package co.com.bancolombia.security;

import co.com.bancolombia.model.authenticateuser.AuthenticateUser;
import co.com.bancolombia.model.authenticateuser.gateways.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * <b>Descripción:</b> Clase que determina la implementación principal del proveedor de tokens JWT que genera tokens
 * seguros utilizando la librería JWT. Esta clase es responsable de crear tokens JWT válidos
 * con información del usuario autenticado
 * <br>
 * <b>HU03:</b> Agregar Autenticación al sistema
 *
 * @author Jorge Armando Julio Cruz
 */
@Component
@Primary
public class JwtTokenProvider implements TokenProvider {

    /**
     * Atributo que determina la clave secreta utilizada para firmar y verificar los tokens JWT.
     * Se genera a partir de una cadena base64 configurada externamente.
     */
    private final SecretKey key;

    /**
     * Atributo que determina el tiempo de expiración de los tokens en segundos.
     */
    private final long expSeconds;

    /**
     * Atributo que determina el identificador del emisor del token (issuer claim).
     * Es utilizado para identificar quién emitió el token JWT.
     */
    private final String issuer;

    /**
     * Constructor que inicializa el proveedor de tokens JWT con la configuración necesaria.
     * @param base64Secret, clave secreta codificada en Base64 para firmar los tokens.
     * @param expSeconds, tiempo de expiración en segundos. Debe ser mayor a 0.
     * @param issuer, identificador del emisor del token. No debe ser null.
     */
    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String base64Secret,
            @Value("${security.jwt.expiration-seconds:3600}") long expSeconds,
            @Value("${security.jwt.issuer:autenticacion}") String issuer
    ) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));;
        this.expSeconds = expSeconds;
        this.issuer = issuer;
    }

    /**
     * Genera un token JWT seguro para el usuario autenticado con toda la información
     * necesaria para autorización posterior.
     * <br>
     * <p><b>Estructura del Token JWT:</b></p>
     * <ul>
     * <li><b>Subject (sub):</b> ID único del usuario</li>
     * <li><b>Issuer (iss):</b> Identificador del sistema emisor</li>
     * <li><b>Issued At (iat):</b> Timestamp de creación</li>
     * <li><b>Expiration (exp):</b> Timestamp de expiración</li>
     * <li><b>Email (claim personalizado):</b> Correo electrónico del usuario</li>
     * <li><b>Role (claim personalizado):</b> Rol del usuario en el sistema</li>
     * <li><b>Permissions (claim personalizado):</b> Lista de permisos del usuario</li>
     * </ul>
     *
     * <p><b>Configuración de Seguridad:</b></p>
     * <ul>
     * <li>Algoritmo: HMAC-SHA256 (HS256)</li>
     * <li>Firma digital con clave secreta configurada</li>
     * <li>Tiempo de expiración configurable</li>
     * </ul>
     *
     * <p><b>Programación Reactiva:</b> Retorna el token envuelto en un Mono para
     * compatibilidad con el stack reactivo de Spring WebFlux.</p>
     *
     * @param user, Usuario autenticado con toda la información necesaria.
     * @return Mono&lt;String&gt; Token JWT firmado y válido. El Mono nunca será vacío.
     */
    @Override
    public Mono<String> generateToken(AuthenticateUser user) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(user.getId())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("permissions", user.getPermissions())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return Mono.just(token);
    }

    /**
     * Retorna el tiempo de expiración configurado para los tokens JWT
     * <p>Este valor se utiliza tanto para configurar la expiración en el token
     * como para informar a los clientes sobre cuándo deben renovar sus tokens.</p>
     * @return long Tiempo de expiración en segundos. Siempre mayor a 0.
     */
    @Override
    public long expirationTime() {
        return expSeconds;
    }
}
