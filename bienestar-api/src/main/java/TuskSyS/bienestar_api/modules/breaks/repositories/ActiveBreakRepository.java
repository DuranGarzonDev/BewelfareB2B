package TuskSyS.bienestar_api.modules.breaks.repositories;

import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActiveBreakRepository extends JpaRepository<ActiveBreak, Long> {
    
    // Método para buscar pausas filtradas por una categoría específica
    List<ActiveBreak> findByCategoryId(Long categoryId);

    // 1. Trae las pausas globales (company_id es nulo) Y las de la empresa del usuario
    @Query("SELECT a FROM ActiveBreak a WHERE a.company IS NULL OR a.company.id = :companyId")
    List<ActiveBreak> findGlobalAndByCompany(@Param("companyId") UUID companyId);

    // 2. Trae SOLO las globales (Para usuarios que aún no tienen empresa)
    List<ActiveBreak> findByCompanyIsNull();
}