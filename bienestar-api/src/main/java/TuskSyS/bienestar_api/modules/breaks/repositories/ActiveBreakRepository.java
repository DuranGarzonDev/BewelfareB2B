package TuskSyS.bienestar_api.modules.breaks.repositories;

import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActiveBreakRepository extends JpaRepository<ActiveBreak, Long> {
    // Método para buscar pausas filtradas por una categoría específica
    List<ActiveBreak> findByCategoryId(Long categoryId);
}