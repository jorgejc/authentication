/**
 * SecurityConfig.java
 */

package co.com.bancolombia.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <b>Descripción:</b> Clase que determina la configuración de seguridad reactiva para el sistema.
 * Esta clase configura toda la seguridad de la aplicación utilizando Spring Security Reactive
 * con autenticación basada en tokens JWT. Implementa un modelo de seguridad stateless
 * donde cada request debe incluir un token JWT válido para acceder a recursos protegidos.
 * <br>
 * <b>HU03:</b> Agregar Autenticación al sistema
 *
 * @author Jorge Armando Julio Cruz
 * @version 1.0
 */
@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    /**
     * Método encargado de configurar la cadena de filtros de seguridad reactiva para la aplicación.
     * Define las reglas de autorización por endpoint, configurando qué rutas son públicas
     * y cuáles requieren autenticación. También establece el manejo de tokens JWT
     * y la gestión de errores de autenticación/autorización.
     * <br>
     * <b>HU03:</b>Agregar Autenticación al sistema
     *
     * @param http, configurador de seguridad HTTP del servidor reactivo
     * @param jwtDecoder, decodificador de tokens JWT para validación
     * @param jwtAuthConverter, convertidor de JWT a objeto de autenticación de Spring
     * @return SecurityWebFilterChain Cadena de filtros de seguridad configurada
     * @author Jorge Armando Julio Cruz
     */
   @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder,
            ReactiveJwtAuthenticationConverterAdapter jwtAuthConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(h -> h.frameOptions(fr -> fr.disable()))
                .authorizeExchange(exchanges  -> exchanges

                        .pathMatchers("/api/v1/login").permitAll()
//                        .pathMatchers(HttpMethod.POST, "/api/v1/users").permitAll()

                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .pathMatchers("/h2/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("ADMIN","ASESOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("ADMIN","ASESOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/email/{email}/exists").authenticated()

                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .authenticationEntryPoint((exchange, authException) -> {
                            String detail = "Authentication is required";

                            if (authException instanceof org.springframework.security.oauth2.server.resource.InvalidBearerTokenException) {
                                detail = "Invalid or expired token";
                            } else if (authException instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth2Ex) {
                                String errorCode = oauth2Ex.getError().getErrorCode();
                                if ("invalid_token".equals(errorCode) || "invalid_request".equals(errorCode)) {
                                    detail = "Token inválido o expirado";
                                }
                            }
                            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", detail);
                        })
                        .accessDeniedHandler((exchange, accessDenied) ->
                                buildErrorResponse(exchange, HttpStatus.FORBIDDEN, "Access denied",
                                        "You do not have permission to perform this action")
                        )
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthConverter))
                )
                .build();
    }

    /**
     * Método encargado de configurar el decodificador de tokens JWT reactivo.
     * Crea un decodificador que valida la firma de los tokens JWT usando una clave secreta
     * compartida. Los tokens deben estar firmados con el algoritmo HMAC SHA-256.
     * La clave secreta se obtiene de la configuración de la aplicación.
     * <br>
     * <b>HU03:</b>Agregar Autenticación al sistema
     *
     * @param base64Secret, clave secreta codificada en Base64 para validar tokens JWT
     * @return ReactiveJwtDecoder, decodificador reactivo configurado para validar tokens
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String base64Secret) {
        var secretKey = new SecretKeySpec(Base64.getDecoder().decode(base64Secret), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Método encargado de configurar el convertidor de autenticación JWT reactivo.
     * Este bean transforma un token JWT válido en un objeto Authentication de Spring Security.
     * Extrae los roles y permisos del token y los convierte en GrantedAuthority para
     * que Spring Security pueda realizar la autorización.
     * <br>
     * <b>HU03:</b>Agregar Autenticación al sistema
     *
     * @return ReactiveJwtAuthenticationConverterAdapter Convertidor reactivo configurado
     * @author Jorge Armando Julio Cruz
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthConverter() {
        var delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }

    /**
     * Método encargado de extraer roles y permisos de un token JWT para crear autoridades de Spring Security.
     * Analiza los claims del token JWT para obtener:
     * <br>
     * - role: Se convierte en una autoridad con prefijo
     * <br>
     * - permissions: Lista de permisos específicos
     * <br>
     * <b>HU03:</b>Agregar Autenticación al sistema
     *
     * @param jwt, Token JWT decodificado que contiene los claims del usuario
     * @return Collection&lt;GrantedAuthority&gt; Lista de autoridades para Spring Security
     * @author Jorge Armando Julio Cruz <jjulio@heinsohn.com.co>
     */
    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        String role = jwt.getClaimAsString("role");
        if (role != null && !role.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }

        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) {
            permissions.stream()
                    .filter(Objects::nonNull).filter(p -> !p.isBlank()).map(SimpleGrantedAuthority::new).forEach(authorities::add);
        }
        return authorities;
    }

    /**
     * Método encargado de construir una respuesta de error estructurada para problemas de seguridad.
     * Crea una respuesta HTTP estándar usando RFC 7807 (Problem Details for HTTP APIs)
     * para errores de autenticación y autorización. Esto proporciona información
     * consistente y estructurada sobre los errores de seguridad.
     * <br>
     * <b>HU03:</b>Agregar Autenticación al sistema
     *
     * @param exchange, Intercambio web del servidor reactivo
     * @param status, código de estado HTTP (401 Unauthorized, 403 Forbidden, etc.)
     * @param title, ítulo descriptivo del error
     * @param detail, descripción detallada del problema de seguridad
     * @return Mono&lt;Void&gt; Respuesta reactiva con el error estructurado
     * @author Jorge Armando Julio Cruz
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, HttpStatus status, String title, String detail) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        var problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setInstance(URI.create(request.getPath().value()));

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(problemDetail))
                .flatMap(bytes -> {
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Mono.just(buffer));
                });
    }
}
