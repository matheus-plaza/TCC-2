package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;

public record Talhao(
        Long id,
        Long propriedadeId,
        String identificacao,
        BigDecimal areaHectares,
        TexturaSolo texturaPredominante) {

    public Talhao {
        new Validacao()
                .obrigatorio(propriedadeId, "Propriedade")
                .obrigatorio(identificacao, "Identificação do talhão")
                .obrigatorio(areaHectares, "Área")
                .positivo(areaHectares, "Área")
                .lancarSeHouverErros();
    }
}
