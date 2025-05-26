package br.com.easylink.easylinkservice.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateUrlRequestDTO(
        @NotBlank(message = "A URL original não pode estar em branco.")
        @URL(message = "A URL fornecida é inválida.")
        String originalUrl
) {}
