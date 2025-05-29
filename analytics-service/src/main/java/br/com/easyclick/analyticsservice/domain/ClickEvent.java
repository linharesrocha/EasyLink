package br.com.easyclick.analyticsservice.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "click_events")
@NoArgsConstructor
public class ClickEvent {
    @Id
    private String id;
    private String shortKey;
    private LocalDateTime clickTimestamp;

    public ClickEvent(String shortKey, LocalDateTime clickTimestamp) {
        this.shortKey = shortKey;
        this.clickTimestamp = LocalDateTime.now();
    }
}
