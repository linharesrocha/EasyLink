package br.com.easylink.user.api;

import br.com.easylink.user.api.dto.AuthResponseDTO;
import br.com.easylink.user.api.dto.LoginRequestDTO;
import br.com.easylink.user.api.dto.RegisterRequestDTO;
import br.com.easylink.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO request) {
        authService.register(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        String token = authService.login(request.username(), request.password());
        AuthResponseDTO response = new AuthResponseDTO(token);
        return ResponseEntity.ok(response);
    }
}
