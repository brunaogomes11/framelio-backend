package com.gomes.photographer_manager.domain.agency.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AgencyRequest(
    @Schema(description = "Nome fantasia da agência", example = "Studio Lente Viva")
    @NotBlank(message = "O nome fantasia é obrigatório")
    String nomeFantasia,

    @Schema(description = "CNPJ da agência (apenas dígitos)", example = "12345678000199")
    @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter 14 dígitos")
    String cnpj,

    @Schema(description = "Website da agência", example = "https://www.lenteviva.com.br")
    String website
) {
}
