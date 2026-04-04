package TuskSyS.bienestar_api.modules.breaks.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "active_breaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Ej: "Estiramiento de cuello"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer durationSeconds; // Cuánto dura en segundos

    private String mediaUrl; // Enlace a un video o imagen

    // Relación: Muchas pausas pertenecen a una sola categoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 👇 NUEVA RELACIÓN MULTI-TENANT 👇
    // Si company es NULL, la pausa es GLOBAL (la ven todos).
    // Si company tiene un ID, la pausa es PRIVADA (solo la ve esa empresa).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private TuskSyS.bienestar_api.modules.companies.entities.Company company;
}