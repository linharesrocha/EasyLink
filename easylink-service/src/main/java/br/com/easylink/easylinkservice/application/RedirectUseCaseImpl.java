package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.RedirectUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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

        if (urlMappingOpt.isPresent()) {
            log.debug("Found original URL [{}] in database for shortKey [{}].",
                    urlMappingOpt.get().getOriginalUrl(), shortKey);
            return urlMappingOpt.map(UrlMapping::getOriginalUrl);
        } else {
            log.debug("No original URL found in database for shortKey [{}].", shortKey);
            return Optional.empty();
        }
    }
}