package co.com.bancolombia.api.dto.response;

import java.util.List;

public record AuthenticatedUserSummary(
        String id,
        String email,
        String role,
        List<String> permissions
) {
}
