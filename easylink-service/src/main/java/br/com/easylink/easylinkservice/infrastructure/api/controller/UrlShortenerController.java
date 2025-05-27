package br.com.easylink.easylinkservice.infrastructure.api.controller;

import br.com.easylink.easylinkservice.application.ports.*;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlRequestDTO;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlResponseDTO;
import br.com.easylink.easylinkservice.infrastructure.api.dto.UpdateUrlRequestDTO;
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
public class UrlShortenerController {

    private final UrlShortenerUseCase urlShortenerUseCase;
    private final RedirectUseCase redirectUseCase;
    private final QrCodeGeneratorPort qrCodeGeneratorPort;
    private final UpdateUrlUseCase updateUrlUseCase;
    private final DeleteUrlUseCase deleteUrlUseCase;

    private final String BASE_URL = "http://localhost:8080/";
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String URL_CLICKS_TOPIC = "url-clicks-topic";

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
