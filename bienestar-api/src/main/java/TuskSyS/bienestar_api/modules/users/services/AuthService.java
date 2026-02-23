package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.config.JwtService;
import TuskSyS.bienestar_api.modules.users.dtos.AuthResponse;
import TuskSyS.bienestar_api.modules.users.dtos.LoginRequest;
import TuskSyS.bienestar_api.modules.users.dtos.RegisterRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // <-- El Gerente de Login

    public AuthResponse register(RegisterRequest request) {
        // 1. Validar si el correo ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya está en uso"); 
        }

        // 2. Construir el nuevo usuario
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) 
                .role("EMPLOYEE") 
                .isActive(true) // Forzamos a que el usuario nazca activo
                .build();

        // 3. Guardar en PostgreSQL
        userRepository.save(user);

        // 4. Generar el JWT real
        String jwtToken = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .role(user.getRole())
                .fullName(user.getFullName())
                .build();
    }

    // ==========================================
    // MÉTODO DE LOGIN (¡Ahora sí existe!)
    // ==========================================
    public AuthResponse login(LoginRequest request) {
        try {
            // 1. Spring Security hace el trabajo de validar
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. Si pasa, buscamos al usuario en la BD y generamos su token
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            String jwtToken = jwtService.generateToken(user.getEmail());

            // 3. Devolvemos la "tarjeta de acceso"
            return AuthResponse.builder()
                    .token(jwtToken)
                    .role(user.getRole())
                    .fullName(user.getFullName())
                    .build();

        } catch (Exception e) {
            // Si algo explota, nos lo dirá en la terminal
            System.err.println("❌ ERROR REAL DE LOGIN: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Fallo en el login: " + e.getMessage());
        }
    }
}