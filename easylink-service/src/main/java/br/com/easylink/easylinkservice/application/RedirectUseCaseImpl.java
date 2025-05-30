package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.RedirectUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedirectUseCaseImpl implements RedirectUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    @Cacheable(value = "redirects", key = "#shortKey")
    public Optional<String> getOriginalUrl(String shortKey) {
        log.info("Cache miss! Attempting to fetch original URL from database for shortKey: {}", shortKey);

        Optional<UrlMapping> urlMappingOpt = urlMappingRepositoryPort.findByShortKey(shortKey);

        // Verifica se existe a shortKey
        if (urlMappingOpt.isPresent()) {
            UrlMapping urlMapping = urlMappingOpt.get();


            // Se existir, verifica se não está com data expirado
            if(urlMapping.getExpiresAt() != null && Instant.now().isAfter(urlMapping.getExpiresAt())) {
                log.warn("Link with shortKey [{}] has expired at [{}]. Current time: [{}].", shortKey, urlMapping.getExpiresAt(), Instant.now());
                urlMappingRepositoryPort.deleteByShortKey(shortKey);
                return Optional.empty();
            }
            log.debug("Found original URL [{}] in database for shortKey [{}].", urlMappingOpt.get().getOriginalUrl(), shortKey);
            return Optional.of(urlMapping.getOriginalUrl());
        } else {
            log.debug("No original URL found in database for shortKey [{}].", shortKey);
            return Optional.empty();
        }
    }
}