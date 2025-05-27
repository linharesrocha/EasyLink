package br.com.easyclick.analyticsservice.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "click_events")
public class ClickEvent {
    @Id
    private String id;
    private String shortKey;
    private LocalDateTime clickTimestamp;

    public ClickEvent(String shortKey) {
        this.shortKey = shortKey;
        this.clickTimestamp = LocalDateTime.now();
    }
}
