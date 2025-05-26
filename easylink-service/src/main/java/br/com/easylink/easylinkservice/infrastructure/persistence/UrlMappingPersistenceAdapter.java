package br.com.easylink.easylinkservice.infrastructure.persistence;

import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlMappingPersistenceAdapter implements UrlMappingRepositoryPort {

    private final SpringDataUrlMappingRepository repository;

    @Override
    public UrlMapping save(UrlMapping urlMapping) {
        return repository.save(urlMapping);
    }

    @Override
    public Optional<UrlMapping> findByShortKey(String shortKey) {
        return repository.findByShortKey(shortKey);
    }
}
