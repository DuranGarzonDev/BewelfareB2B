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
}