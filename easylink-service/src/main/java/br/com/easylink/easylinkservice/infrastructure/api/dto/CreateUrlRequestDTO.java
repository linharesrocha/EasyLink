package br.com.easylink.easylinkservice.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para criar um novo link encurtado")
public record CreateUrlRequestDTO(
        @Schema(description = "A URL original completa a ser encurtada", example = "https://www.google.com/search?q=long+url", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A URL original não pode estar em branco.")
        @URL(message = "A URL fornecida é inválida.")
        String originalUrl,

        @Schema(description = "Apelido personalizado opcional para o link. Deve conter apenas letras e números.", example = "minha-campanha-legal", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(min = 3, max = 50, message = "O apelido personalizado deve ter entre 3 e 50 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9\\-]+$", message = "O apelido personalizado deve conter apenas letras, números e hífens.")
        String customKey
) {}