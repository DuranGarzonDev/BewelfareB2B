package TuskSyS.bienestar_api.modules.users.controllers;

import TuskSyS.bienestar_api.modules.companies.dtos.CompanyStatsDTO;
import TuskSyS.bienestar_api.modules.users.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==========================================
    // 📊 ENDPOINT: DASHBOARD DEL ADMINISTRADOR
    // ==========================================
    @GetMapping("/{adminId}/company-stats")
    public ResponseEntity<CompanyStatsDTO> getCompanyStats(@PathVariable UUID adminId) {
        try {
            return ResponseEntity.ok(adminService.getCompanyStats(adminId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}