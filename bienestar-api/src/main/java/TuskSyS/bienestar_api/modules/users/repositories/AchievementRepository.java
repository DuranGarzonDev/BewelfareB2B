package TuskSyS.bienestar_api.modules.users.repositories;

import TuskSyS.bienestar_api.modules.users.entities.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}