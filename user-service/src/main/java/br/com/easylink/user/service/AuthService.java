package br.com.easylink.user.service;

import br.com.easylink.user.domain.User;
import br.com.easylink.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public User register(String username, String rawPassword) {
        if(userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Usuário já existe com o nome: " + username);
        }

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role("USER")
                .build();

        return userRepository.save(newUser);
    }

    public String login(String username, String password) {
        var authToken = new UsernamePasswordAuthenticationToken(username, password);

        var authentication = authenticationManager.authenticate(authToken);

        return tokenService.generateToken((User) authentication.getPrincipal());
    }
}
