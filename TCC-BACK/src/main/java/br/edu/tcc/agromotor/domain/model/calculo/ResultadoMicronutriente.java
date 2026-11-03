package br.edu.tcc.agromotor.domain.model.calculo;

import br.edu.tcc.agromotor.domain.model.Micronutriente;

import java.math.BigDecimal;

/**
 * @param avaliado         falso quando falta o teor no laudo ou o manual não indica dose para a cultura
 * @param classe           classe do teor no solo; nula quando o micronutriente não foi avaliado
 * @param doseKgHa         nula quando o micronutriente não foi avaliado
 * @param previstoNoManual falso quando o manual não indica dose desse nutriente para a cultura
 */
public record ResultadoMicronutriente(
        Micronutriente nutriente,
        boolean avaliado,
        BigDecimal teor,
        String classe,
        BigDecimal doseKgHa,
        String justificativa,
        boolean previstoNoManual) {
}
