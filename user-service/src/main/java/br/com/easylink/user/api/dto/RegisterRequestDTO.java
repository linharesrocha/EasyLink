package br.com.easylink.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para registrar um novo usuário")
public record RegisterRequestDTO(
        @Schema(description = "Nome de usuário único para o login", example = "joao.silva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String username,

        @Schema(description = "Senha para o novo usuário", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String password
) {}
