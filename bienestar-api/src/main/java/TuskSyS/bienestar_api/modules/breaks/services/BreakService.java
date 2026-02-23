package TuskSyS.bienestar_api.modules.breaks.services;

import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import TuskSyS.bienestar_api.modules.breaks.repositories.ActiveBreakRepository;
import TuskSyS.bienestar_api.modules.breaks.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BreakService {

    private final CategoryRepository categoryRepository;
    private final ActiveBreakRepository activeBreakRepository;

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
}