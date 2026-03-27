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

    public List<ActiveBreak> getAllBreaks() {
        return activeBreakRepository.findAll();
    }

    public ActiveBreak createBreak(ActiveBreak activeBreak, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        activeBreak.setCategory(category);
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

    // 3. Cambiamos Long por UUID aquí también
    public long getCompletedBreaksCount(UUID userId) {
        return userBreakRepository.countByUserId(userId);
    }

    // === NUEVO MÉTODO: CALCULAR LA RACHA DE DÍAS CONSECUTIVOS ===
    public long calculateStreak(UUID userId) {
        // 1. Traemos todo el historial de pausas del usuario
        List<UserBreak> breaks = userBreakRepository.findByUserIdOrderByCompletedAtDesc(userId);
        if (breaks.isEmpty()) return 0;

        // 2. Extraemos solo las fechas (sin la hora) y quitamos los días repetidos
        List<java.time.LocalDate> uniqueDates = breaks.stream()
                .map(b -> b.getCompletedAt().toLocalDate())
                .distinct()
                .toList();

        long streak = 0;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate targetDate = today;

        // 3. Verificamos si la racha sigue viva (hizo pausa hoy o ayer)
        if (!uniqueDates.contains(today)) {
            if (uniqueDates.contains(today.minusDays(1))) {
                targetDate = today.minusDays(1); // La racha está viva, empezamos a contar desde ayer
            } else {
                return 0; // Perdió la racha, pasaron más de 24 horas
            }
        }

        // 4. Contamos los días hacia atrás
        for (java.time.LocalDate date : uniqueDates) {
            if (date.equals(targetDate)) {
                streak++;
                targetDate = targetDate.minusDays(1); // Buscamos el día anterior
            } else if (date.isBefore(targetDate)) {
                break; // Se rompió la secuencia
            }
        }
        
        return streak;
    }

    // === NUEVO MÉTODO: OBTENER HISTORIAL DETALLADO ===
    public List<BreakHistoryResponse> getUserHistory(UUID userId) {
        // 1. Buscamos todas las pausas del usuario ordenadas desde la más reciente
        List<UserBreak> history = userBreakRepository.findByUserIdOrderByCompletedAtDesc(userId);

        // 2. Traducimos cada "UserBreak" (Base de Datos) a un "BreakHistoryResponse" (Frontend)
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