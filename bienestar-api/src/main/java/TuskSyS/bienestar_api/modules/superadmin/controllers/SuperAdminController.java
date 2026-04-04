package TuskSyS.bienestar_api.modules.superadmin.controllers;

import TuskSyS.bienestar_api.modules.companies.entities.Company;
import TuskSyS.bienestar_api.modules.companies.repositories.CompanyRepository;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    // === GESTIÓN DE EMPRESAS ===

    @GetMapping("/companies")
    public ResponseEntity<?> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @PostMapping("/companies")
    public ResponseEntity<?> createCompany(@RequestBody Company company) {
        // Guardamos la nueva empresa (Ej: Ecopetrol, Bancolombia)
        return ResponseEntity.ok(companyRepository.save(company));
    }

    // === GESTIÓN DE USUARIOS ===

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // El SuperAdmin lo ve TODO
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable UUID userId, @RequestParam String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Ascendemos o descendemos al usuario (EMPLOYEE, ADMIN, SUPERADMIN)
        user.setRole(role.toUpperCase());
        return ResponseEntity.ok(userRepository.save(user));
    }
}