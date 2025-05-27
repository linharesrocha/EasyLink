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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationUserDetailsService userDetailsService;

    private User user;
    private String existingUsername;
    private String nonExistingUsername;

    @BeforeEach
    void setUp() {
        existingUsername = "testuser";
        nonExistingUsername = "nonexistentuser";
        user = User.builder()
                .id(1L)
                .username(existingUsername)
                .password("encodedPassword")
                .role("USER")
                .build();
    }

    @Test
    @DisplayName("Deve carregar UserDetails quando usuário existe")
    void loadUserByUsername_quandoUsuarioExiste_deveRetornarUserDetails() {
        when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(existingUsername);

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("USER")));

        verify(userRepository, times(1)).findByUsername(existingUsername);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existe")
    void loadUserByUsername_quandoUsuarioNaoExiste_deveLancarUsernameNotFoundException() {
        when(userRepository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistingUsername);
        });

        assertEquals("Usuário '" + nonExistingUsername + "' não encontrado.", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(nonExistingUsername);
    }
}