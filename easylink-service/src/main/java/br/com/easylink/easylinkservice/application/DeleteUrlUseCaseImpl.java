package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
import br.com.easylink.easylinkservice.application.ports.DeleteUrlUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUrlUseCaseImpl implements DeleteUrlUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    @Transactional
    public void deleteUrl(String shortKey, String ownerUsername) {
        log.debug("Attempting to delete URL for shortKey [{}] by user [{}].", shortKey, ownerUsername);

        log.debug("Fetching URL mapping for shortKey: {}", shortKey);
        UrlMapping urlMapping = urlMappingRepositoryPort.findByShortKey(shortKey)
                .orElseThrow(() -> {
                    log.warn("URL mapping not found for shortKey: {} during delete attempt.", shortKey);
                    return new UrlNotFoundException("Link not found with key: " + shortKey);
                });
        log.debug("Found URL mapping: {}", urlMapping);

        if (!urlMapping.getOwnerUsername().equals(ownerUsername)) {
            log.warn("User [{}] is not authorized to delete link with shortKey [{}]. Owner is [{}].",
                    ownerUsername, shortKey, urlMapping.getOwnerUsername());
            throw new UserNotAuthorizedException("User not authorized to delete this link.");
        }
        log.debug("User [{}] authorized to delete link with shortKey [{}].", ownerUsername, shortKey);

        urlMappingRepositoryPort.deleteByShortKey(shortKey);
        log.info("URL mapping for shortKey [{}] deleted successfully by user [{}].", shortKey, ownerUsername);
    }
}