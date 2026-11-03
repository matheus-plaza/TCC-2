package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.ParametrosEmMemoria;
import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.NutrienteNpk;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static br.edu.tcc.agromotor.domain.ParametrosEmMemoria.faixa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EngineCalculoAgronomicoTest {

    private static final Cultura SOJA = new Cultura(1L, "Soja", new BigDecimal("60.0"), new BigDecimal("50.0"),
            new BigDecimal("5.0"), new BigDecimal("5.5"), GrupoBotanico.LEGUMINOSA);
    private static final Cultura MILHO = new Cultura(2L, "Milho", new BigDecimal("70.0"), new BigDecimal("60.0"),
            new BigDecimal("5.0"), new BigDecimal("5.5"), GrupoBotanico.GRAMINEA);

    private static ParametrosEmMemoria tabelas() {
        return new ParametrosEmMemoria()
                .comFaixa(AtributoSolo.FOSFORO, faixa("40.01", null), "10", "12.01", "Alto")
                .comFaixa(AtributoSolo.POTASSIO, Intervalo.QUALQUER, "3.1", "10.1", "Muito alto")
                .comFaixa(AtributoSolo.MATERIA_ORGANICA, Intervalo.QUALQUER, "25", "34.01", "Alto")
                .comRegraNpk(1L, NutrienteNpk.P2O5, "Alto", null, "4.1", "5.01", "61", "80")
                .comRegraNpk(1L, NutrienteNpk.K2O, "Muito alto", null, "4.1", "5.01", "50", "70")
                .comRegraNpk(2L, NutrienteNpk.N, null, GrupoBotanico.LEGUMINOSA, "0", "8", "20", "60")
                .comRegraNpk(2L, NutrienteNpk.P2O5, "Alto", null, "0", "8", null, null)
                .comRegraNpk(2L, NutrienteNpk.K2O, "Muito alto", null, "0", "8", "20", "20")
                .comMicro(1L, Micronutriente.ZINCO, "Baixo", "2.0", null)
                .comRegraCalagem(SistemaManejo.PLANTIO_DIRETO, "Aplicar em superfície.")
                .comGessoDoManual();
    }

    private static EntradaRecomendacao cenarioDoLaudoDeExemplo(LaudoSolo subsuperficie) {
        return new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(), subsuperficie, SOJA, MILHO,
                SistemaManejo.PLANTIO_DIRETO, new BigDecimal("4.5"), new BigDecimal("100.0"));
    }

    @Test
    @DisplayName("Laudo da Tela A com a soja do manual: calagem e doses em faixa")
    void cenarioDoExemplo() {
        ResultadoRecomendacao r = new EngineCalculoAgronomico(tabelas()).processar(cenarioDoLaudoDeExemplo(null));

        assertThat(r.indicadores().somaBases()).isEqualByComparingTo("4.65");
        assertThat(r.indicadores().ctcTotal()).isEqualByComparingTo("9.45");
        assertThat(r.indicadores().saturacaoBases()).isEqualByComparingTo("49.2");
        assertThat(r.calagem().necessario()).isTrue();
        assertThat(r.calagem().necessidadeCalcarioTonHa()).isEqualByComparingTo("1.02");
        assertThat(r.calagem().observacao()).isEqualTo("Aplicar em superfície.");
        assertThat(r.gessagem().necessario()).isFalse();
        assertThat(r.gessagem().doseKgHa()).isEqualByComparingTo("0");
        assertThat(r.adubacao().doseNkgHa()).isEqualTo(FaixaDose.zero());
        assertThat(r.adubacao().doseP2O5kgHa()).isEqualTo(new FaixaDose(new BigDecimal("61"), new BigDecimal("80")));
        assertThat(r.adubacao().doseK2OkgHa()).isEqualTo(new FaixaDose(new BigDecimal("50"), new BigDecimal("70")));
        assertThat(r.versaoParametros().codigo()).isEqualTo("TESTE");
    }

    @Test
    @DisplayName("Aciona a gessagem com o laudo de 20-40 cm do mesmo cenário")
    void comLaudoDeSubsuperficie() {
        // SB = 4,65, Al = 2 e t = 6,65: m% = 30,1; argila 50%: 2.200 kg/ha
        LaudoSolo camada = LaudoSoloBuilder.umLaudo().naCamada2040().comArgila(50).comAl(2).build();

        ResultadoRecomendacao r = new EngineCalculoAgronomico(tabelas()).processar(cenarioDoLaudoDeExemplo(camada));

        assertThat(r.gessagem().necessario()).isTrue();
        assertThat(r.gessagem().doseKgHa()).isEqualByComparingTo("2200");
    }

    @Test
    @DisplayName("Memória de cálculo registra as etapas para auditoria")
    void memoriaDeCalculo() {
        ResultadoRecomendacao r = new EngineCalculoAgronomico(tabelas()).processar(cenarioDoLaudoDeExemplo(null));

        assertThat(r.memoriaCalculo())
                .anyMatch(l -> l.startsWith("SB = Ca + Mg + K"))
                .anyMatch(l -> l.startsWith("T = SB + (H+Al)"))
                .anyMatch(l -> l.startsWith("K na CTC"))
                .anyMatch(l -> l.startsWith("NC = (V2 − V1) × T / PRNT"))
                .anyMatch(l -> l.startsWith("P₂O₅:") && l.contains("61,00 a 80,00 kg/ha"))
                .anyMatch(l -> l.startsWith("K₂O:") && l.contains("50,00 a 70,00 kg/ha"));
    }

    @Test
    @DisplayName("Avisa do teor que falta para um nutriente previsto no manual")
    void avisaTeorFaltante() {
        ResultadoRecomendacao r = new EngineCalculoAgronomico(tabelas()).processar(cenarioDoLaudoDeExemplo(null));

        assertThat(r.avisos()).contains("Zinco não informado no laudo.");
        assertThat(r.avisos()).noneMatch(a -> a.contains("Boro"));
    }

    @Test
    @DisplayName("Célula Inviável interrompe o cálculo com a explicação")
    void celulaInviavelRecusaOCenario() {
        EntradaRecomendacao entrada = new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(), null, MILHO, SOJA,
                SistemaManejo.PLANTIO_DIRETO, new BigDecimal("7"), new BigDecimal("100"));

        assertThatThrownBy(() -> new EngineCalculoAgronomico(tabelas()).processar(entrada))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inviável");
    }

    @Test
    @DisplayName("Reúne as pendências de todos os cálculos em uma única exceção")
    void reunePendenciasDeTodosOsCalculos() {
        EntradaRecomendacao entrada = new EntradaRecomendacao(LaudoSoloBuilder.umLaudo().build(),
                LaudoSoloBuilder.umLaudo().naCamada2040().build(), MILHO, SOJA, SistemaManejo.PLANTIO_DIRETO,
                new BigDecimal("4.5"), new BigDecimal("100"));

        assertThatThrownBy(() -> new EngineCalculoAgronomico(new ParametrosEmMemoria().semVersao()).processar(entrada))
                .isInstanceOfSatisfying(ParametroAusenteException.class, e -> assertThat(e.pendencias())
                        .anyMatch(p -> p.contains("Versão vigente"))
                        .anyMatch(p -> p.contains("Regra de calagem"))
                        .anyMatch(p -> p.contains("gessagem"))
                        .anyMatch(p -> p.contains("Fósforo")));
    }

    @Test
    @DisplayName("Recomendação é calculada sobre o laudo de 0-20 cm")
    void recusaLaudoDe20a40ComoBase() {
        LaudoSolo camada2040 = LaudoSoloBuilder.umLaudo().naCamada2040().build();

        assertThatThrownBy(() -> new EntradaRecomendacao(camada2040, null, SOJA, MILHO,
                SistemaManejo.PLANTIO_DIRETO, new BigDecimal("4.5"), new BigDecimal("100")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("0-20 cm");
    }

    @Test
    @DisplayName("Motor não altera o laudo recebido")
    void naoAlteraEntrada() {
        EntradaRecomendacao entrada = cenarioDoLaudoDeExemplo(null);
        LaudoSolo antes = entrada.laudo();

        new EngineCalculoAgronomico(tabelas()).processar(entrada);

        assertThat(entrada.laudo()).isSameAs(antes).isEqualTo(LaudoSoloBuilder.umLaudo().build());
    }
}
