package br.edu.tcc.agromotor.domain.model.calculo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param ncCalculadaTonHa         resultado direto da fórmula, em t/ha
 * @param necessidadeCalcarioTonHa dose final, em t/ha: zero se V1 não estiver abaixo do limite da cultura
 */
public record ResultadoCalagem(
        boolean necessario,
        BigDecimal v2AlvoPorcentagem,
        BigDecimal ncCalculadaTonHa,
        BigDecimal necessidadeCalcarioTonHa,
        String observacao,
        String justificativa,
        List<String> memoria) {
}
