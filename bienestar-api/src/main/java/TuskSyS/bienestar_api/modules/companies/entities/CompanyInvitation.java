package TuskSyS.bienestar_api.modules.companies.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String inviteeEmail; // El correo de la persona invitada

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, REJECTED

    private LocalDateTime invitedAt;
}