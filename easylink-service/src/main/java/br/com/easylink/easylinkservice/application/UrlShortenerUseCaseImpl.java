package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.CustomKeyAlreadyExistsException;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.application.ports.UrlShortenerUseCase;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
    public class UrlShortenerUseCaseImpl implements UrlShortenerUseCase {

    private static final int SHORT_KEY_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    public UrlMapping shortenUrl(String originalUrl, String ownerUsername, String customKey, Instant expiresAt) {
        log.debug("Attempting to shorten URL: [{}] for user: [{}]", originalUrl, ownerUsername);

        String finalShortKey;

        if(StringUtils.hasText(customKey)) {
            if(urlMappingRepositoryPort.findByShortKey(customKey).isPresent()) {
                log.warn("Custom key '{}' already exists. User: {}", customKey, ownerUsername);
                throw new CustomKeyAlreadyExistsException("O apelido '" + customKey + "' já está em uso. Por favor, escolha outro.");
            }
            finalShortKey = customKey;
            log.info("Using provided custom key '{}' for user: {}", finalShortKey, ownerUsername);
        } else {
            finalShortKey = generateUniqueShortKey();
            log.info("No custom key provided, generated unique short key '{}' for user: {}", finalShortKey, ownerUsername);
        }

        UrlMapping newUrlMapping = new UrlMapping();
        newUrlMapping.setOriginalUrl(originalUrl);
        newUrlMapping.setShortKey(finalShortKey);
        newUrlMapping.setCreatedAt(Instant.now());
        newUrlMapping.setExpiresAt(expiresAt);
        newUrlMapping.setOwnerUsername(ownerUsername);

        log.debug("Saving new URL mapping: Original=[{}], ShortKey=[{}], User=[{}], CreatedAt=[{}], ExpiresAt=[{}]",
                newUrlMapping.getOriginalUrl(),
                newUrlMapping.getShortKey(),
                newUrlMapping.getOwnerUsername(),
                newUrlMapping.getCreatedAt(),
                newUrlMapping.getExpiresAt());

        return urlMappingRepositoryPort.save(newUrlMapping);
    }

    private String generateUniqueShortKey() {
        int attempt = 0;
        while (attempt < MAX_GENERATION_ATTEMPTS) {
            String generatedKey = RandomStringUtils.randomAlphanumeric(SHORT_KEY_LENGTH);

            if (urlMappingRepositoryPort.findByShortKey(generatedKey).isEmpty()) {
                log.info("Generated unique short key '{}' in {} attempt(s).", generatedKey, attempt + 1);
                return generatedKey;
            }

            log.warn("Collision detected for generated key '{}'. Retrying...", generatedKey);
            attempt++;
        }

        log.error("Failed to generate a unique short key after {} attempts.", MAX_GENERATION_ATTEMPTS);
        throw new IllegalStateException("Could not generate a unique short key. Please try again later.");
    }
}