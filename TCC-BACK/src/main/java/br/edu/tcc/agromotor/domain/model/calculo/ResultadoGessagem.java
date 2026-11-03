package br.edu.tcc.agromotor.domain.model.calculo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param avaliada falso quando o talhão não tem laudo de 20-40 cm para avaliar
 */
public record ResultadoGessagem(
        boolean avaliada,
        boolean necessario,
        BigDecimal doseKgHa,
        String justificativa,
        List<String> memoria) {
}
