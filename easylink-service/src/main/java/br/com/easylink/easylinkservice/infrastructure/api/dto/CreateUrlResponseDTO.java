package br.com.easylink.easylinkservice.infrastructure.api.dto;

import java.time.LocalDateTime;

public record CreateUrlResponseDTO(
   String shortKey,
   String originalUrl,
   String shortUrl,
   LocalDateTime createdAt
) {}
