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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUrlUseCaseImplTest {

    @Mock
    private UrlMappingRepositoryPort urlMappingRepositoryPort;

    @InjectMocks
    private UpdateUrlUseCaseImpl updateUrlUseCase;

    private UrlMapping existingUrlMapping;
    private String shortKey;
    private String ownerUsername;
    private String newOriginalUrl;

    @BeforeEach
    void setUp() {
        shortKey = "validKey";
        ownerUsername = "ownerUser";
        newOriginalUrl = "https://www.newexample.com";

        existingUrlMapping = new UrlMapping();
        existingUrlMapping.setId(1L);
        existingUrlMapping.setShortKey(shortKey);
        existingUrlMapping.setOriginalUrl("https://www.oldexample.com");
        existingUrlMapping.setOwnerUsername(ownerUsername);
        existingUrlMapping.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve atualizar a URL original quando o link existe e o usuário é o dono")
    void updateUrl_quandoLinkExisteEDonoCorreto_deveAtualizarEsalvar() {
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));
        when(urlMappingRepositoryPort.save(any(UrlMapping.class))).thenReturn(existingUrlMapping);

        UrlMapping updatedMapping = updateUrlUseCase.updateUrl(shortKey, newOriginalUrl, ownerUsername);

        ArgumentCaptor<UrlMapping> urlMappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepositoryPort, times(1)).save(urlMappingCaptor.capture());
        UrlMapping savedMapping = urlMappingCaptor.getValue();

        assertNotNull(updatedMapping);
        assertEquals(newOriginalUrl, savedMapping.getOriginalUrl());
        assertEquals(newOriginalUrl, updatedMapping.getOriginalUrl());
        assertEquals(ownerUsername, savedMapping.getOwnerUsername());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o link não é encontrado")
    void updateUrl_quandoLinkNaoExiste_deveLancarExcecao() {
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateUrlUseCase.updateUrl(shortKey, newOriginalUrl, ownerUsername);
        });

        assertEquals("Link não encontrado com a chave: " + shortKey, exception.getMessage());
        verify(urlMappingRepositoryPort, never()).save(any(UrlMapping.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o usuário não é o dono do link")
    void updateUrl_quandoUsuarioNaoEDono_deveLancarExcecao() {
        String wrongOwner = "anotherUser";
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateUrlUseCase.updateUrl(shortKey, newOriginalUrl, wrongOwner);
        });

        assertEquals("Usuário não autorizado a editar esse link.", exception.getMessage());
        verify(urlMappingRepositoryPort, never()).save(any(UrlMapping.class));
    }
}