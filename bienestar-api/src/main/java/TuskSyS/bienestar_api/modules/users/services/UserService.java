package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.modules.users.dtos.UserProfileRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}