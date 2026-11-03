package br.edu.tcc.agromotor.domain.model;

import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;

import java.time.LocalDateTime;

/**
 * Recomendação registrada. Guarda o cenário e o resultado do momento em que foi
 * gerada, de modo que mudanças posteriores nas tabelas não alteram o histórico.
 */
public record Recomendacao(
        Long id,
        EntradaRecomendacao entrada,
        ResultadoRecomendacao resultado,
        StatusRecomendacao status,
        LocalDateTime dataGeracao,
        Long geradoPorId,
        Long adotadoPorId,
        LocalDateTime dataAdocao) {

    public static Recomendacao simulacao(EntradaRecomendacao entrada, ResultadoRecomendacao resultado,
                                         Long geradoPorId, LocalDateTime agora) {
        return new Recomendacao(null, entrada, resultado, StatusRecomendacao.SIMULACAO, agora, geradoPorId, null, null);
    }

    /** Transição SIMULACAO → ADOTADA (RF06). Uma recomendação adotada não muda mais. */
    public Recomendacao adotar(Long agronomoId, LocalDateTime agora) {
        if (adotada()) {
            throw new RegraNegocioException("Esta recomendação já foi adotada");
        }
        return new Recomendacao(id, entrada, resultado, StatusRecomendacao.ADOTADA, dataGeracao, geradoPorId,
                agronomoId, agora);
    }

    public boolean adotada() {
        return status == StatusRecomendacao.ADOTADA;
    }
}
