package br.edu.tcc.agromotor.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Colheita e gasto de uma safra. Com todos os campos vazios, o registro é removido. */
public record RegistroSafraRequest(
        @Digits(integer = 3, fraction = 2, message = "Produtividade: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99)") BigDecimal produtividadeColhidaTonHa,
        @PastOrPresent(message = "a data da colheita não pode ser futura") LocalDate dataColheita,
        @Digits(integer = 10, fraction = 2, message = "Gasto: use até 10 dígitos inteiros e 2 casas decimais") BigDecimal custoAdubacaoReais) {
}
