package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
import br.com.easylink.easylinkservice.application.ports.DeleteUrlUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeleteUrlUseCaseImpl implements DeleteUrlUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    @Transactional
    public void deleteUrl(String shortKey, String ownerUsername) {
        UrlMapping urlMapping = urlMappingRepositoryPort.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException("Link not found with key: " + shortKey));
        if (!urlMapping.getOwnerUsername().equals(ownerUsername)) {
            throw new UserNotAuthorizedException("User not authorized to delete this link.");
        }
        urlMappingRepositoryPort.deleteByShortKey(shortKey);
    }
}
