package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.ParametrosEmMemoria;
import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoGessagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Critérios e doses da p. 56 do manual. */
class CalculadorGessagemTest {

    private final CalculadorGessagem calculador = new CalculadorGessagem();
    private final ParametrosEmMemoria parametros = new ParametrosEmMemoria().comGessoDoManual();

    private static LaudoSoloBuilder camadaSemRestricao() {
        // SB = 1,5, Al = 0,1 e Ca = 1,0: m% = 6,25, nenhum critério acionado
        return LaudoSoloBuilder.umLaudo().naCamada2040().comArgila(45).comCa(1.0).comMg(0.4).comK(0.1).comAl(0.1).comHAl(2.0);
    }

    @Test
    @DisplayName("m% acima do limite aciona a gessagem; dose pela faixa de argila")
    void acionaPorSaturacaoDeAluminio() {
        // Al = 0,5 e t = 2,0: m% = 25
        ResultadoGessagem r = calcular(camadaSemRestricao().comAl(0.5).build());

        assertThat(r.necessario()).isTrue();
        assertThat(r.doseKgHa()).isEqualByComparingTo("2200");
        assertThat(r.justificativa()).contains("m%");
    }

    @Test
    @DisplayName("m% exatamente no limite não aciona (a regra é maior que)")
    void saturacaoDeAluminioNoLimite() {
        // Al = 0,375 e t = 1,875: m% = 20 exatos
        assertThat(calcular(camadaSemRestricao().comAl(0.375).build()).necessario()).isFalse();
    }

    @Test
    @DisplayName("m% de 20,04 aciona, mesmo aparecendo como 20,0 depois de arredondado")
    void comparaSaturacaoExata() {
        LaudoSolo camada = camadaSemRestricao().comAl(0.376).build();

        assertThat(camada.indicadores().saturacaoAluminio()).isEqualByComparingTo("20.0");
        assertThat(calcular(camada).necessario()).isTrue();
    }

    @Test
    @DisplayName("Ca²⁺ abaixo do limite aciona a gessagem")
    void acionaPorCalcio() {
        ResultadoGessagem r = calcular(camadaSemRestricao().comCa(0.49).build());

        assertThat(r.necessario()).isTrue();
        assertThat(r.justificativa()).contains("Ca²⁺");
    }

    @Test
    @DisplayName("Ca²⁺ exatamente no limite não aciona")
    void calcioNoLimite() {
        assertThat(calcular(camadaSemRestricao().comCa(0.5).build()).necessario()).isFalse();
    }

    @Test
    @DisplayName("V% baixo, sozinho, não aciona: o manual não usa esse critério")
    void saturacaoDeBasesNaoEhCriterio() {
        // SB = 1 e T = 6: V% = 16,7; Ca = 0,6 e m% = 9,1
        LaudoSolo camada = camadaSemRestricao().comCa(0.6).comMg(0.3).comK(0.1).comHAl(5).build();

        assertThat(camada.indicadores().saturacaoBases()).isEqualByComparingTo("16.7");
        assertThat(calcular(camada).necessario()).isFalse();
    }

    @ParameterizedTest(name = "argila de {0}%: {1} kg/ha")
    @CsvSource({"0, 700", "20, 700", "20.01, 1200", "40, 1200", "40.01, 2200", "60, 2200", "60.01, 3200", "100, 3200"})
    @DisplayName("A dose segue a tabela por argila, com os limites na faixa de baixo")
    void doseConformeArgila(double argila, String dose) {
        assertThat(calcular(camadaSemRestricao().comArgila(argila).comAl(0.5).build()).doseKgHa()).isEqualByComparingTo(dose);
    }

    @Test
    @DisplayName("A dose usa a argila da camada de 20-40 cm, não a da camada de 0-20 cm")
    void usaArgilaDaCamada() {
        // O laudo de 0-20 cm tem argila de 45%
        assertThat(calcular(camadaSemRestricao().comArgila(62.5).comAl(0.5).build()).doseKgHa()).isEqualByComparingTo("3200");
    }

    @Test
    @DisplayName("Sem laudo de 20-40 cm a gessagem fica como não avaliada")
    void semLaudoDeSubsuperficie() {
        EntradaRecomendacao entrada = new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(), null,
                new Cultura("Soja", new BigDecimal("60")), null, SistemaManejo.PLANTIO_DIRETO,
                new BigDecimal("4.5"), new BigDecimal("100"));

        ResultadoGessagem r = calculador.calcular(entrada, parametros);

        assertThat(r.avaliada()).isFalse();
        assertThat(r.necessario()).isFalse();
        assertThat(r.doseKgHa()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Sem critérios cadastrados o motor aponta a pendência")
    void parametroAusente() {
        EntradaRecomendacao entrada = entrada(camadaSemRestricao().build());

        assertThatThrownBy(() -> calculador.calcular(entrada, new ParametrosEmMemoria()))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("gessagem");
    }

    @Test
    @DisplayName("Sem dose para a faixa de argila o motor aponta a pendência")
    void doseAusente() {
        EntradaRecomendacao entrada = entrada(camadaSemRestricao().comAl(0.5).build());

        assertThatThrownBy(() -> calculador.calcular(entrada, new ParametrosEmMemoria().comGessagem("20", "0.5")))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("Dose de gesso");
    }

    private ResultadoGessagem calcular(LaudoSolo camada) {
        return calculador.calcular(entrada(camada), parametros);
    }

    private static EntradaRecomendacao entrada(LaudoSolo camada) {
        return new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(), camada,
                new Cultura("Soja", new BigDecimal("60")), null, SistemaManejo.PLANTIO_DIRETO,
                new BigDecimal("4.5"), new BigDecimal("100"));
    }
}
