package TuskSyS.bienestar_api.modules.users.controllers;

import TuskSyS.bienestar_api.modules.users.dtos.UserProfileRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Endpoint para cargar los datos cuando entras a la pantalla
    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    // Endpoint para guardar los cambios
    @PutMapping("/profile/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable UUID userId, @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }
}