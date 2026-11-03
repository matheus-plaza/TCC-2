package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.TexturaSolo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TalhaoRequest(
        @NotBlank(message = "informe a identificação do talhão")
        @Size(max = 100, message = "Identificação do talhão: use no máximo 100 caracteres") String identificacao,
        @NotNull(message = "informe a área") @DecimalMin(value = "0.01", message = "a área deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Área: use até 8 dígitos inteiros e 2 casas decimais") BigDecimal areaHectares,
        TexturaSolo texturaPredominante) {
}
