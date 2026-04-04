package TuskSyS.bienestar_api.modules.users.repositories;

import TuskSyS.bienestar_api.modules.users.entities.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    // Para saber qué logros ya tiene el usuario y no dárselos dos veces
    boolean existsByUserIdAndAchievementId(UUID userId, Long achievementId);
    
    // Para que Angular pueda mostrar las medallas del usuario
    List<UserAchievement> findByUserId(UUID userId);
}