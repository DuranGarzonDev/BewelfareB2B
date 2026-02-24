package TuskSyS.bienestar_api.modules.breaks.repositories;

import TuskSyS.bienestar_api.modules.breaks.entities.UserBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID; // <-- 1. IMPORTAMOS UUID

public interface UserBreakRepository extends JpaRepository<UserBreak, Long> {
    
    // 2. Cambiamos Long por UUID aquí
    long countByUserId(UUID userId);
}