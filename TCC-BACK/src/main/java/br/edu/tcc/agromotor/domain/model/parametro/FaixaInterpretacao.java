package br.edu.tcc.agromotor.domain.model.parametro;

import br.edu.tcc.agromotor.domain.model.AtributoSolo;

import java.math.BigDecimal;

/**
 * Linha da tabela de enquadramento: atributo + critério cruzado (argila para P,
 * CTC para K) + faixa de teor = classe de fertilidade.
 */
public record FaixaInterpretacao(AtributoSolo atributo, Intervalo criterio, Intervalo teor, String classe) {

    public boolean aplicaA(BigDecimal valorCriterio, BigDecimal valorTeor) {
        return criterio.contem(valorCriterio) && teor.contem(valorTeor);
    }
}
