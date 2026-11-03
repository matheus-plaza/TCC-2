package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.ParametrosEmMemoria;
import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoMicronutriente;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadorMicronutrientesTest {

    private static final Cultura SOJA = new Cultura(1L, "Soja", new BigDecimal("60"), new BigDecimal("50"), null, null, GrupoBotanico.LEGUMINOSA);
    private static final Cultura MILHO = new Cultura(2L, "Milho", new BigDecimal("70"), new BigDecimal("60"), null, null, GrupoBotanico.GRAMINEA);

    private final CalculadorMicronutrientes calculador = new CalculadorMicronutrientes();

    /** Zinco e boro da soja (Tabelas 3.5 e 9.13). */
    private final ParametrosEmMemoria parametros = new ParametrosEmMemoria()
            .comFaixa(AtributoSolo.ZINCO, Intervalo.QUALQUER, "0", "0.4", "Muito baixo")
            .comFaixa(AtributoSolo.ZINCO, Intervalo.QUALQUER, "0.4", "0.9", "Baixo")
            .comFaixa(AtributoSolo.ZINCO, Intervalo.QUALQUER, "0.9", "1.3", "Médio")
            .comFaixa(AtributoSolo.ZINCO, Intervalo.QUALQUER, "1.3", "10.01", "Alto")
            .comMicro(1L, Micronutriente.ZINCO, "Muito baixo", "2.0", "A tabela do manual começa em Baixo; foi usada a dose dessa linha.")
            .comMicro(1L, Micronutriente.ZINCO, "Baixo", "2.0", null)
            .comMicro(1L, Micronutriente.ZINCO, "Médio", "1.5", null)
            .comMicro(1L, Micronutriente.ZINCO, "Alto", "0", null)
            .comMicro(1L, Micronutriente.BORO, "Baixo", "1.5", null);

    @Test
    @DisplayName("A dose depende da classe do teor no solo")
    void dosePelaClasse() {
        assertThat(zinco(0.8).doseKgHa()).isEqualByComparingTo("2.0");
        assertThat(zinco(0.8).classe()).isEqualTo("Baixo");
        assertThat(zinco(1.0).doseKgHa()).isEqualByComparingTo("1.5");
        assertThat(zinco(1.0).classe()).isEqualTo("Médio");
    }

    @Test
    @DisplayName("Da classe Alto em diante a dose é zero")
    void classeAltaZeraADose() {
        ResultadoMicronutriente zn = zinco(1.3);

        assertThat(zn.avaliado()).isTrue();
        assertThat(zn.classe()).isEqualTo("Alto");
        assertThat(zn.doseKgHa()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Muito baixo usa a dose de Baixo e registra a decisão na justificativa")
    void muitoBaixoUsaLinhaDeBaixo() {
        ResultadoMicronutriente zn = zinco(0.3);

        assertThat(zn.doseKgHa()).isEqualByComparingTo("2.0");
        assertThat(zn.justificativa()).contains("Muito baixo").contains("começa em Baixo");
    }

    @Test
    @DisplayName("Sem teor no laudo o nutriente previsto no manual fica como não avaliado")
    void semTeor() {
        ResultadoMicronutriente zn = calcular(LaudoSoloBuilder.umLaudo().build(), SOJA).getFirst();

        assertThat(zn.avaliado()).isFalse();
        assertThat(zn.previstoNoManual()).isTrue();
        assertThat(zn.doseKgHa()).isNull();
    }

    @Test
    @DisplayName("Cultura sem dose no manual fica como não avaliada, sem interromper o cálculo")
    void culturaSemDoseNoManual() {
        ResultadoMicronutriente zn = calcular(LaudoSoloBuilder.umLaudo().comZn(0.3).build(), MILHO).getFirst();

        assertThat(zn.nutriente()).isEqualTo(Micronutriente.ZINCO);
        assertThat(zn.avaliado()).isFalse();
        assertThat(zn.previstoNoManual()).isFalse();
        assertThat(zn.justificativa()).contains("não indica dose");
    }

    @Test
    @DisplayName("Teor sem linha para a classe é pendência de cadastro")
    void classeSemRegra() {
        EntradaRecomendacao entrada = entrada(LaudoSoloBuilder.umLaudo().comZn(11).build(), SOJA);
        ParametrosEmMemoria p = new ParametrosEmMemoria()
                .comFaixa(AtributoSolo.ZINCO, Intervalo.QUALQUER, "10.01", null, "Muito alto")
                .comMicro(1L, Micronutriente.ZINCO, "Baixo", "2.0", null);

        assertThatThrownBy(() -> calculador.calcular(entrada, p))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("classe Muito alto");
    }

    private ResultadoMicronutriente zinco(double teor) {
        return calcular(LaudoSoloBuilder.umLaudo().comZn(teor).build(), SOJA).getFirst();
    }

    private List<ResultadoMicronutriente> calcular(LaudoSolo laudo, Cultura cultura) {
        return calculador.calcular(entrada(laudo, cultura), parametros);
    }

    private static EntradaRecomendacao entrada(LaudoSolo laudo, Cultura cultura) {
        return new EntradaRecomendacao(laudo, null, cultura, null, SistemaManejo.PLANTIO_DIRETO,
                new BigDecimal("8"), new BigDecimal("100"));
    }
}
