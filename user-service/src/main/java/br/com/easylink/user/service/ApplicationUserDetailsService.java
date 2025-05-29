package br.com.easylink.user.service;

import br.com.easylink.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user details for Spring Security: {}", username);

        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("User '{}' found for Spring Security UserDetails.", username);
                    return user;
                })
                .orElseThrow(() -> {
                    log.warn("User '{}' not found when attempting to load UserDetails for Spring Security.", username);
                    return new UsernameNotFoundException("Usuário '" + username + "' não encontrado.");
                });
    }
}
