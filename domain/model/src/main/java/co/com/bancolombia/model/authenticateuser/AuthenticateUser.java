package co.com.bancolombia.model.authenticateuser;
import lombok.*;
//import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthenticateUser {
    private String id;
    private String email;
    private String encodedPassword;
    private String role;
    private List<String> permissions;
}
