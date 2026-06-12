package com.gomes.photographer_manager.domain.agency.response;

import com.gomes.photographer_manager.domain.agency.Agency;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma agência")
public record AgencyResponse(

        @Schema(description = "Identificador único ULID da agência")
        String id,

        @Schema(description = "ID ULID do usuário dono da agência")
        String userId,

        @Schema(description = "Nome fantasia da agência")
        String nomeFantasia,

        @Schema(description = "CNPJ da agência")
        String cnpj,

        @Schema(description = "Website da agência")
        String website
) {
    public AgencyResponse(Agency agency) {
        this(
                agency.getId(),
                agency.getUserId(),
                agency.getNomeFantasia(),
                agency.getCnpj(),
                agency.getWebsite()
        );
    }
}
