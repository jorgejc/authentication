package co.com.bancolombia.api;

import co.com.bancolombia.api.auth.AuthenticationHandler;
import co.com.bancolombia.api.dto.request.CreateUserRecord;
import co.com.bancolombia.api.dto.response.UserRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/users",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "saveUseCase",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "register user",
                            description = "register new user",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = CreateUserRecord.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "create user",
                                            content = @Content(schema = @Schema(implementation = UserRecordResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error validation"),
                                    @ApiResponse(responseCode = "409", description = "Conflict (duplicate)")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/login",
                    method = RequestMethod.POST,
                    beanClass = co.com.bancolombia.api.auth.AuthenticationHandler.class,
                    beanMethod = "authenticate",
                    operation = @Operation(
                            operationId = "login",
                            summary = "Authentication",
                            description = "Receives the user's credentials, validates them, and generates a JWT token if authentication is successful.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = co.com.bancolombia.api.dto.request.AuthenticateRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Auth OK",
                                            content = @Content(schema = @Schema(implementation = co.com.bancolombia.api.dto.response.AuthenticateResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid petition"),
                                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                                    @ApiResponse(responseCode = "503", description = "External service unavailable")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler, AuthenticationHandler authenticationHandler) {
        return route()
                .POST("/api/v1/users",handler::saveUseCase)
                .GET("/api/v1/users/email/{email}/exists", handler::existsByEmailUseCase)
                .POST("/api/v1/login", authenticationHandler::authenticate)
                .build();

    }
}
