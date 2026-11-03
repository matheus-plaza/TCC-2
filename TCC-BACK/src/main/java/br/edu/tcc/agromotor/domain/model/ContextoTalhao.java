package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;

/** Talhão com a identificação de sua propriedade e de seu produtor. */
public record ContextoTalhao(
        Long talhaoId,
        String talhaoIdentificacao,
        BigDecimal areaHectares,
        TexturaSolo texturaPredominante,
        Long propriedadeId,
        String propriedadeNome,
        String municipio,
        String uf,
        Long produtorId,
        String produtorNome) {
}
