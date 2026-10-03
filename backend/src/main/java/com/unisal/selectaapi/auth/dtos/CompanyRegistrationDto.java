package com.unisal.selectaapi.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRegistrationDto(
        @NotBlank(message = "O nome do responsável é obrigatório")
        String responsibleName,

        @NotBlank(message = "O e-mail corporativo é obrigatório")
        @Email(message = "Informe um e-mail corporativo válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        String password,

        @NotBlank(message = "A razão social ou nome da empresa é obrigatório")
        String companyName,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Pattern(
                regexp = "(?:\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})",
                message = "Informe um CNPJ com 14 dígitos")
        String cnpj,

        @NotBlank(message = "O segmento da empresa é obrigatório")
        String industry,

        @Pattern(
                regexp = "^$|https?://.+",
                message = "O site deve começar com http:// ou https://")
        String website,

        @NotNull(message = "latitude é obrigatorio")
        Double latitude,

        @NotNull(message = "longitude é obrigatório")
        Double longitude,

        @Size(max = 1500, message = "A descrição deve ter no máximo 1500 caracteres")
        String description) {
}
