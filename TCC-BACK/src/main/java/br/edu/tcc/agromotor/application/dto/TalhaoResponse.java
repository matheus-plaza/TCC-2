package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.TexturaSolo;

import java.math.BigDecimal;

public record TalhaoResponse(
        Long id,
        String identificacao,
        BigDecimal areaHectares,
        TexturaSolo texturaPredominante,
        Long propriedadeId,
        String propriedadeNome,
        String municipio,
        String uf,
        Long produtorId,
        String produtorNome) {

    public static TalhaoResponse de(ContextoTalhao c) {
        return new TalhaoResponse(c.talhaoId(), c.talhaoIdentificacao(), c.areaHectares(), c.texturaPredominante(),
                c.propriedadeId(), c.propriedadeNome(), c.municipio(), c.uf(), c.produtorId(), c.produtorNome());
    }
}
