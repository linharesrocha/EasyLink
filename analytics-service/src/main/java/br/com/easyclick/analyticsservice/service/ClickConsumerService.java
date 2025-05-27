package br.com.easyclick.analyticsservice.service;

import br.com.easyclick.analyticsservice.domain.ClickEvent;
import br.com.easyclick.analyticsservice.persistence.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickConsumerService {

    private final ClickEventRepository repository;

    @KafkaListener(topics = "url-clicks-topic", groupId = "analytics-group-1")
    public void listen(String shortKey) {
        log.info("Evento de clique recebido para a chave: {}", shortKey);

        ClickEvent newClickEvent = new ClickEvent(shortKey);
        repository.save(newClickEvent);

        log.info("Evento de clique salvo no MongoDB com ID: {}", newClickEvent);
    }
}
