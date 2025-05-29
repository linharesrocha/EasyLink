package br.com.easylink.user.service;

import br.com.easylink.user.domain.User;
import br.com.easylink.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public User register(String username, String rawPassword) {
        log.debug("Attempting to register user: {}", username);
        if(userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Usuário já existe com o nome: " + username);
        }
        log.debug("User {} does not exist, proceeding with registration.", username);

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role("USER")
                .build();
        log.debug("Password encoded for user {}.", username);

        User savedUser = userRepository.save(newUser);
        log.info("User {} registered successfully with ID {}.", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    public String login(String username, String password) {
        log.debug("Attempting to login user: {}", username);
        var authToken = new UsernamePasswordAuthenticationToken(username, password);

        log.debug("Authenticating user {} via AuthenticationManager.", username);
        var authentication = authenticationManager.authenticate(authToken);
        log.debug("User {} authenticated successfully. Principal type: {}", username, authentication.getPrincipal().getClass().getName());

        String token = tokenService.generateToken((User) authentication.getPrincipal());
        log.info("JWT token generated for user {}.", username);
        return token;
    }
}
