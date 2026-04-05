package TuskSyS.bienestar_api.modules.companies.repositories;

import TuskSyS.bienestar_api.modules.companies.entities.CompanyInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyInvitationRepository extends JpaRepository<CompanyInvitation, UUID> {
    
    // Cambiamos Long por UUID
    List<CompanyInvitation> findByCompanyIdOrderByInvitedAtDesc(UUID companyId);

    // Este se queda igual
    List<CompanyInvitation> findByInviteeEmailAndStatus(String email, String status);

    // Cambiamos Long por UUID
    boolean existsByCompanyIdAndInviteeEmailAndStatus(UUID companyId, String email, String status);
}