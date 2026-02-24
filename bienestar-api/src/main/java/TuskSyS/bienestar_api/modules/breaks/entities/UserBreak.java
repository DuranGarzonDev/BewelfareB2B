package TuskSyS.bienestar_api.modules.breaks.entities;

// Importamos la entidad de Usuario (Verifica que esta ruta coincida con tu proyecto)
import TuskSyS.bienestar_api.modules.users.entities.User; 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_breaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación: Qué usuario hizo la pausa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación: Qué pausa hizo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_break_id", nullable = false)
    private ActiveBreak activeBreak;

    // Cuándo la terminó
    @Column(nullable = false)
    private LocalDateTime completedAt;
}