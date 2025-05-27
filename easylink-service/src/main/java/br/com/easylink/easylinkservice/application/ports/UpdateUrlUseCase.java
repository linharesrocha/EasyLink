package br.com.easylink.easylinkservice.application.ports;

import br.com.easylink.easylinkservice.domain.UrlMapping;

public interface UpdateUrlUseCase {
    UrlMapping updateUrl(String shortKey, String newOriginalUrl, String ownerUsername);
}
