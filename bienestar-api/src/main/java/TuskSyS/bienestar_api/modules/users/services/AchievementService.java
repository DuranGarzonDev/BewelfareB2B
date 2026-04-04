package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.modules.users.entities.Achievement;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.entities.UserAchievement;
import TuskSyS.bienestar_api.modules.users.repositories.AchievementRepository;
import TuskSyS.bienestar_api.modules.users.repositories.UserAchievementRepository;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;

    // ==========================================
    // 🏆 EVALUADOR DE LOGROS
    // ==========================================
    public void checkAndGrantAchievements(User user, int totalBreaksCompleted) {
        List<Achievement> allAchievements = achievementRepository.findAll();

        for (Achievement achievement : allAchievements) {
            // 1. Verificamos si ya tiene este logro
            if (userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
                continue; // Si ya lo tiene, saltamos al siguiente
            }

            boolean unlocked = false;

            // 2. Evaluamos la condición según el tipo de logro
            switch (achievement.getType()) {
                case "STREAK":
                    if (user.getCurrentStreak() >= achievement.getThreshold()) unlocked = true;
                    break;
                case "TOTAL_BREAKS":
                    if (totalBreaksCompleted >= achievement.getThreshold()) unlocked = true;
                    break;
                case "COINS_COLLECTED":
                    if (user.getCoins() >= achievement.getThreshold()) unlocked = true;
                    break;
            }

            // 3. ¡Si lo desbloqueó, le damos la recompensa!
            if (unlocked) {
                // Le damos el premio en efectivo (Coins)
                user.setCoins(user.getCoins() + achievement.getRewardCoins());
                
                // Guardamos el diploma
                UserAchievement record = UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .unlockedAt(LocalDateTime.now())
                        .build();
                
                userAchievementRepository.save(record);
                
                System.out.println("🌟 ¡LOGRO DESBLOQUEADO! El usuario " + user.getFullName() + " ganó '" + achievement.getTitle() + "' y " + achievement.getRewardCoins() + " Coins.");
            }
        }
        
        // Guardamos las nuevas monedas del usuario en la BD
        userRepository.save(user);
    }
}