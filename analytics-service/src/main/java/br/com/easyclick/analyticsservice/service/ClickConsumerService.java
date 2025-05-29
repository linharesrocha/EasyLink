package br.com.easyclick.analyticsservice.service;

import br.com.easyclick.analyticsservice.domain.ClickEvent;
import br.com.easyclick.analyticsservice.dto.UrlClickedEvent;
import br.com.easyclick.analyticsservice.persistence.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickConsumerService {

    private final ClickEventRepository repository;
    private static final ZoneId BRT_ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @KafkaListener(topics = "url-clicks-topic", groupId = "analytics-group-1")
    public void listen(UrlClickedEvent event) {
        log.info("Evento de clique recebido para a chave: {}", event.shortKey());

        ClickEvent newClickEvent = new ClickEvent(
                event.shortKey(),
                event.clickedAt().atZone(BRT_ZONE_ID).toLocalDateTime()
        );

        repository.save(newClickEvent);

        log.info("Evento de clique salvo no MongoDB com ID: {}", newClickEvent);
    }
}
