package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedirectUseCaseImplTest {

    @Mock
    private UrlMappingRepositoryPort urlMappingRepositoryPort;

    @InjectMocks
    private RedirectUseCaseImpl redirectUseCase;

    private String existingShortKey;
    private String nonExistingShortKey;
    private String originalUrl;
    private UrlMapping urlMapping;

    @BeforeEach
    void setUp() {
        existingShortKey = "testKey1";
        nonExistingShortKey = "noKeyHere";
        originalUrl = "https://www.example.com";

        urlMapping = new UrlMapping();
        urlMapping.setId(1L);
        urlMapping.setShortKey(existingShortKey);
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setCreatedAt(LocalDateTime.now());
        urlMapping.setOwnerUsername("testowner");
    }

    @Test
    @DisplayName("Deve retornar a URL original quando a shortKey existe")
    void getOriginalUrl_quandoChaveExiste_deveRetornarOptionalComUrl() {
        when(urlMappingRepositoryPort.findByShortKey(existingShortKey)).thenReturn(Optional.of(urlMapping));

        Optional<String> result = redirectUseCase.getOriginalUrl(existingShortKey);

        assertTrue(result.isPresent());
        assertEquals(originalUrl, result.get());
        verify(urlMappingRepositoryPort, times(1)).findByShortKey(existingShortKey);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando a shortKey não existe")
    void getOriginalUrl_quandoChaveNaoExiste_deveRetornarOptionalVazio() {
        when(urlMappingRepositoryPort.findByShortKey(nonExistingShortKey)).thenReturn(Optional.empty());

        Optional<String> result = redirectUseCase.getOriginalUrl(nonExistingShortKey);

        assertFalse(result.isPresent());
        verify(urlMappingRepositoryPort, times(1)).findByShortKey(nonExistingShortKey);
    }
}