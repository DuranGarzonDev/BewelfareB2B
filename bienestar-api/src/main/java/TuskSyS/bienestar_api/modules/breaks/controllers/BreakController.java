package TuskSyS.bienestar_api.modules.breaks.controllers;

import TuskSyS.bienestar_api.modules.breaks.dtos.BreakHistoryResponse;
import TuskSyS.bienestar_api.modules.breaks.entities.ActiveBreak;
import TuskSyS.bienestar_api.modules.breaks.entities.Category;
import TuskSyS.bienestar_api.modules.breaks.services.BreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/breaks")
@RequiredArgsConstructor
public class BreakController {

    private final BreakService breakService;

    // === RUTAS PARA CATEGORÍAS ===
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(breakService.getCategories()); // Alineado con el servicio
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(breakService.createCategory(category));
    }

    // === RUTAS PARA PAUSAS ACTIVAS ===
    @GetMapping
    public ResponseEntity<List<ActiveBreak>> getAllBreaks(@RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(breakService.getAllBreaks(userId)); // Se le pasa el UUID
    }

    @PostMapping
    public ResponseEntity<ActiveBreak> createBreak(
            @RequestBody Map<String, Object> payload, // Alineado con Map para aceptar todo el JSON
            @RequestParam Long categoryId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(breakService.createBreak(payload, categoryId, userId));
    }

    @PostMapping("/{breakId}/complete/{userId}")
    public ResponseEntity<?> completeBreak(@PathVariable Long breakId, @PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(breakService.completeBreak(userId, breakId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === RUTAS DE ESTADÍSTICAS E HISTORIAL ===
    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable UUID userId) {
        // Se llama directo al mapa combinado del servicio
        return ResponseEntity.ok(breakService.getUserStats(userId));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<BreakHistoryResponse>> getUserHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(breakService.getUserHistory(userId));
    }

    // === RUTA ESPÍA PARA VER LOS UUID DE LOS USUARIOS ===
    @org.springframework.beans.factory.annotation.Autowired
    private TuskSyS.bienestar_api.modules.users.repositories.UserRepository userRepository;

    @GetMapping("/spy-users")
    public ResponseEntity<?> spyUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}