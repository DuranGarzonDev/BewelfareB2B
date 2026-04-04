package TuskSyS.bienestar_api.modules.users.controllers;

import TuskSyS.bienestar_api.modules.users.dtos.AuthResponse;
import TuskSyS.bienestar_api.modules.users.dtos.GoogleLoginRequest; // <-- Importante
import TuskSyS.bienestar_api.modules.users.dtos.LoginRequest;
import TuskSyS.bienestar_api.modules.users.dtos.RegisterRequest;
import TuskSyS.bienestar_api.modules.users.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // La ruta base para este controlador
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 👇 NUEVA RUTA EXCLUSIVA PARA GOOGLE 👇
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            return ResponseEntity.ok(authService.googleLogin(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}