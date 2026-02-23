package TuskSyS.bienestar_api.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Spring Boot inyecta automáticamente los valores de nuestro application.yml aquí
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Método principal para generar el token a partir del email del usuario
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    // Método que construye la "tarjeta de acceso"
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims) // Datos extra (opcional)
                .setSubject(username) // El "dueño" del token (el email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Fecha de caducidad
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // La firma criptográfica
                .compact(); // Empaquetar todo en el famoso String codificado
    }

    // Método interno para leer nuestra clave secreta
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}