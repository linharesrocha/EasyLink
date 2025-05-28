// Path: easylink-service/src/test/java/br/com/easylink/easylinkservice/application/DeleteUrlUseCaseImplTest.java
package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
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
    @DisplayName("Should delete the link successfully when the user is the owner")
    void deleteUrl_whenLinkExistsAndUserIsOwner_shouldCallDeleteFromRepository() {
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));
        doNothing().when(urlMappingRepositoryPort).deleteByShortKey(shortKey);

        assertDoesNotThrow(() -> deleteUrlUseCase.deleteUrl(shortKey, ownerUsername));

        verify(urlMappingRepositoryPort, times(1)).findByShortKey(shortKey);
        verify(urlMappingRepositoryPort, times(1)).deleteByShortKey(shortKey);
    }

    @Test
    @DisplayName("Should throw UrlNotFoundException when the link to be deleted is not found")
    void deleteUrl_whenLinkIsNotFound_shouldThrowUrlNotFoundException() {
        String nonExistingKey = "nonExistingKey";
        when(urlMappingRepositoryPort.findByShortKey(nonExistingKey)).thenReturn(Optional.empty());

        UrlNotFoundException exception = assertThrows(UrlNotFoundException.class, () -> {
            deleteUrlUseCase.deleteUrl(nonExistingKey, ownerUsername);
        });

        assertEquals("Link not found with key: " + nonExistingKey, exception.getMessage());
        verify(urlMappingRepositoryPort, never()).deleteByShortKey(anyString());
    }

    @Test
    @DisplayName("Should throw UserNotAuthorizedException when user is not the owner of the link")
    void deleteUrl_whenUserIsNotTheOwner_shouldThrowUserNotAuthorizedException() {
        String anotherUser = "anotherUser";
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));

        UserNotAuthorizedException exception = assertThrows(UserNotAuthorizedException.class, () -> {
            deleteUrlUseCase.deleteUrl(shortKey, anotherUser);
        });

        assertEquals("User not authorized to delete this link.", exception.getMessage());
        verify(urlMappingRepositoryPort, never()).deleteByShortKey(anyString());
    }
}