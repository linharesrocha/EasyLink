package br.com.easylink.easylinkservice.application.ports;

import br.com.easylink.easylinkservice.domain.UrlMapping;

public interface UrlShortenerUseCase {
    UrlMapping shortenUrl(String originalUrl, String ownerUsername);
}
