package TuskSyS.bienestar_api.modules.breaks.services;

import TuskSyS.bienestar_api.modules.breaks.dtos.BreakHistoryResponse;
import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import TuskSyS.bienestar_api.modules.breaks.entities.UserBreak;
import TuskSyS.bienestar_api.modules.breaks.repositories.ActiveBreakRepository;
import TuskSyS.bienestar_api.modules.breaks.repositories.CategoryRepository;
import TuskSyS.bienestar_api.modules.breaks.repositories.UserBreakRepository;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import TuskSyS.bienestar_api.modules.users.services.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BreakService {

    private final ActiveBreakRepository activeBreakRepository;
    private final CategoryRepository categoryRepository;
    private final UserBreakRepository userBreakRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService; 

    // ==========================================
    // OBTENER TODAS LAS CATEGORÍAS
    // ==========================================
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    // ==========================================
    // CREAR CATEGORÍA
    // ==========================================
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // ==========================================
    // CREAR PAUSA ACTIVA (Adaptado a Payload)
    // ==========================================
    // ==========================================
    // CREAR PAUSA ACTIVA (Parseo Seguro)
    // ==========================================
    public ActiveBreak createBreak(Map<String, Object> payload, Long categoryId, UUID userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Extraemos los números convirtiéndolos a String primero, es la forma más segura de evitar ClassCastException
        int duration = payload.containsKey("durationSeconds") ? Integer.parseInt(payload.get("durationSeconds").toString()) : 60;
        int coins = payload.containsKey("coinReward") ? Integer.parseInt(payload.get("coinReward").toString()) : 10;

        ActiveBreak newBreak = ActiveBreak.builder()
                .title(String.valueOf(payload.get("title")))
                .description(payload.get("description") != null ? String.valueOf(payload.get("description")) : "")
                .durationSeconds(duration)
                .mediaUrl(payload.get("mediaUrl") != null ? String.valueOf(payload.get("mediaUrl")) : "")
                .coinReward(coins) 
                .category(category)
                .build();

        return activeBreakRepository.save(newBreak);
    }

    // ==========================================
    // OBTENER TODAS LAS PAUSAS
    // ==========================================
    public List<ActiveBreak> getAllBreaks(UUID userId) {
        return activeBreakRepository.findAll();
    }

    // ==========================================
    // OBTENER ESTADÍSTICAS DEL USUARIO
    // ==========================================
    public Map<String, Object> getUserStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        long count = userBreakRepository.countByUserId(userId);
        
        return Map.of(
                "pausasCompletadas", count,
                "rachaDias", user.getCurrentStreak() != null ? user.getCurrentStreak() : 0
        );
    }

    // ==========================================
    // COMPLETAR PAUSA Y APLICAR LOGROS
    // ==========================================
    public UserBreak completeBreak(UUID userId, Long breakId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        ActiveBreak activeBreak = activeBreakRepository.findById(breakId)
                .orElseThrow(() -> new RuntimeException("Pausa no encontrada"));

        UserBreak record = UserBreak.builder()
                .user(user)
                .activeBreak(activeBreak)
                .completedAt(LocalDateTime.now())
                .build();
        userBreakRepository.save(record);

        int reward = activeBreak.getCoinReward() != null ? activeBreak.getCoinReward() : 10;
        user.setCoins((user.getCoins() == null ? 0 : user.getCoins()) + reward);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lastBreak = user.getLastBreakDate();

        if (lastBreak == null || lastBreak.isBefore(today.minusDays(1))) {
            user.setCurrentStreak(1);
        } else if (lastBreak.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        }

        if (user.getCurrentStreak() > (user.getMaxStreak() == null ? 0 : user.getMaxStreak())) {
            user.setMaxStreak(user.getCurrentStreak());
        }

        user.setLastBreakDate(today);
        userRepository.save(user);

        int totalBreaksCount = (int) userBreakRepository.countByUserId(userId);
        achievementService.checkAndGrantAchievements(user, totalBreaksCount);

        return record;
    }

    // ==========================================
    // HISTORIAL DE PAUSAS (Corregido con Builder)
    // ==========================================
    public List<BreakHistoryResponse> getUserHistory(UUID userId) {
        List<UserBreak> allBreaks = userBreakRepository.findAll();
        
        return allBreaks.stream()
                .filter(ub -> ub.getUser().getId().equals(userId))
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt())) // Más recientes primero
                .map(ub -> BreakHistoryResponse.builder()
                        .id(ub.getId())
                        .title(ub.getActiveBreak().getTitle())
                        .categoryName(ub.getActiveBreak().getCategory() != null ? ub.getActiveBreak().getCategory().getName() : "General")
                        .durationSeconds(ub.getActiveBreak().getDurationSeconds())
                        .completedAt(ub.getCompletedAt())
                        .coinReward(ub.getActiveBreak().getCoinReward() != null ? ub.getActiveBreak().getCoinReward() : 10)
                        .build()
                )
                .collect(Collectors.toList());
    }
}