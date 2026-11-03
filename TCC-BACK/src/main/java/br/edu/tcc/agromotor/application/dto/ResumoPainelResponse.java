package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.MetodoPh;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Indicadores do painel inicial, restritos aos dados que o usuário pode ver. */
public record ResumoPainelResponse(
        int propriedades,
        int talhoes,
        BigDecimal areaTotalHectares,
        int laudos,
        int recomendacoesAdotadas,
        int simulacoesSalvas,
        int colheitasRegistradas,
        List<LaudoRecente> laudosRecentes,
        List<RecomendacaoRecente> recomendacoesRecentes,
        List<SafraResponse> colheitasRecentes,
        List<TalhaoResponse> talhoesSemLaudo) {

    public record LaudoRecente(Long id, Long talhaoId, LocalDate data, String profundidadeCm, String talhao,
                               String propriedade, BigDecimal v, BigDecimal ph, MetodoPh metodoPh) {
    }

    public record RecomendacaoRecente(Long id, LocalDate data, String status, String cultura, String talhao,
                                      String propriedade, BigDecimal calcarioTonHa) {
    }
}
