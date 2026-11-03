package br.edu.tcc.agromotor.application.dto;

import java.time.LocalDateTime;

/** Última cultura adotada no talhão, pré-selecionada como antecessora na safra seguinte. */
public record SugestaoAntecessoraResponse(CulturaResponse cultura, Long recomendacaoId, LocalDateTime dataAdocao) {
}
