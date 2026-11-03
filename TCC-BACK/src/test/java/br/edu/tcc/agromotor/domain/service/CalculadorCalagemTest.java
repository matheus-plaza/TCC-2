package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.ParametrosEmMemoria;
import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoCalagem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadorCalagemTest {

    /** Soja no manual (p. 120): elevar para 60% sempre que estiver abaixo de 50%. */
    private static final Cultura SOJA = new Cultura(1L, "Soja", new BigDecimal("60.0"), new BigDecimal("50.0"),
            new BigDecimal("5.0"), new BigDecimal("5.5"), GrupoBotanico.LEGUMINOSA);
    private static final Cultura V2_70 = new Cultura("Controle", new BigDecimal("70.0"));

    private final CalculadorCalagem calculador = new CalculadorCalagem();
    private final ParametrosEmMemoria parametros = new ParametrosEmMemoria()
            .comRegraCalagem(SistemaManejo.PLANTIO_DIRETO, "Aplicar em superfície.")
            .comRegraCalagem(SistemaManejo.CONVENCIONAL, "Incorporar o calcário.");

    /** Teste de unidade da seção 10 da especificação: verifica a fórmula, com o V2 que recebe. */
    @Test
    @DisplayName("Deve calcular a necessidade de calagem com precisão para cultura da Soja")
    void deveCalcularNecessidadeDeCalagemComSucesso() {
        // Arrange (Laudo com V1 = 49.2%, CTC Total T = 9.45, V2 Alvo Soja = 70%)
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo()
                .comCa(3.2).comMg(1.1).comK(0.35)
                .comHAl(4.8)
                .build();

        Cultura soja = new Cultura("Soja", new BigDecimal("70.0"));

        // Act
        CalculadorCalagem calculador = new CalculadorCalagem();
        BigDecimal nc = calculador.calcularNC(laudo, soja, new BigDecimal("100.0"));

        // Assert (NC esperado = (70 - 49.2) * 9.45 / 100 = 1.9656 -> 1.97 ton/ha)
        Assertions.assertThat(nc).isEqualByComparingTo(new BigDecimal("1.97"));
    }

    @Test
    @DisplayName("NC exata de 1,965 t/ha arredonda para cima, sem erro de representação")
    void naoSofreErroDeRepresentacaoNoArredondamento() {
        // V1 é uma dízima (49,2063...), mas V2 × T − 100 × SB = 196,5 é exato
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(3.2).comMg(1.1).comK(0.35).comHAl(4.8).build();
        assertThat(calculador.calcularNC(laudo, V2_70, new BigDecimal("100"))).isEqualByComparingTo("1.97");
    }

    @Test
    @DisplayName("PRNT menor exige mais calcário")
    void ajustaDosePeloPrnt() {
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(3.2).comMg(1.1).comK(0.35).comHAl(4.8).build();
        // 196,5 / 80 = 2,45625
        assertThat(calculador.calcularNC(laudo, V2_70, new BigDecimal("80"))).isEqualByComparingTo("2.46");
    }

    @Test
    @DisplayName("V1 igual ou acima do V2 não gera dose na fórmula")
    void formulaZeraQuandoV1AtingeV2() {
        // SB = 7, T = 10: V1 = 70% exatos
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(5).comMg(1.5).comK(0.5).comHAl(3).build();
        assertThat(calculador.calcularNC(laudo, V2_70, new BigDecimal("100"))).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("PRNT zero é rejeitado")
    void rejeitaPrntZero() {
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().build();
        assertThatThrownBy(() -> calculador.calcularNC(laudo, V2_70, BigDecimal.ZERO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("PRNT");
    }

    @Test
    @DisplayName("V1 abaixo do limite da cultura: dose pela fórmula, com justificativa e forma de aplicação")
    void resultadoCompleto() {
        // V1 = 49,2%, abaixo de 50%: NC = (60 × 9,45 − 100 × 4,65) / 100 = 1,02
        ResultadoCalagem r = calculador.calcular(entrada(LaudoSoloBuilder.umLaudo().build(), SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(r.necessario()).isTrue();
        assertThat(r.necessidadeCalcarioTonHa()).isEqualByComparingTo("1.02");
        assertThat(r.ncCalculadaTonHa()).isEqualByComparingTo("1.02");
        assertThat(r.v2AlvoPorcentagem()).isEqualByComparingTo("60.0");
        assertThat(r.justificativa()).contains("49,2%").contains("50,0%").contains("60,0%");
        assertThat(r.observacao()).isEqualTo("Aplicar em superfície.");
    }

    @Test
    @DisplayName("V1 entre o limite e o V2: o manual não indica calcário, embora a fórmula dê valor positivo")
    void acimaDoLimiteNaoRecebeDose() {
        // SB = 5,5 e T = 10: V1 = 55%, acima de 50% e abaixo de 60%
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(4).comMg(1.2).comK(0.3).comHAl(4.5).build();

        ResultadoCalagem r = calculador.calcular(entrada(laudo, SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(r.ncCalculadaTonHa()).isEqualByComparingTo("0.50");
        assertThat(r.necessidadeCalcarioTonHa()).isEqualByComparingTo("0");
        assertThat(r.necessario()).isFalse();
        assertThat(r.observacao()).contains("não necessária");
        assertThat(r.memoria()).anyMatch(l -> l.contains("não está abaixo do limite"));
    }

    @Test
    @DisplayName("V1 exatamente no limite não aciona: a regra é estar abaixo dele")
    void noLimiteNaoAciona() {
        // SB = 5 e T = 10: V1 = 50% exatos
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(3.5).comMg(1.2).comK(0.3).comHAl(5).build();

        ResultadoCalagem r = calculador.calcular(entrada(laudo, SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(r.necessidadeCalcarioTonHa()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("O limite é comparado com o V1 exato, sem o arredondamento da apresentação")
    void comparaLimiteExato() {
        // SB = 4,998 e T = 10: V1 = 49,98%, que aparece como 50,0%
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(3.498).comMg(1.2).comK(0.3).comHAl(5.002).build();

        ResultadoCalagem r = calculador.calcular(entrada(laudo, SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(laudo.indicadores().saturacaoBases()).isEqualByComparingTo("50.0");
        assertThat(r.necessidadeCalcarioTonHa()).isPositive();
    }

    @Test
    @DisplayName("Não há teto: a dose calculada é a recomendada nos dois manejos")
    void semTetoPorManejo() {
        // SB = 2, T = 12: NC = (60 × 12 − 200) / 100 = 5,20 t/ha
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(1.2).comMg(0.6).comK(0.2).comHAl(10).build();

        assertThat(calculador.calcular(entrada(laudo, SistemaManejo.PLANTIO_DIRETO), parametros).necessidadeCalcarioTonHa())
                .isEqualByComparingTo("5.20");
        ResultadoCalagem convencional = calculador.calcular(entrada(laudo, SistemaManejo.CONVENCIONAL), parametros);
        assertThat(convencional.necessidadeCalcarioTonHa()).isEqualByComparingTo("5.20");
        assertThat(convencional.observacao()).isEqualTo("Incorporar o calcário.");
    }

    @Test
    @DisplayName("pH abaixo do ideal com V1 acima do limite sinaliza a condição, sem inventar dose")
    void phBaixoSemDeficitDeBases() {
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comPh(4.8).comCa(6).comMg(1.5).comK(0.5).comHAl(2).build();

        ResultadoCalagem r = calculador.calcular(entrada(laudo, SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(r.necessario()).isTrue();
        assertThat(r.necessidadeCalcarioTonHa()).isEqualByComparingTo("0");
        assertThat(r.justificativa()).contains("pH").contains("Avaliar tecnicamente");
    }

    @Test
    @DisplayName("pH em água não dispara o aviso de pH ideal, que o manual só dá em CaCl₂")
    void phEmAguaSemComparacaoComOIdeal() {
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comPhEmAgua(4.8).comCa(6).comMg(1.5).comK(0.5).comHAl(2).build();

        ResultadoCalagem r = calculador.calcular(entrada(laudo, SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(r.necessario()).isFalse();
        assertThat(r.necessidadeCalcarioTonHa()).isEqualByComparingTo("0");
        assertThat(r.justificativa()).doesNotContain("pH");
    }

    @Test
    @DisplayName("O método do pH não muda a dose de calcário")
    void metodoDoPhNaoMudaADose() {
        ResultadoCalagem emCacl2 = calculador.calcular(
                entrada(LaudoSoloBuilder.umLaudo().comPh(5.2).build(), SistemaManejo.PLANTIO_DIRETO), parametros);
        ResultadoCalagem emAgua = calculador.calcular(
                entrada(LaudoSoloBuilder.umLaudo().comPhEmAgua(5.2).build(), SistemaManejo.PLANTIO_DIRETO), parametros);

        assertThat(emAgua.necessidadeCalcarioTonHa()).isEqualByComparingTo(emCacl2.necessidadeCalcarioTonHa());
        assertThat(emAgua.ncCalculadaTonHa()).isEqualByComparingTo(emCacl2.ncCalculadaTonHa());
    }

    @Test
    @DisplayName("Sem o limite de V% da cultura, o cálculo aponta a pendência")
    void limiteAusente() {
        EntradaRecomendacao entrada = new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(), null, V2_70, null,
                SistemaManejo.PLANTIO_DIRETO, new BigDecimal("4.5"), new BigDecimal("100"));

        assertThatThrownBy(() -> calculador.calcular(entrada, parametros))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("Limite de V%");
    }

    @Test
    @DisplayName("Sem regra para o manejo, o cálculo aponta a pendência")
    void regraAusente() {
        ParametrosEmMemoria vazio = new ParametrosEmMemoria();
        EntradaRecomendacao entrada = entrada(LaudoSoloBuilder.umLaudo().build(), SistemaManejo.PLANTIO_DIRETO);

        assertThatThrownBy(() -> calculador.calcular(entrada, vazio))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("Regra de calagem para Plantio Direto");
    }

    private static EntradaRecomendacao entrada(LaudoSolo laudo, SistemaManejo manejo) {
        return new EntradaRecomendacao(laudo, null, SOJA, null, manejo, new BigDecimal("4.5"), new BigDecimal("100.0"));
    }
}
