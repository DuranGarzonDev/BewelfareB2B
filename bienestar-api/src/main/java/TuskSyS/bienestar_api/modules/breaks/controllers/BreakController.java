package TuskSyS.bienestar_api.modules.breaks.controllers;

import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import TuskSyS.bienestar_api.modules.breaks.services.BreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breaks")
@RequiredArgsConstructor
public class BreakController {

    private final BreakService breakService;

    // === RUTAS PARA CATEGORÍAS ===
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(breakService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(breakService.createCategory(category));
    }

    // === RUTAS PARA PAUSAS ACTIVAS ===
    @GetMapping
    public ResponseEntity<List<ActiveBreak>> getAllBreaks() {
        return ResponseEntity.ok(breakService.getAllBreaks());
    }

    @PostMapping
    public ResponseEntity<ActiveBreak> createBreak(
            @RequestBody ActiveBreak activeBreak, 
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(breakService.createBreak(activeBreak, categoryId));
    }
}