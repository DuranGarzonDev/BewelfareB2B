package TuskSyS.bienestar_api.modules.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token; // El famoso JWT que usaremos después
    private String role;
    private String fullName;
    private String userId;
}