package TuskSyS.bienestar_api.modules.breaks.controllers;

import TuskSyS.bienestar_api.modules.breaks.dtos.BreakHistoryResponse;
import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import TuskSyS.bienestar_api.modules.breaks.services.BreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    
    // 1. OBTENER PAUSAS (AHORA FILTRADAS POR EMPRESA MEDIANTE userId)
    @GetMapping("/{userId}")
    public ResponseEntity<List<ActiveBreak>> getAvailableBreaks(@PathVariable UUID userId) {
        return ResponseEntity.ok(breakService.getAvailableBreaks(userId));
    }

    // 2. CREAR PAUSA (AHORA REQUIERE EL creatorId PARA SABER A QUÉ EMPRESA ASIGNARLA)
    @PostMapping("/{creatorId}")
    public ResponseEntity<ActiveBreak> createBreak(
            @PathVariable UUID creatorId,
            @RequestBody ActiveBreak activeBreak, 
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(breakService.createBreak(activeBreak, categoryId, creatorId));
    }

    @PostMapping("/{breakId}/complete/{userId}")
    public ResponseEntity<?> completeBreak(@PathVariable Long breakId, @PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(breakService.completeBreak(userId, breakId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable UUID userId) {
        long totalBreaks = breakService.getCompletedBreaksCount(userId);
        long streak = breakService.calculateStreak(userId); 
        
        return ResponseEntity.ok().body(java.util.Map.of(
            "pausasCompletadas", totalBreaks,
            "rachaDias", streak 
        ));
    }

    @org.springframework.beans.factory.annotation.Autowired
    private TuskSyS.bienestar_api.modules.users.repositories.UserRepository userRepository;

    @GetMapping("/spy-users")
    public ResponseEntity<?> spyUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<BreakHistoryResponse>> getUserHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(breakService.getUserHistory(userId));
    }
}