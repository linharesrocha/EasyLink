package br.com.easylink.user.api;

import br.com.easylink.user.api.dto.AuthResponseDTO;
import br.com.easylink.user.api.dto.LoginRequestDTO;
import br.com.easylink.user.api.dto.RegisterRequestDTO;
import br.com.easylink.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registra um novo usuário",
            description = "Cria um novo usuário no sistema com o nome de usuário e senha fornecidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: username já existe ou campos em branco)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO request) {
        log.info("Attempting to register new user: {}", request.username());
        try {
            authService.register(request.username(), request.password());
            log.info("User {} registration process completed successfully.", request.username());
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
        } catch (IllegalStateException e) {
            log.warn("Registration failed for user {}: {}", request.username(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during registration for user {}: {}", request.username(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @Operation(summary = "Autentica um usuário",
            description = "Autentica um usuário com nome de usuário e senha, retornando um token JWT em caso de sucesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, token JWT retornado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (campos em branco)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (usuário ou senha incorretos)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        log.info("Login attempt for user: {}", request.username());
        try {
            String token = authService.login(request.username(), request.password());
            AuthResponseDTO response = new AuthResponseDTO(token);
            log.info("User {} logged in successfully.", request.username());
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Login failed for user {}: Invalid credentials.", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Exemplo
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", request.username(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
