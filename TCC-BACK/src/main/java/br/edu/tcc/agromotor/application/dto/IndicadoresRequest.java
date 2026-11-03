package br.edu.tcc.agromotor.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Teores usados na pré-visualização dos valores calculados da Tela A. */
public record IndicadoresRequest(
        @NotNull(message = "informe o cálcio") @DecimalMin(value = "0", message = "o cálcio não pode ser negativo") BigDecimal caCmolc,
        @NotNull(message = "informe o magnésio") @DecimalMin(value = "0", message = "o magnésio não pode ser negativo") BigDecimal mgCmolc,
        @NotNull(message = "informe o potássio") @DecimalMin(value = "0", message = "o potássio não pode ser negativo") BigDecimal kCmolc,
        @NotNull(message = "informe o alumínio") @DecimalMin(value = "0", message = "o alumínio não pode ser negativo") BigDecimal alCmolc,
        @NotNull(message = "informe a acidez potencial") @DecimalMin(value = "0", message = "a acidez potencial não pode ser negativa") BigDecimal hAlCmolc) {
}
