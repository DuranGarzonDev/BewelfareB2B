package TuskSyS.bienestar_api.modules.users.dtos;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String fullName;
    private String email;
    private String bio;
    private String profilePictureUrl;
}