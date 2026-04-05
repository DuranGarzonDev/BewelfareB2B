package TuskSyS.bienestar_api.modules.companies.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyStatsDTO {
    private String companyName;
    private long totalEmployees;
    private long totalBreaksCompleted;
    private long totalCoinsEarned;
}