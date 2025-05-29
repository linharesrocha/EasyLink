package br.com.easylink.easylinkservice.infrastructure.messaging.dto;

import java.time.Instant;

public record UrlClickedEvent(
   String shortKey,
   Instant clickedAt,
   String userAgent,
   String referrer
) {}
