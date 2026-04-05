package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.modules.breaks.repositories.UserBreakRepository;
import TuskSyS.bienestar_api.modules.companies.dtos.CompanyStatsDTO;
import TuskSyS.bienestar_api.modules.companies.entities.Company;
import TuskSyS.bienestar_api.modules.companies.entities.CompanyInvitation;
import TuskSyS.bienestar_api.modules.companies.repositories.CompanyInvitationRepository;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserBreakRepository userBreakRepository;
    private final CompanyInvitationRepository companyInvitationRepository; // 👇 INYECTADO PARA INVITACIONES

    // ==========================================
    // 📊 OBTENER ESTADÍSTICAS
    // ==========================================
    public CompanyStatsDTO getCompanyStats(UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

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

    // ==========================================
    // 📩 INVITAR USUARIO A LA EMPRESA
    // ==========================================
    public String inviteUser(UUID adminId, String targetEmail) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // Validamos que el que invita sea un ADMIN
        if (!"ADMIN".equals(admin.getRole()) && !"SUPERADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Solo los administradores pueden enviar invitaciones.");
        }

        Company company = admin.getCompany();
        if (company == null) {
            throw new RuntimeException("No tienes una empresa asignada para invitar usuarios.");
        }

        String email = targetEmail.toLowerCase().trim();

        // 1. Verificar si el usuario YA está en esta empresa
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && company.equals(existingUser.get().getCompany())) {
            throw new RuntimeException("Este usuario ya pertenece a tu empresa.");
        }

        // 2. Verificar si YA hay una invitación PENDIENTE enviada a ese correo por esta empresa
        boolean alreadyInvited = companyInvitationRepository.existsByCompanyIdAndInviteeEmailAndStatus(
                company.getId(), email, "PENDING"
        );
        if (alreadyInvited) {
            throw new RuntimeException("Ya existe una invitación pendiente para este correo.");
        }

        // 3. Crear el ticket de invitación
        CompanyInvitation invitation = CompanyInvitation.builder()
                .company(company)
                .inviteeEmail(email)
                .status("PENDING")
                .invitedAt(LocalDateTime.now())
                .build();

        companyInvitationRepository.save(invitation);

        return "Invitación enviada con éxito a " + email;
    }
}