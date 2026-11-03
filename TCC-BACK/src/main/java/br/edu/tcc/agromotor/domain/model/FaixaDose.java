package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;

/**
 * Dose como o manual a apresenta: uma faixa, em kg/ha. O sistema não escolhe um valor
 * dentro dela; essa decisão é do agrônomo. Dose fixa tem mínimo igual ao máximo.
 */
public record FaixaDose(BigDecimal min, BigDecimal max) {

    public FaixaDose {
        min = Decimais.valor(min);
        max = Decimais.valor(max);
    }

    public static FaixaDose zero() {
        return new FaixaDose(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public String descricao() {
        return min.compareTo(max) == 0
                ? Decimais.formatar(min)
                : Decimais.formatar(min) + " a " + Decimais.formatar(max);
    }
}
