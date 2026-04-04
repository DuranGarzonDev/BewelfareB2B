package TuskSyS.bienestar_api.modules.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardDTO {
    private String fullName;
    private Integer coins;
    private Integer currentStreak;
    private String profilePictureUrl;
    private String rankName; // Ej: "Novato", "Bronce", "Plata", "Oro", "Leyenda"
}