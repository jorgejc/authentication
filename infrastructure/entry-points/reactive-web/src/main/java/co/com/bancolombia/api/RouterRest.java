package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.CreateUserRecord;
import co.com.bancolombia.api.dto.response.UserRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
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
    )
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route()
                .POST("/api/v1/users"),handler::saveUseCase)
                .GET("/api/v1/users/email/{email}/exists", handler::existsByEmailUseCase).build();

    }
}
