package TuskSyS.bienestar_api.modules.companies.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    // Este campo es CLAVE para la Fase 1. Ej: "ufpso.edu.co"
    // Si un usuario se registra con este dominio, entra directo a esta empresa.
    @Column(unique = true)
    private String emailDomain; 

    @Column(length = 1000)
    private String logoUrl; // Para personalizar el dashboard por empresa a futuro

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}