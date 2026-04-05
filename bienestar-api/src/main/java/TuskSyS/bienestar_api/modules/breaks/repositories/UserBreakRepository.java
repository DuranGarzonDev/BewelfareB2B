package TuskSyS.bienestar_api.modules.breaks.repositories;

import TuskSyS.bienestar_api.modules.breaks.entities.UserBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Importamos List
import java.util.UUID;

public interface UserBreakRepository extends JpaRepository<UserBreak, Long> {
    
    long countByUserId(UUID userId);

    // NUEVO MÉTODO MÁGICO: Trae el historial de un usuario ordenado por fecha descendente
    List<UserBreak> findByUserIdOrderByCompletedAtDesc(UUID userId);

    // ==========================================
    // 📊 ESTADÍSTICAS B2B: Contar pausas de toda una empresa
    // ==========================================
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ub) FROM UserBreak ub WHERE ub.user.company = :company")
    long countByCompany(@org.springframework.data.repository.query.Param("company") TuskSyS.bienestar_api.modules.companies.entities.Company company);
}