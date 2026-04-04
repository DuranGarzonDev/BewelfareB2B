package TuskSyS.bienestar_api.modules.users.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // Ej: "Leyenda Viva"
    private String description; // Ej: "Llega a 50 días de racha"
    private Integer rewardCoins;// Ej: 500
    private String type;        // STREAK, TOTAL_BREAKS, COINS_COLLECTED
    private Integer threshold;  // El valor a alcanzar (50)
    private String badgeIcon;   // Icono o URL de la medalla
}