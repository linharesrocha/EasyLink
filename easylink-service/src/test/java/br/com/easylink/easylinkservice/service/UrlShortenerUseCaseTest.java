package br.com.easylink.easylinkservice.service;

import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.application.ports.UrlShortenerUseCase;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UrlShortenerUseCaseTest {

    @Autowired
    private UrlShortenerUseCase urlShortenerUseCase;

    @Autowired
    private UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Test
    @DisplayName("Deve encurtar uma URL com sucesso e salvá-la no banco")
    void shouldShortenUrlSuccessfully() {
        // Given
        String originalUrl = "https://www.google.com/search?q=tdd+spring+boot+hexagonal+architecture";

        // When
        UrlMapping result = urlShortenerUseCase.shortenUrl(originalUrl);

        // Then
        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertNotNull(result.getShortKey());
        assertEquals(8, result.getShortKey().length());
        assertNotNull(result.getCreatedAt());

        var savedMapping = urlMappingRepositoryPort.findByShortKey(result.getShortKey());
        assertTrue(savedMapping.isPresent());
        assertEquals(originalUrl, savedMapping.get().getOriginalUrl());
    }
}

