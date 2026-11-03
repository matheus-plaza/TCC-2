package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.MetodoPh;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Linha do tempo do talhão (RF07), do evento mais recente para o mais antigo. */
public record HistoricoTalhaoResponse(TalhaoResponse talhao, List<Evento> eventos) {

    public enum TipoEvento {
        LAUDO,
        RECOMENDACAO_ADOTADA,
        COLHEITA
    }

    public record Evento(TipoEvento tipo, LocalDate data, Long referenciaId, String titulo, String detalhe,
                         LaudoResumo laudo, DosesResumo doses, SafraResponse safra) {
    }

    public record LaudoResumo(String profundidadeCm, BigDecimal ph, MetodoPh metodoPh, BigDecimal v, BigDecimal m,
                              BigDecimal mo, BigDecimal p, BigDecimal k) {
    }

    public record DosesResumo(String cultura, BigDecimal calcarioTonHa, BigDecimal gessoKgHa, FaixaDose nKgHa,
                              FaixaDose p2o5KgHa, FaixaDose k2oKgHa) {
    }
}
