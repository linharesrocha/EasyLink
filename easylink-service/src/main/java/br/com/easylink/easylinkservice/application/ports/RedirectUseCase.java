package br.com.easylink.easylinkservice.application.ports;

import java.util.Optional;

public interface RedirectUseCase {
    Optional<String> getOriginalUrl(String shortKey);
}
