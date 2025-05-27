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
class DeleteUrlUseCaseImplTest {

    @Mock
    private UrlMappingRepositoryPort urlMappingRepositoryPort;

    @InjectMocks
    private DeleteUrlUseCaseImpl deleteUrlUseCase;

    private UrlMapping existingUrlMapping;
    private String shortKey;
    private String ownerUsername;

    @BeforeEach
    void setUp() {
        shortKey = "validKey";
        ownerUsername = "ownerUser";

        existingUrlMapping = new UrlMapping();
        existingUrlMapping.setId(1L);
        existingUrlMapping.setShortKey(shortKey);
        existingUrlMapping.setOriginalUrl("https://www.example.com");
        existingUrlMapping.setOwnerUsername(ownerUsername);
        existingUrlMapping.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve deletar o link com sucesso quando o usuário é o dono")
    void deleteUrl_quandoLinkExisteEUsuarioEDono_deveChamarDeleteDoRepositorio() {
        // GIVEN
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));
        doNothing().when(urlMappingRepositoryPort).deleteByShortKey(shortKey);

        // WHEN
        assertDoesNotThrow(() -> deleteUrlUseCase.deleteUrl(shortKey, ownerUsername));

        // THEN
        verify(urlMappingRepositoryPort, times(1)).findByShortKey(shortKey);
        verify(urlMappingRepositoryPort, times(1)).deleteByShortKey(shortKey);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o link não é encontrado para deleção")
    void deleteUrl_quandoLinkNaoExiste_deveLancarRuntimeException() {
        // GIVEN
        String nonExistingKey = "nonExistingKey";
        when(urlMappingRepositoryPort.findByShortKey(nonExistingKey)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteUrlUseCase.deleteUrl(nonExistingKey, ownerUsername);
        });

        assertEquals("Link não encontrado com a chave: " + nonExistingKey, exception.getMessage());
        verify(urlMappingRepositoryPort, never()).deleteByShortKey(anyString());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o usuário não é o dono do link para deleção")
    void deleteUrl_quandoUsuarioNaoEDono_deveLancarRuntimeException() {
        // GIVEN
        String anotherUser = "anotherUser";
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteUrlUseCase.deleteUrl(shortKey, anotherUser);
        });

        assertEquals("Usuário não autorizado a deletar este link.", exception.getMessage());
        verify(urlMappingRepositoryPort, never()).deleteByShortKey(anyString());
    }
}