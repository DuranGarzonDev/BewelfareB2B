package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.modules.users.dtos.UserProfileRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TuskSyS.bienestar_api.modules.companies.repositories.CompanyInvitationRepository invitationRepository;

    public User getUserProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User updateProfile(UUID userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        
        return userRepository.save(user);
    }

    // 👇 NUEVO: MOTOR DE NOTIFICACIONES INTELIGENTES DE RACHA 👇
    public Map<String, Object> getStreakStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        java.time.LocalDate today = java.time.LocalDate.now();
        boolean alreadyDidBreakToday = today.equals(user.getLastBreakDate());
        int currentStreak = user.getCurrentStreak() != null ? user.getCurrentStreak() : 0;

        String message;
        String type; // Para que Angular sepa qué color de alerta poner

        if (alreadyDidBreakToday) {
            message = "¡Misión cumplida por hoy! Racha asegurada 🔥";
            type = "SUCCESS";
        } else if (currentStreak > 0) {
            message = "¡Peligro! Tu racha de " + currentStreak + " días expira pronto. ¡Haz una pausa! ⚠️";
            type = "WARNING";
        } else {
            message = "Aún no tienes una racha activa. ¡Haz tu primera pausa y comienza hoy! 🚀";
            type = "INFO";
        }

        return Map.of(
            "message", message,
            "type", type,
            "currentStreak", currentStreak,
            "streakAtRisk", (!alreadyDidBreakToday && currentStreak > 0)
        );
    }

    // ==========================================
    // 📩 VER INVITACIONES PENDIENTES DEL USUARIO
    // ==========================================
    public List<TuskSyS.bienestar_api.modules.companies.dtos.InvitationResponseDTO> getPendingInvitations(String email) {
        return invitationRepository.findByInviteeEmailAndStatus(email, "PENDING").stream()
                .map(inv -> TuskSyS.bienestar_api.modules.companies.dtos.InvitationResponseDTO.builder()
                        .id(inv.getId())
                        .companyName(inv.getCompany().getName())
                        .invitedAt(inv.getInvitedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    // ==========================================
    // ✅ RESPONDER A LA INVITACIÓN (Aceptar/Rechazar)
    // ==========================================
    public String respondToInvitation(UUID userId, UUID invitationId, boolean accept) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        TuskSyS.bienestar_api.modules.companies.entities.CompanyInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        // Validar que la invitación sea realmente para este correo
        if (!invitation.getInviteeEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Esta invitación no te pertenece.");
        }

        if (accept) {
            invitation.setStatus("ACCEPTED");
            user.setCompany(invitation.getCompany());
            userRepository.save(user); // Actualizamos la empresa del usuario
        } else {
            invitation.setStatus("REJECTED");
        }
        
        invitationRepository.save(invitation);

        return accept ? "¡Bienvenido a " + invitation.getCompany().getName() + "!" : "Invitación rechazada.";
    }
}