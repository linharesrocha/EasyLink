package br.com.easylink.analyticsservice.dto;

import java.time.Instant;

public record UrlClickedEvent(
        String shortKey,
        Instant clickedAt,
        String userAgent,
        String referrer
) {}