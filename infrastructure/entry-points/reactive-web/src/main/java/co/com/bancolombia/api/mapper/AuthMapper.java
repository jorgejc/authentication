package co.com.bancolombia.api.mapper;


import co.com.bancolombia.api.dto.response.AuthenticateResponse;
import co.com.bancolombia.api.dto.response.AuthenticatedUserSummary;
import co.com.bancolombia.usecase.authentication.AuthenticationResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn",  source = "expiresIn")
    @Mapping(target = "user", source = "user")
    AuthenticateResponse toAuthenticateResponse(AuthenticationResult result);

    AuthenticatedUserSummary toUserSummary(co.com.bancolombia.model.authenticateuser.AuthenticateUser user);
}
