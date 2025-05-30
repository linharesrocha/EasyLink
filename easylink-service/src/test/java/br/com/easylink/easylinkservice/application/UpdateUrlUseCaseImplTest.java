// Path: easylink-service/src/test/java/br/com/easylink/easylinkservice/application/UpdateUrlUseCaseImplTest.java
package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
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
        existingUrlMapping.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should update original URL when link exists and user is the owner")
    void updateUrl_whenLinkExistsAndUserIsOwner_shouldUpdateAndSaveChanges() {
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
    @DisplayName("Should throw UrlNotFoundException when link is not found")
    void updateUrl_whenLinkIsNotFound_shouldThrowUrlNotFoundException() {
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.empty());

        UrlNotFoundException exception = assertThrows(UrlNotFoundException.class, () -> {
            updateUrlUseCase.updateUrl(shortKey, newOriginalUrl, ownerUsername);
        });

        assertEquals("Link not found with key: " + shortKey, exception.getMessage());
        verify(urlMappingRepositoryPort, never()).save(any(UrlMapping.class));
    }

    @Test
    @DisplayName("Should throw UserNotAuthorizedException when user is not the owner")
    void updateUrl_whenUserIsNotTheOwner_shouldThrowUserNotAuthorizedException() {
        String wrongOwner = "anotherUser";
        when(urlMappingRepositoryPort.findByShortKey(shortKey)).thenReturn(Optional.of(existingUrlMapping));

        UserNotAuthorizedException exception = assertThrows(UserNotAuthorizedException.class, () -> {
            updateUrlUseCase.updateUrl(shortKey, newOriginalUrl, wrongOwner);
        });

        assertEquals("User not authorized to edit this link.", exception.getMessage());
        verify(urlMappingRepositoryPort, never()).save(any(UrlMapping.class));
    }
}