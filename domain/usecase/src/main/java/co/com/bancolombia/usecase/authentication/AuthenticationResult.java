package co.com.bancolombia.usecase.authentication;

import co.com.bancolombia.model.authenticateuser.AuthenticateUser;

public record AuthenticationResult(
        String accessToken,
        long expiresIn,
        AuthenticateUser user
) {}
