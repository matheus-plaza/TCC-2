package br.edu.tcc.agromotor.domain.model.parametro;

import br.edu.tcc.agromotor.domain.model.Micronutriente;

import java.math.BigDecimal;

/**
 * Dose de um micronutriente conforme a classe no solo. Acima do nível crítico, a dose
 * é zero.
 *
 * @param observacao decisão de leitura da tabela do manual; nula quando não há
 */
public record RegraMicronutriente(
        Long culturaId,
        Micronutriente nutriente,
        String classe,
        BigDecimal doseKgHa,
        String observacao) {
}
