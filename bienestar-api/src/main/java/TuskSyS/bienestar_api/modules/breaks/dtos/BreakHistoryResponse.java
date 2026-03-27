package TuskSyS.bienestar_api.modules.breaks.dtos;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BreakHistoryResponse {
    private Long id; // El ID del registro
    private String title; // "Regla 20-20-20"
    private String categoryName; // "Visual"
    private int durationSeconds; // 20
    private LocalDateTime completedAt; // La fecha y hora exacta
}