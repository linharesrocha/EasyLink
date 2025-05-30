package br.com.easylink.easylinkservice.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Schema(description = "Dados para criar um novo link encurtado")
public record CreateUrlRequestDTO(
        @Schema(description = "A URL original completa a ser encurtada", example = "https://www.google.com/search?q=long+url", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A URL original não pode estar em branco.")
        @URL(message = "A URL fornecida é inválida.")
        String originalUrl,

        @Schema(description = "Apelido personalizado opcional para o link. Deve conter apenas letras e números.", example = "minha-campanha-legal", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(min = 3, max = 50, message = "O apelido personalizado deve ter entre 3 e 50 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9\\-]+$", message = "O apelido personalizado deve conter apenas letras, números e hífens.")
        String customKey,

        @Schema(description = "Data e hora de expiração opcional para o link (formato ISO-8601 UTC, ex: 2025-12-31T23:59:59Z). Se fornecida, deve ser no futuro.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Future(message = "A data de expiração deve ser no futuro.")
        Instant expiresAt
) {}