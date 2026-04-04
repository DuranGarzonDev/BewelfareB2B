package TuskSyS.bienestar_api.modules.users.services;

import TuskSyS.bienestar_api.config.JwtService;
import TuskSyS.bienestar_api.modules.users.dtos.AuthResponse;
import TuskSyS.bienestar_api.modules.users.dtos.LoginRequest;
import TuskSyS.bienestar_api.modules.users.dtos.RegisterRequest;
import TuskSyS.bienestar_api.modules.users.entities.Achievement;
import TuskSyS.bienestar_api.modules.users.entities.User;
import TuskSyS.bienestar_api.modules.users.repositories.AchievementRepository;
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
    private final AuthenticationManager authenticationManager; 
    private final CompanyRepository companyRepository;
    private final AchievementRepository achievementRepository; // 👇 INYECTADO PARA LOS LOGROS

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

    // 👇 NUEVO: SEMBRADOR DE LOGROS 👇
    @PostConstruct
    public void seedAchievements() {
        if (achievementRepository.count() == 0) {
            achievementRepository.save(Achievement.builder()
                .title("Primer Paso")
                .description("Completa tu primera pausa activa")
                .rewardCoins(50)
                .type("TOTAL_BREAKS")
                .threshold(1)
                .build());

            achievementRepository.save(Achievement.builder()
                .title("Constancia de Hierro")
                .description("Llega a una racha de 7 días")
                .rewardCoins(100)
                .type("STREAK")
                .threshold(7)
                .build());

            achievementRepository.save(Achievement.builder()
                .title("Leyenda UFPSO")
                .description("Llega a una racha de 50 días")
                .rewardCoins(500)
                .type("STREAK")
                .threshold(50)
                .build());
            
            System.out.println("🏆 Logros iniciales sembrados en la base de datos.");
        }
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya está en uso"); 
        }

        String email = request.getEmail().toLowerCase();
        String domain = email.substring(email.indexOf("@") + 1);

        Company assignedCompany = companyRepository.findByEmailDomain(domain).orElse(null);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) 
                .role("EMPLOYEE") 
                .isActive(true) 
                .company(assignedCompany) 
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .role(savedUser.getRole())
                .fullName(savedUser.getFullName())
                .userId(savedUser.getId().toString())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            String jwtToken = jwtService.generateToken(user.getEmail());

            return AuthResponse.builder()
                    .token(jwtToken)
                    .role(user.getRole())
                    .fullName(user.getFullName())
                    .userId(user.getId().toString())
                    .build();

        } catch (Exception e) {
            System.err.println("❌ ERROR REAL DE LOGIN: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Fallo en el login: " + e.getMessage());
        }
    }

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String googleUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getToken();
        
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

        String email = ((String) payload.get("email")).toLowerCase();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setProfilePictureUrl(picture);
            user = userRepository.save(user);
        } else {
            String domain = email.substring(email.indexOf("@") + 1);
            Company assignedCompany = companyRepository.findByEmailDomain(domain).orElse(null);

            user = User.builder()
                    .fullName(name)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role("EMPLOYEE")
                    .isActive(true)
                    .company(assignedCompany)
                    .profilePictureUrl(picture)
                    .build();

            user = userRepository.save(user);
            System.out.println("✅ Nuevo usuario registrado vía Google: " + email);
        }

        String jwtToken = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .role(user.getRole())
                .fullName(user.getFullName())
                .userId(user.getId().toString())
                .build();
    }
}