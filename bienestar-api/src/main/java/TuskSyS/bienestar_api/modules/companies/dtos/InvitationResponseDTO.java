package TuskSyS.bienestar_api.modules.companies.dtos;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvitationResponseDTO {
    private UUID id;
    private String companyName;
    private LocalDateTime invitedAt;
}