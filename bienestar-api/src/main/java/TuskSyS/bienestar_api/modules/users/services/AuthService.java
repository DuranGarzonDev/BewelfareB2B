package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.config.JwtService;
import TuskSyS.bienestar_api.modules.users.dtos.AuthResponse;
import TuskSyS.bienestar_api.modules.users.dtos.LoginRequest;
import TuskSyS.bienestar_api.modules.users.dtos.RegisterRequest;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.UserRepository;
import TuskSyS.bienestar_api.modules.companies.entities.Company;
import TuskSyS.bienestar_api.modules.companies.repositories.CompanyRepository;
import jakarta.annotation.PostConstruct;
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
    private final CompanyRepository companyRepository;

    // ==========================================
    // TRUCO DE ARQUITECTO: SEMBRADOR AUTOMÁTICO
    // ==========================================
    // Como no tienes acceso a la BD por ahora, este método se ejecuta solo 
    // al arrancar el servidor y crea la UFPSO si no la encuentra.
    @PostConstruct
    public void seedInitialCompany() {
        if (companyRepository.findByEmailDomain("ufpso.edu.co").isEmpty()) {
            Company ufpso = Company.builder()
                    .name("Universidad Francisco de Paula Santander Ocaña")
                    .emailDomain("ufpso.edu.co")
                    .build();
            companyRepository.save(ufpso);
            System.out.println("✅ Empresa UFPSO creada automáticamente en PostgreSQL.");
        }
    }

    // ==========================================
    // TRUCO DE ARQUITECTO: ASCENDER A ADMIN
    // ==========================================
    
    @PostConstruct
    public void promoteTestUserToAdmin() {
        userRepository.findByEmail("admin@ufpso.edu.co").ifPresent(user -> {
            if (!"ADMIN".equals(user.getRole())) {
                user.setRole("ADMIN");
                userRepository.save(user);
                System.out.println("👑 ¡ÉXITO! El usuario jddurang@ufpso.edu.co ha sido promovido a ADMIN.");
            }
        });
    }

    // ==========================================
    // MÉTODO DE REGISTRO CON AUTO-JOIN
    // ==========================================
    public AuthResponse register(RegisterRequest request) {
        // 1. Validar si el correo ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya está en uso"); 
        }

        // 2. Extraemos el dominio del correo (Ej: "jhon@ufpso.edu.co" -> "ufpso.edu.co")
        String email = request.getEmail().toLowerCase();
        String domain = email.substring(email.indexOf("@") + 1);

        // 3. Buscamos si existe una empresa con ese dominio
        Company assignedCompany = companyRepository.findByEmailDomain(domain).orElse(null);

        // 4. Construir el nuevo usuario (Aún sin ID)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) 
                .role("EMPLOYEE") 
                .isActive(true) // Forzamos a que el usuario nazca activo
                .company(assignedCompany) // <-- ¡AQUÍ SE ASIGNA LA EMPRESA!
                .build();

        // 5. Guardar en PostgreSQL (¡Aquí nace el UUID!)
        User savedUser = userRepository.save(user);

        // 6. Generar el JWT real
        String jwtToken = jwtService.generateToken(savedUser.getEmail());

        // 7. Devolver el paquete completo incluyendo el UUID autogenerado
        return AuthResponse.builder()
                .token(jwtToken)
                .role(savedUser.getRole())
                .fullName(savedUser.getFullName())
                .userId(savedUser.getId().toString())
                .build();
    }

    // ==========================================
    // MÉTODO DE LOGIN
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
                    .userId(user.getId().toString())
                    .build();

        } catch (Exception e) {
            // Si algo explota, nos lo dirá en la terminal
            System.err.println("❌ ERROR REAL DE LOGIN: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Fallo en el login: " + e.getMessage());
        }
    }
}