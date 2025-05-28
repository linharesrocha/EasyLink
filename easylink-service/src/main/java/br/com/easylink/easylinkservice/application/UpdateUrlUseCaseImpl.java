package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
import br.com.easylink.easylinkservice.application.ports.UpdateUrlUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUrlUseCaseImpl implements UpdateUrlUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    @Transactional
    public UrlMapping updateUrl(String shortKey, String newOriginalUrl, String ownerUsername) {
        UrlMapping urlMapping = urlMappingRepositoryPort.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException("Link not found with key: " + shortKey));

        if(!urlMapping.getOwnerUsername().equals(ownerUsername)) {
            throw new UserNotAuthorizedException("User not authorized to edit this link.");
        }

        urlMapping.setOriginalUrl(newOriginalUrl);

        return urlMappingRepositoryPort.save(urlMapping);
    }
}
