package br.com.easylink.easylinkservice.infrastructure.api.controller;

import br.com.easylink.easylinkservice.application.ports.RedirectUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlShortenerUseCase;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlRequestDTO;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerUseCase urlShortenerUseCase;
    private final RedirectUseCase redirectUseCase;

    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateUrlResponseDTO> shortenUrl(@RequestBody @Valid CreateUrlRequestDTO request) {
        UrlMapping newMapping = urlShortenerUseCase.shortenUrl(request.originalUrl());

        URI shortUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{shortKey}")
                .buildAndExpand(newMapping.getShortKey())
                .toUri();

        CreateUrlResponseDTO responseDto = new CreateUrlResponseDTO(
                newMapping.getShortKey(),
                newMapping.getOriginalUrl(),
                shortUri.toString(),
                newMapping.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortKey) {
        Optional<String> originalUrlOpt= redirectUseCase.getOriginalUrl(shortKey);

        if(originalUrlOpt.isPresent()) {
            String originalUrl = originalUrlOpt.get();
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(URI.create(originalUrl))
                    .build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
