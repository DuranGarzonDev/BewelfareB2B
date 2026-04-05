package TuskSyS.bienestar_api.modules.users.repositories;

import TuskSyS.bienestar_api.modules.companies.entities.Company;
import TuskSyS.bienestar_api.modules.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // ==========================================
    // 🏆 MOTOR DE GAMIFICACIÓN: TOP 10 (AISLADO POR EMPRESA)
    // ==========================================
    List<User> findTop10ByCompanyOrderByCoinsDesc(Company company);
    
    List<User> findTop10ByCompanyIsNullOrderByCoinsDesc();

    // ==========================================
    // 📊 ESTADÍSTICAS B2B
    // ==========================================
    long countByCompany(TuskSyS.bienestar_api.modules.companies.entities.Company company);
    List<User> findByCompany(TuskSyS.bienestar_api.modules.companies.entities.Company company);
}