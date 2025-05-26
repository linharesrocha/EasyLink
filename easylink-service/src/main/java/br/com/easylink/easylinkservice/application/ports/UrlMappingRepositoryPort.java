package br.com.easylink.easylinkservice.application.ports;

import br.com.easylink.easylinkservice.domain.UrlMapping;

import java.util.Optional;

public interface UrlMappingRepositoryPort {

    UrlMapping save(UrlMapping urlMapping);
    Optional<UrlMapping> findByShortKey(String shortKey);
}
