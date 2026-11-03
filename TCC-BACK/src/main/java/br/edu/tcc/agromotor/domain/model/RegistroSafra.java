package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * O que aconteceu no campo depois de uma recomendação adotada: a produtividade colhida
 * e o gasto com adubação. Não participa do cálculo das doses. O gasto pode ser informado
 * antes da colheita, por isso cada parte é opcional, mas uma delas precisa existir.
 */
public record RegistroSafra(
        Long id,
        Long recomendacaoId,
        BigDecimal produtividadeColhidaTonHa,
        LocalDate dataColheita,
        BigDecimal custoAdubacaoReais,
        Long registradoPorId,
        LocalDateTime atualizadoEm) {

    public RegistroSafra {
        new Validacao()
                .obrigatorio(recomendacaoId, "Recomendação")
                .verdadeiro(produtividadeColhidaTonHa != null || custoAdubacaoReais != null,
                        "Informe a colheita ou o gasto com adubação")
                .verdadeiro((produtividadeColhidaTonHa == null) == (dataColheita == null),
                        "Informe a produtividade colhida junto com a data da colheita")
                .positivo(produtividadeColhidaTonHa, "Produtividade colhida")
                .naoNegativo(custoAdubacaoReais, "Gasto com adubação")
                .lancarSeHouverErros();
    }

    public boolean temColheita() {
        return produtividadeColhidaTonHa != null;
    }

    /** Quanto a colheita ficou acima (positivo) ou abaixo (negativo) da produtividade esperada. */
    public BigDecimal diferencaPercentual(BigDecimal esperadaTonHa) {
        return temColheita() ? Decimais.percentual(produtividadeColhidaTonHa.subtract(esperadaTonHa), esperadaTonHa) : null;
    }

    public BigDecimal custoPorHectare(BigDecimal areaHectares) {
        return custoAdubacaoReais == null
                ? null
                : custoAdubacaoReais.divide(areaHectares, Decimais.CASAS_VALOR, RoundingMode.HALF_UP);
    }
}
