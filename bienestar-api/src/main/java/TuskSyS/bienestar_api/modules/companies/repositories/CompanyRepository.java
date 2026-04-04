package TuskSyS.bienestar_api.modules.companies.repositories;

import TuskSyS.bienestar_api.modules.companies.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    
    // Spring Boot hace la magia SQL automáticamente con solo nombrar bien el método:
    // SELECT * FROM companies WHERE email_domain = ?
    Optional<Company> findByEmailDomain(String emailDomain);
}