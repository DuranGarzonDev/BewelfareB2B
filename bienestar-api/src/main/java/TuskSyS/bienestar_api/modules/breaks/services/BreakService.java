package TuskSyS.bienestar_api.modules.breaks.services;

import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import TuskSyS.bienestar_api.modules.breaks.entities.UserBreak;
import TuskSyS.bienestar_api.modules.breaks.repositories.ActiveBreakRepository;
import TuskSyS.bienestar_api.modules.breaks.repositories.CategoryRepository;
import TuskSyS.bienestar_api.modules.breaks.repositories.UserBreakRepository;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import TuskSyS.bienestar_api.modules.breaks.dtos.BreakHistoryResponse;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BreakService {

    private final CategoryRepository categoryRepository;
    private final ActiveBreakRepository activeBreakRepository;
    private final UserBreakRepository userBreakRepository;
    private final UserRepository userRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // ==========================================
    // 1. OBTENER PAUSAS FILTRADAS POR EMPRESA
    // ==========================================
    public List<ActiveBreak> getAvailableBreaks(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getCompany() != null) {
            // Si tiene empresa, trae las globales + las de su empresa
            return activeBreakRepository.findGlobalAndByCompany(user.getCompany().getId());
        }
        
        // Si es un usuario sin empresa, solo ve las globales
        return activeBreakRepository.findByCompanyIsNull();
    }

    // ==========================================
    // 2. CREAR PAUSA ASIGNADA A LA EMPRESA
    // ==========================================
    public ActiveBreak createBreak(ActiveBreak activeBreak, Long categoryId, UUID creatorId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creador no encontrado"));

        activeBreak.setCategory(category);
        
        // ¡LA MAGIA AQUÍ! La pausa hereda la empresa de quien la creó
        activeBreak.setCompany(creator.getCompany()); 

        return activeBreakRepository.save(activeBreak);
    }

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

        return userBreakRepository.save(record);
    }

    public long getCompletedBreaksCount(UUID userId) {
        return userBreakRepository.countByUserId(userId);
    }

    public long calculateStreak(UUID userId) {
        List<UserBreak> breaks = userBreakRepository.findByUserIdOrderByCompletedAtDesc(userId);
        if (breaks.isEmpty()) return 0;

        List<java.time.LocalDate> uniqueDates = breaks.stream()
                .map(b -> b.getCompletedAt().toLocalDate())
                .distinct()
                .toList();

        long streak = 0;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate targetDate = today;

        if (!uniqueDates.contains(today)) {
            if (uniqueDates.contains(today.minusDays(1))) {
                targetDate = today.minusDays(1); 
            } else {
                return 0; 
            }
        }

        for (java.time.LocalDate date : uniqueDates) {
            if (date.equals(targetDate)) {
                streak++;
                targetDate = targetDate.minusDays(1); 
            } else if (date.isBefore(targetDate)) {
                break; 
            }
        }
        
        return streak;
    }

    public List<BreakHistoryResponse> getUserHistory(UUID userId) {
        List<UserBreak> history = userBreakRepository.findByUserIdOrderByCompletedAtDesc(userId);

        return history.stream().map(record -> BreakHistoryResponse.builder()
                .id(record.getId())
                .title(record.getActiveBreak().getTitle())
                .categoryName(record.getActiveBreak().getCategory().getName())
                .durationSeconds(record.getActiveBreak().getDurationSeconds())
                .completedAt(record.getCompletedAt())
                .build()
        ).toList();
    }
}