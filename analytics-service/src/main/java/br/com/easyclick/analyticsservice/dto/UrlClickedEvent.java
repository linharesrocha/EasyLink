package br.com.easyclick.analyticsservice.dto;

import java.time.Instant;

public record UrlClickedEvent(
        String shortKey,
        Instant clickedAt,
        String userAgent,
        String referrer
) {}