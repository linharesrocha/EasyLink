package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShortenerUseCaseImplTest {

    @Mock
    private UrlMappingRepositoryPort urlMappingRepositoryPort;

    @InjectMocks
    private UrlShortenerUseCaseImpl urlShortenerUseCase;

    private String originalUrl;
    private String ownerUsername;
    private String customKey;
    private UrlMapping urlMappingMock;

    @BeforeEach
    void setUp() {
        originalUrl = "https://www.example.com/muito/longa/url";
        ownerUsername = "testuser";
        customKey = "meu-site";

        urlMappingMock = new UrlMapping();
        urlMappingMock.setId(1L);
        urlMappingMock.setOriginalUrl(originalUrl);
        urlMappingMock.setOwnerUsername(ownerUsername);
        urlMappingMock.setShortKey("ABC123XY");
        urlMappingMock.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Deve encurtar URL com data de expiração e salvar corretamente")
    void shortenUrl_comDataExpiracao_deveSalvarComDadosCorretos() {
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        when(urlMappingRepositoryPort.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Assumindo que a interface e a implementação de UrlShortenerUseCase foram atualizadas
        // para aceitar expiresAt
        UrlMapping result = urlShortenerUseCase.shortenUrl(originalUrl, ownerUsername, null, futureDate);

        ArgumentCaptor<UrlMapping> urlMappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepositoryPort, times(1)).save(urlMappingCaptor.capture());

        UrlMapping capturedMapping = urlMappingCaptor.getValue();

        assertNotNull(result);
        assertEquals(originalUrl, capturedMapping.getOriginalUrl());
        assertEquals(ownerUsername, capturedMapping.getOwnerUsername());
        assertNotNull(capturedMapping.getShortKey());
        assertNotNull(capturedMapping.getCreatedAt());
        assertEquals(futureDate, capturedMapping.getExpiresAt()); // Verificar expiresAt
        assertEquals(futureDate, result.getExpiresAt());
    }

    @Test
    @DisplayName("Deve encurtar URL sem data de expiração e expiresAt deve ser nulo")
    void shortenUrl_semDataExpiracao_deveSalvarComExpiresAtNulo() {
        when(urlMappingRepositoryPort.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Assumindo que a interface e a implementação de UrlShortenerUseCase foram atualizadas
        // para aceitar expiresAt (que pode ser nulo)
        UrlMapping result = urlShortenerUseCase.shortenUrl(originalUrl, ownerUsername, null, null);

        ArgumentCaptor<UrlMapping> urlMappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepositoryPort, times(1)).save(urlMappingCaptor.capture());
        UrlMapping capturedMapping = urlMappingCaptor.getValue();

        assertNotNull(result);
        assertNull(capturedMapping.getExpiresAt()); // Verificar que expiresAt é nulo
        assertNull(result.getExpiresAt());
    }
}