package TuskSyS.bienestar_api.modules.users.repositories;

import TuskSyS.bienestar_api.modules.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Spring Data es tan inteligente que si nombramos el método así, 
    // él automáticamente crea la consulta SQL para buscar por email:
    // "SELECT * FROM users WHERE email = ?"
    Optional<User> findByEmail(String email);
    
    // Para validar rápidamente si un correo ya está registrado
    boolean existsByEmail(String email);
}