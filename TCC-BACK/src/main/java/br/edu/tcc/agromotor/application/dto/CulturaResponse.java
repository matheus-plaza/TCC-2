package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;

import java.math.BigDecimal;

public record CulturaResponse(
        Long id,
        String nome,
        BigDecimal vAlvo,
        BigDecimal vMinimo,
        BigDecimal phMinIdeal,
        BigDecimal phMaxIdeal,
        GrupoBotanico grupo) {

    public static CulturaResponse de(Cultura c) {
        return c == null ? null
                : new CulturaResponse(c.id(), c.nome(), c.vAlvo(), c.vMinimo(), c.phMinIdeal(), c.phMaxIdeal(), c.grupo());
    }
}
