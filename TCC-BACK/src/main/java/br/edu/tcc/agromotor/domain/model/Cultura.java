package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;

/**
 * Cultura e seus requisitos, vindos da tabela paramétrica {@code culturas}.
 *
 * @param vAlvo   V2: saturação por bases a atingir com a calagem, em %
 * @param vMinimo saturação por bases abaixo da qual o manual indica o calcário, em %
 */
public record Cultura(
        Long id,
        String nome,
        BigDecimal vAlvo,
        BigDecimal vMinimo,
        BigDecimal phMinIdeal,
        BigDecimal phMaxIdeal,
        GrupoBotanico grupo) {

    public Cultura(String nome, BigDecimal vAlvo) {
        this(null, nome, vAlvo, null, null, null, null);
    }

    public boolean leguminosa() {
        return grupo == GrupoBotanico.LEGUMINOSA;
    }
}
