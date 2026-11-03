package br.edu.tcc.agromotor.domain.model.parametro;

import java.math.BigDecimal;

/**
 * Faixa semiaberta [min, max) das tabelas paramétricas: min é inclusivo, max é
 * exclusivo e um limite nulo significa "sem limite" naquele lado.
 */
public record Intervalo(BigDecimal min, BigDecimal max) {

    public static final Intervalo QUALQUER = new Intervalo(null, null);

    public boolean contem(BigDecimal valor) {
        return (min == null || valor.compareTo(min) >= 0)
                && (max == null || valor.compareTo(max) < 0);
    }
}
