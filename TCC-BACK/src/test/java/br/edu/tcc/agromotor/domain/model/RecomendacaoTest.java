package br.edu.tcc.agromotor.domain.model;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecomendacaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 1, 10, 0);

    private final Recomendacao simulacao = Recomendacao.simulacao(
            new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(), null, new Cultura("Soja", new BigDecimal("70")),
                    null, SistemaManejo.PLANTIO_DIRETO, new BigDecimal("4.5"), new BigDecimal("100")),
            null, 7L, AGORA);

    @Test
    @DisplayName("Adotar muda o status e registra quem adotou, sem alterar o original")
    void adotar() {
        Recomendacao adotada = simulacao.adotar(9L, AGORA.plusDays(1));

        assertThat(adotada.status()).isEqualTo(StatusRecomendacao.ADOTADA);
        assertThat(adotada.adotadoPorId()).isEqualTo(9L);
        assertThat(adotada.dataAdocao()).isEqualTo(AGORA.plusDays(1));
        assertThat(adotada.dataGeracao()).isEqualTo(AGORA);
        assertThat(simulacao.status()).isEqualTo(StatusRecomendacao.SIMULACAO);
    }

    @Test
    @DisplayName("Uma recomendação adotada não pode ser adotada de novo")
    void naoAdotaDuasVezes() {
        Recomendacao adotada = simulacao.adotar(9L, AGORA);

        assertThatThrownBy(() -> adotada.adotar(9L, AGORA))
                .isInstanceOf(RegraNegocioException.class);
    }
}
