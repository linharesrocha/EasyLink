package br.com.easylink.easylinkservice.infrastructure.api.controller;

import br.com.easylink.easylinkservice.application.ports.*;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlRequestDTO;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlResponseDTO;
import br.com.easylink.easylinkservice.infrastructure.api.dto.UpdateUrlRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Gerenciador de URLs", description = "Endpoints para criar, editar, deletar e redirecionar URLs encurtadas")
public class UrlShortenerController {

    private final UrlShortenerUseCase urlShortenerUseCase;
    private final RedirectUseCase redirectUseCase;
    private final QrCodeGeneratorPort qrCodeGeneratorPort;
    private final UpdateUrlUseCase updateUrlUseCase;
    private final DeleteUrlUseCase deleteUrlUseCase;

    private final String BASE_URL = "http://localhost:8080/";
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String URL_CLICKS_TOPIC = "url-clicks-topic";

    @Operation(summary = "Encurta uma nova URL",
            description = "Cria um novo link encurtado para a URL original fornecida. Requer autenticação.",
            security = @SecurityRequirement(name = "bearerAuth")) // Indica que este endpoint é protegido
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "URL encurtada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUrlResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (URL inválida ou em branco)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado (token JWT ausente ou inválido)"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (token JWT não tem permissão)"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateUrlResponseDTO> shortenUrl(
            @RequestBody @Valid CreateUrlRequestDTO request,
            @RequestHeader("X-User-Username") String username) {
        UrlMapping newMapping = urlShortenerUseCase.shortenUrl(request.originalUrl(), username);

        URI shortUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{shortKey}")
                .buildAndExpand(newMapping.getShortKey())
                .toUri();

        String gatewayShortURL = BASE_URL + newMapping.getShortKey();

        CreateUrlResponseDTO responseDto = new CreateUrlResponseDTO(
                newMapping.getShortKey(),
                newMapping.getOriginalUrl(),
                gatewayShortURL,
                newMapping.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Redireciona para a URL original",
            description = "Redireciona o usuário para a URL original correspondente à chave curta fornecida. Este endpoint é público.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecionamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado para a chave fornecida")
    })
    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortKey) {
        Optional<String> originalUrlOpt = redirectUseCase.getOriginalUrl(shortKey);

        if (originalUrlOpt.isPresent()) {
            log.info("Disparando evento de clique para o Kafka. Chave: {}", shortKey);
            kafkaTemplate.send(URL_CLICKS_TOPIC, shortKey);

            // Ação 2: Montar a resposta de redirecionamento.
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(URI.create(originalUrlOpt.get()))
                    .build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Obtém o QR Code para um link encurtado",
            description = "Retorna uma imagem PNG do QR Code para a URL encurtada correspondente à chave. Este endpoint é público.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagem do QR Code gerada com sucesso",
                    content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
            @ApiResponse(responseCode = "404", description = "Link não encontrado para a chave fornecida"),
            @ApiResponse(responseCode = "500", description = "Erro ao gerar QR Code")
    })
    @GetMapping(value = "/{shortKey}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortKey) {
        try {
            String fullShortUrl = BASE_URL + shortKey;

            // Usamos o método da nossa porta
            byte[] qrCodeImage = qrCodeGeneratorPort.generate(fullShortUrl, 250, 250);

            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCodeImage);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Atualiza a URL de destino de um link encurtado",
            description = "Altera a URL original para a qual uma chave curta redireciona. Requer autenticação e que o usuário seja o dono do link.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL de destino atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUrlResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (usuário não é o dono do link)"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/api/v1/urls/{shortKey}")
    public ResponseEntity<CreateUrlResponseDTO> updateShortenedUrl(
            @PathVariable String shortKey,
            @RequestBody @Valid UpdateUrlRequestDTO request,
            @RequestHeader("X-User-Username") String username
            ) {

        UrlMapping updatedMapping = updateUrlUseCase.updateUrl(shortKey, request.newOriginalUrl(), username);

        // Reutilizando o DTO de criação para a resposta, mas poderíamos ter um UpdateUrlResponseDTO
        String gatewayShortUrl = BASE_URL + updatedMapping.getShortKey();
        CreateUrlResponseDTO responseDto = new CreateUrlResponseDTO(
                updatedMapping.getShortKey(),
                updatedMapping.getOriginalUrl(),
                gatewayShortUrl,
                updatedMapping.getCreatedAt()
        );
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Deleta um link encurtado",
            description = "Remove um link encurtado do sistema. Requer autenticação e que o usuário seja o dono do link.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Link deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (usuário não é o dono do link)"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/api/v1/urls/{shortKey}")
    public ResponseEntity<Void> deleteShortenedUrl(
            @PathVariable String shortKey,
            @RequestHeader("X-User-Username") String username) {
        try {
            deleteUrlUseCase.deleteUrl(shortKey, username);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("Link não encontrado")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().startsWith("Usuário não autorizado")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            log.error("Erro inesperado ao deletar link: {}", shortKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
