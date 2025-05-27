package br.com.easylink.user.service;

import br.com.easylink.user.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private User user;
    private final String testSecret = "meu-secret-super-secreto-para-gerar-tokens-do-easylink";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", testSecret);
        user = User.builder()
                .username("testuser")
                .build();
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido")
    void generateToken_comUsuarioValido_deveRetornarToken() {
        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(testSecret))
                .withIssuer("EasyLink API")
                .build()
                .verify(token);

        assertEquals("testuser", decodedJWT.getSubject());
        assertEquals("EasyLink API", decodedJWT.getIssuer());
        assertTrue(decodedJWT.getExpiresAtAsInstant().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("Deve ter data de expiração correta (aproximadamente 2 horas no futuro)")
    void generateToken_deveTerDataExpiracaoCorreta() {
        String token = tokenService.generateToken(user);
        DecodedJWT decodedJWT = JWT.decode(token); // Decodifica sem verificar assinatura para pegar a data

        Instant expectedMinExpiration = LocalDateTime.now().plusHours(2).minusMinutes(1).toInstant(ZoneOffset.of("-03:00"));
        Instant expectedMaxExpiration = LocalDateTime.now().plusHours(2).plusMinutes(1).toInstant(ZoneOffset.of("-03:00"));

        assertTrue(decodedJWT.getExpiresAtAsInstant().isAfter(expectedMinExpiration));
        assertTrue(decodedJWT.getExpiresAtAsInstant().isBefore(expectedMaxExpiration));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando JWTCreationException ocorrer")
    void generateToken_quandoFalhaNaCriacaoJWT_deveLancarRuntimeException() {
        ReflectionTestUtils.setField(tokenService, "secret", null); // Simula um secret inválido

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tokenService.generateToken(user);
        });

        assertEquals("The Secret cannot be null", exception.getMessage());
    }
}