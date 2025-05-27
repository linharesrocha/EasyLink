package br.com.easylink.user.service;

import br.com.easylink.user.domain.User;
import br.com.easylink.user.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private String rawPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        // Configuração comum para os testes
        rawPassword = "password123";
        encodedPassword = "encodedPassword123";
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password(encodedPassword)
                .role("USER")
                .build();
    }

    // --- Testes para o método register ---

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso")
    void register_quandoUsuarioNaoExiste_deveRetornarUsuarioSalvo() {
        // GIVEN
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // WHEN
        User savedUser = authService.register(user.getUsername(), rawPassword);

        // THEN
        assertNotNull(savedUser);
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals("USER", savedUser.getRole());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Não deve registrar usuário se o username já existir")
    void register_quandoUsuarioJaExiste_deveLancarIllegalStateException() {
        // GIVEN
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // WHEN & THEN
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.register(user.getUsername(), rawPassword);
        });

        assertEquals("Usuário já existe com o nome: " + user.getUsername(), exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Testes para o método login ---

    @Test
    @DisplayName("Deve autenticar usuário com sucesso e retornar token JWT")
    void login_quandoCredenciaisValidas_deveRetornarToken() {
        // GIVEN
        org.springframework.security.core.Authentication authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        // Quando o authenticationManager.authenticate for chamado, retorna objeto mockado
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String expectedToken = "mocked-jwt-token";
        // Quando o tokenService.generateToken for chamado, retorna um token mockado
        when(tokenService.generateToken(user)).thenReturn(expectedToken);

        // WHEN
        String actualToken = authService.login(user.getUsername(), rawPassword);

        // THEN
        assertNotNull(actualToken);
        assertEquals(expectedToken, actualToken);

        // Verifica as interações
        verify(authenticationManager, times(1)).authenticate(
                argThat(token -> token.getName().equals(user.getUsername()) &&
                        token.getCredentials().toString().equals(rawPassword))
        );
        verify(tokenService, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("Deve lançar AuthenticationException quando AuthenticationManager falhar")
    void login_quandoAuthenticationManagerLancaExcecao_devePropagarExcecao() {
        // GIVEN
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Credenciais inválidas mockadas") {});

        // WHEN & THEN
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> {
            authService.login(user.getUsername(), rawPassword);
        });
        verify(tokenService, never()).generateToken(any(User.class));
    }

}