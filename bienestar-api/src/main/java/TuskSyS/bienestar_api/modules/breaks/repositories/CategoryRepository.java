package TuskSyS.bienestar_api.modules.breaks.repositories;

import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}