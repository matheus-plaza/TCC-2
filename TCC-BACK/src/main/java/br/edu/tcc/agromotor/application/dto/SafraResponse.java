package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.RegistroSafra;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comparação entre o planejado na recomendação adotada e o resultado de campo. Os campos
 * do resultado ficam nulos enquanto nada foi registrado.
 */
public record SafraResponse(
        Long recomendacaoId,
        Long talhaoId,
        String talhao,
        String propriedade,
        String cultura,
        LocalDate dataAdocao,
        BigDecimal produtividadeEsperadaTonHa,
        BigDecimal produtividadeColhidaTonHa,
        LocalDate dataColheita,
        BigDecimal diferencaPercentual,
        BigDecimal custoAdubacaoReais,
        BigDecimal custoPorHectareReais) {

    public static SafraResponse de(Recomendacao r, ContextoTalhao c, RegistroSafra s) {
        BigDecimal esperada = r.entrada().expectativaRendimentoTonHa();
        return new SafraResponse(r.id(), c.talhaoId(), c.talhaoIdentificacao(), c.propriedadeNome(),
                r.entrada().culturaAlvo().nome(), r.dataAdocao().toLocalDate(), esperada,
                s == null ? null : s.produtividadeColhidaTonHa(),
                s == null ? null : s.dataColheita(),
                s == null ? null : s.diferencaPercentual(esperada),
                s == null ? null : s.custoAdubacaoReais(),
                s == null ? null : s.custoPorHectare(c.areaHectares()));
    }
}
