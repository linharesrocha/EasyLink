package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
import br.com.easylink.easylinkservice.application.ports.UpdateUrlUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUrlUseCaseImpl implements UpdateUrlUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    @Transactional
    public UrlMapping updateUrl(String shortKey, String newOriginalUrl, String ownerUsername) {
        log.debug("Attempting to update URL for shortKey [{}], newOriginalUrl [{}], by user [{}].",
                shortKey, newOriginalUrl, ownerUsername);

        log.debug("Fetching URL mapping for shortKey: {}", shortKey);
        UrlMapping urlMapping = urlMappingRepositoryPort.findByShortKey(shortKey)
                .orElseThrow(() -> {
                    log.warn("URL mapping not found for shortKey: {} during update attempt.", shortKey);
                    return new UrlNotFoundException("Link not found with key: " + shortKey);
                });
        log.debug("Found URL mapping: {}", urlMapping);

        if(!urlMapping.getOwnerUsername().equals(ownerUsername)) {
            log.warn("User [{}] is not authorized to edit link with shortKey [{}]. Owner is [{}].",
                    ownerUsername, shortKey, urlMapping.getOwnerUsername());
            throw new UserNotAuthorizedException("User not authorized to edit this link.");
        }
        log.debug("User [{}] authorized to edit link with shortKey [{}].", ownerUsername, shortKey);

        urlMapping.setOriginalUrl(newOriginalUrl);
        log.debug("Updating originalUrl for shortKey [{}] to [{}].", shortKey, newOriginalUrl);

        UrlMapping updatedMapping = urlMappingRepositoryPort.save(urlMapping);
        log.info("URL mapping for shortKey [{}] updated successfully by user [{}]. New original URL: [{}].",
                shortKey, ownerUsername, updatedMapping.getOriginalUrl());
        return updatedMapping;
    }
}