package br.com.easylink.easylinkservice.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UpdateUrlRequestDTO(
        @NotBlank(message = "A nova URL original não pode estar em branco.")
        @URL(message = "A nova URL fornecida é invalida.")
        String newOriginalUrl
)
{}
