package br.edu.tcc.agromotor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code produtorId} é obrigatório para agrônomo e administrador; o produtor cadastra para si. */
public record PropriedadeRequest(
        Long produtorId,
        @NotBlank(message = "informe o nome da propriedade")
        @Size(max = 150, message = "Nome da propriedade: use no máximo 150 caracteres") String nome,
        @NotBlank(message = "informe o município") @Size(max = 100, message = "Município: use no máximo 100 caracteres") String municipio) {
}
