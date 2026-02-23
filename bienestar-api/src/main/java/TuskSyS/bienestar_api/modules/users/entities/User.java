package TuskSyS.bienestar_api.modules.users.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "users") 
public class User implements UserDetails { // <-- ¡Nuevo contrato de seguridad!

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; 

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role; 

    @Builder.Default
    private boolean isActive = true; 

    // ==========================================================
    // MÉTODOS OBLIGATORIOS DE SPRING SECURITY (USER DETAILS)
    // ==========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Le pasamos el rol del usuario a Spring Security
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        // Para nuestro sistema, el "nombre de usuario" es el correo
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}