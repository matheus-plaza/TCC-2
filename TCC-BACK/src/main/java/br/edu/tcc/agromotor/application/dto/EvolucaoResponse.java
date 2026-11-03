package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.MetodoPh;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Séries temporais para os gráficos de evolução do solo (UC10), em ordem cronológica. */
public record EvolucaoResponse(TalhaoResponse talhao, List<Ponto> pontos, List<Adotada> adotadas, List<SafraResponse> safras) {

    public record Ponto(LocalDate data, Long laudoId, BigDecimal ph, MetodoPh metodoPh, BigDecimal mo, BigDecimal p,
                        BigDecimal k, BigDecimal ca, BigDecimal mg, BigDecimal v, BigDecimal m, BigDecimal ctc) {
    }

    public record Adotada(LocalDate data, Long recomendacaoId, String cultura, BigDecimal calcarioTonHa,
                          BigDecimal gessoKgHa, FaixaDose nKgHa, FaixaDose p2o5KgHa, FaixaDose k2oKgHa) {
    }
}
