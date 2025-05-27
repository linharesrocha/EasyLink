package br.com.easylink.easylinkservice.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para criar um novo link encurtado")
public record CreateUrlRequestDTO(
        @Schema(description = "A URL original completa a ser encurtada", example = "https://www.google.com/search?q=long+url", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A URL original não pode estar em branco.")
        @URL(message = "A URL fornecida é inválida.")
        String originalUrl
) {}