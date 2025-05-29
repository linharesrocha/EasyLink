package br.com.easylink.user.service;

import br.com.easylink.user.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        log.info("Attempting to generate JWT token for user: {}", user.getUsername());
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("EasyLink API")
                    .withSubject(user.getUsername())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
            log.info("JWT token generated successfully for user: {}", user.getUsername());
            return token;
        } catch (JWTCreationException exception){
            log.error("Error generating JWT for user {}: {}", user.getUsername(), exception.getMessage(), exception);
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    private Instant generateExpirationDate() {
        Instant expiration = LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
        log.debug("Generated token expiration date: {} for user.", expiration);
        return expiration;
    }
}
