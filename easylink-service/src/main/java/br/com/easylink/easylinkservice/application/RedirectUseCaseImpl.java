package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.RedirectUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedirectUseCaseImpl implements RedirectUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    public Optional<String> getOriginalUrl(String shortKey) {
        return urlMappingRepositoryPort.findByShortKey(shortKey)
                .map(urlMapping -> urlMapping.getOriginalUrl());
    }
}
