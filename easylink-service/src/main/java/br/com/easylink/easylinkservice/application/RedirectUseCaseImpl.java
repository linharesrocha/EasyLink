package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.RedirectUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedirectUseCaseImpl implements RedirectUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String URL_CLICKS_TOPIC = "url-clicks-topic";

    @Override
    @Cacheable(value = "redirects", key = "#shortKey")
    public Optional<String> getOriginalUrl(String shortKey) {
        return urlMappingRepositoryPort.findByShortKey(shortKey)
                .map(urlMapping -> {

                    kafkaTemplate.send(URL_CLICKS_TOPIC, urlMapping.getShortKey());

                    return urlMapping.getOriginalUrl();
                });
    }
}
