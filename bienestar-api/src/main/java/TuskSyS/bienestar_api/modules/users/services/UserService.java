package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.modules.users.dtos.UserProfileRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 1. Obtener el perfil actual
    public User getUserProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // 2. Actualizar el perfil
    public User updateProfile(UUID userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Actualizamos los datos (Ojo: El email es delicado de cambiar si es el de login, 
        // pero lo permitiremos por ahora según tu requerimiento)
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        
        return userRepository.save(user);
    }
}