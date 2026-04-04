package TuskSyS.bienestar_api.modules.breaks.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakHistoryResponse {
    private Long id; 
    private String title; 
    private String categoryName; 
    private int durationSeconds; 
    private LocalDateTime completedAt; 
    private int coinReward; // 👇 ¡NUEVO! Para mostrar las monedas en el frontend
}