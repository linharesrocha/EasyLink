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

import java.time.LocalDateTime;

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
    private UrlMapping urlMappingMock;

    @BeforeEach
    void setUp() {
        originalUrl = "https://www.example.com/muito/longa/url";
        ownerUsername = "testuser";

        urlMappingMock = new UrlMapping();
        urlMappingMock.setId(1L);
        urlMappingMock.setOriginalUrl(originalUrl);
        urlMappingMock.setOwnerUsername(ownerUsername);
        urlMappingMock.setShortKey("ABC123XY");
        urlMappingMock.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve encurtar URL e salvar com os dados corretos")
    void shortenUrl_comDadosValidos_deveSalvarComDadosCorretos() {
        when(urlMappingRepositoryPort.save(any(UrlMapping.class))).thenReturn(urlMappingMock);

        UrlMapping result = urlShortenerUseCase.shortenUrl(originalUrl, ownerUsername);

        ArgumentCaptor<UrlMapping> urlMappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepositoryPort, times(1)).save(urlMappingCaptor.capture());

        UrlMapping capturedMapping = urlMappingCaptor.getValue();

        assertNotNull(result);
        assertEquals(originalUrl, capturedMapping.getOriginalUrl());
        assertEquals(ownerUsername, capturedMapping.getOwnerUsername());
        assertNotNull(capturedMapping.getShortKey());
        assertEquals(8, capturedMapping.getShortKey().length());
        assertNotNull(capturedMapping.getCreatedAt());

        assertEquals(urlMappingMock.getShortKey(), result.getShortKey());
    }
}