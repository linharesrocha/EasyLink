package br.com.easylink.easylinkservice.application.ports;

import br.com.easylink.easylinkservice.domain.UrlMapping;

import java.time.Instant;

public interface UrlShortenerUseCase {
    UrlMapping shortenUrl(String originalUrl, String ownerUsername, String customKey, Instant expiresAt);
}
