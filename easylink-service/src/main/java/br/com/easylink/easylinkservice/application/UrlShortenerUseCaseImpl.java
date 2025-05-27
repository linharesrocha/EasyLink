package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.application.ports.UrlShortenerUseCase;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlShortenerUseCaseImpl implements UrlShortenerUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    public UrlMapping shortenUrl(String originalUrl, String ownerUsername) {
        String shortKey = RandomStringUtils.randomAlphanumeric(8);

        UrlMapping newUrlMapping = new UrlMapping();
        newUrlMapping.setOriginalUrl(originalUrl);
        newUrlMapping.setShortKey(shortKey);
        newUrlMapping.setCreatedAt(LocalDateTime.now());
        newUrlMapping.setOwnerUsername(ownerUsername);

        return urlMappingRepositoryPort.save(newUrlMapping);
    }
}
