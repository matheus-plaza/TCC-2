package br.edu.tcc.agromotor.domain.model;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LaudoSoloTest {

    @Test
    @DisplayName("Calcula os indicadores do exemplo da Tela A")
    void indicadoresDoExemplo() {
        IndicadoresSolo i = LaudoSoloBuilder.umLaudo().build().indicadores();

        assertThat(i.somaBases()).isEqualByComparingTo("4.65");
        assertThat(i.ctcEfetiva()).isEqualByComparingTo("4.75");
        assertThat(i.ctcTotal()).isEqualByComparingTo("9.45");
        assertThat(i.saturacaoBases()).isEqualByComparingTo("49.2");
        assertThat(i.saturacaoAluminio()).isEqualByComparingTo("2.1");
    }

    @Test
    @DisplayName("Al³⁺ zero é aceito e resulta em m% zero")
    void aluminioZero() {
        IndicadoresSolo i = LaudoSoloBuilder.umLaudo().comAl(0).build().indicadores();

        assertThat(i.saturacaoAluminio()).isEqualByComparingTo("0");
        assertThat(i.ctcEfetiva()).isEqualByComparingTo(i.somaBases());
    }

    @ParameterizedTest
    @ValueSource(doubles = {3.0, 5.2, 9.0})
    @DisplayName("pH dentro de [3,0; 9,0] é aceito, inclusive nos limites")
    void phValido(double ph) {
        assertThatNoException().isThrownBy(() -> LaudoSoloBuilder.umLaudo().comPh(ph).build());
    }

    @ParameterizedTest
    @ValueSource(doubles = {2.99, 9.01})
    @DisplayName("pH fora de [3,0; 9,0] é rejeitado")
    void phInvalido(double ph) {
        assertThatThrownBy(() -> LaudoSoloBuilder.umLaudo().comPh(ph).build())
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("pH");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100.1})
    @DisplayName("Argila fora de [0; 100] é rejeitada")
    void argilaInvalida(double argila) {
        assertThatThrownBy(() -> LaudoSoloBuilder.umLaudo().comArgila(argila).build())
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Argila");
    }

    @Test
    @DisplayName("Teores zerados ou negativos são rejeitados, e todos os erros são reportados juntos")
    void teoresInvalidos() {
        assertThatThrownBy(() -> LaudoSoloBuilder.umLaudo().comP(0).comK(-1).comCa(0).comMg(0).comMo(0).comHAl(0).comAl(-0.1).build())
                .isInstanceOfSatisfying(RegraNegocioException.class, e -> assertThat(e.erros()).hasSize(7));
    }
}
