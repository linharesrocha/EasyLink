package br.com.easylink.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/service")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<String> serviceFallback() {
        return Mono.just("Serviço temporariamente indisponível. Por favor, tente novamente mais tarde.");
    }
}
