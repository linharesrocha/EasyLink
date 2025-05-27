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
        log.info("Cache miss! Buscando no banco de dados para a chave: {}", shortKey);
        return urlMappingRepositoryPort.findByShortKey(shortKey)
                .map(UrlMapping::getOriginalUrl);
    }
}
