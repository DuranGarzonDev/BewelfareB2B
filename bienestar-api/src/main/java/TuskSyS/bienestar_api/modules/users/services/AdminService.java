package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.modules.breaks.repositories.UserBreakRepository;
import TuskSyS.bienestar_api.modules.companies.dtos.CompanyStatsDTO;
import TuskSyS.bienestar_api.modules.companies.entities.Company;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserBreakRepository userBreakRepository;

    // ==========================================
    // 📊 OBTENER ESTADÍSTICAS (Soporta Admin y SuperAdmin)
    // ==========================================
    public CompanyStatsDTO getCompanyStats(UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // 👑 LÓGICA PARA EL SUPERADMIN (TheOddProgrammer)
        if ("SUPERADMIN".equals(admin.getRole())) {
            long totalEmployees = userRepository.count();
            long totalBreaks = userBreakRepository.count();
            long totalCoins = userRepository.findAll().stream()
                    .mapToLong(u -> u.getCoins() != null ? u.getCoins() : 0)
                    .sum();

            return CompanyStatsDTO.builder()
                    .companyName("Control Global (Todas las Empresas)")
                    .totalEmployees(totalEmployees)
                    .totalBreaksCompleted(totalBreaks)
                    .totalCoinsEarned(totalCoins)
                    .build();
        } 
        
        // 🏢 LÓGICA PARA EL ADMIN DE EMPRESA (Ej: UFPSO)
        else if ("ADMIN".equals(admin.getRole())) {
            Company company = admin.getCompany();
            if (company == null) {
                throw new RuntimeException("Este administrador no tiene una empresa asignada");
            }

            long totalEmployees = userRepository.countByCompany(company);
            long totalBreaks = userBreakRepository.countByCompany(company);
            
            List<User> employees = userRepository.findByCompany(company);
            long totalCoins = employees.stream()
                    .mapToLong(u -> u.getCoins() != null ? u.getCoins() : 0)
                    .sum();

            return CompanyStatsDTO.builder()
                    .companyName(company.getName())
                    .totalEmployees(totalEmployees)
                    .totalBreaksCompleted(totalBreaks)
                    .totalCoinsEarned(totalCoins)
                    .build();
        }

        throw new RuntimeException("Acceso denegado: Rol no autorizado");
    }
}