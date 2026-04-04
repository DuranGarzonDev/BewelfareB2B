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
import TuskSyS.bienestar_api.modules.users.dtos.GoogleLoginRequest;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    // TRUCO DE ARQUITECTO: EL NACIMIENTO DEL SUPERADMIN
    // ==========================================
    
    @PostConstruct
    public void promoteTheOddProgrammerToSuperAdmin() {
        userRepository.findByEmail("juangarzonmani@gmail.com").ifPresent(user -> {
            if (!"SUPERADMIN".equals(user.getRole())) {
                user.setRole("SUPERADMIN");
                userRepository.save(user);
                System.out.println("👑 THE ODD PROGRAMMER AL MANDO: juangarzonmani@gmail.com es ahora SUPERADMIN.");
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

    // ==========================================
    // MÉTODO DE LOGIN/REGISTRO CON GOOGLE
    // ==========================================
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        // 1. Validar el token con Google de forma segura usando RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        String googleUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getToken();
        
        // Usamos Map<?, ?> para respetar el Type Safety de Java y quitarnos las advertencias
        Map<?, ?> payload;
        try {
            payload = restTemplate.getForObject(googleUrl, Map.class);
        } catch (Exception e) {
            System.err.println("❌ ERROR: Token de Google inválido o caducado.");
            throw new RuntimeException("Token de Google inválido");
        }

        if (payload == null || !payload.containsKey("email")) {
            throw new RuntimeException("No se pudo extraer la información de Google");
        }

        // 2. Extraemos los datos que nos regaló Google (Casteo seguro)
        String email = ((String) payload.get("email")).toLowerCase();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 3. Buscamos si este usuario ya existe en nuestra base de datos
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // ---> ES UN LOGIN <---
            user = userOptional.get();
            // Truco: Actualizamos su foto por si la cambió en Google recientemente
            user.setProfilePictureUrl(picture);
            user = userRepository.save(user);
        } else {
            // ---> ES UN REGISTRO NUEVO (Hacemos el Auto-Join) <---
            String domain = email.substring(email.indexOf("@") + 1);
            Company assignedCompany = companyRepository.findByEmailDomain(domain).orElse(null);

            user = User.builder()
                    .fullName(name)
                    .email(email)
                    // Le ponemos una contraseña aleatoria hiper-segura porque siempre entrará con Google
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role("EMPLOYEE")
                    .isActive(true)
                    .company(assignedCompany)
                    .profilePictureUrl(picture) // Guardamos su foto
                    .build();

            user = userRepository.save(user);
            System.out.println("✅ Nuevo usuario registrado vía Google: " + email);
        }

        // 4. Google ya hizo su trabajo, ahora nosotros generamos NUESTRO propio JWT (El pase VIP)
        String jwtToken = jwtService.generateToken(user.getEmail());

        // 5. Devolvemos la respuesta al Frontend
        return AuthResponse.builder()
                .token(jwtToken)
                .role(user.getRole())
                .fullName(user.getFullName())
                .userId(user.getId().toString())
                .build();
    }
}