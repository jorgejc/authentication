package co.com.bancolombia.api.dto.response;

public record AuthenticateResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthenticatedUserSummary user
) {
}
