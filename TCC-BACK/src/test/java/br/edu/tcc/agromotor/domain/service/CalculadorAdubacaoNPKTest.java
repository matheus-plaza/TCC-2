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
import br.edu.tcc.agromotor.domain.model.NutrienteNpk;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoAdubacao;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static br.edu.tcc.agromotor.domain.ParametrosEmMemoria.faixa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadorAdubacaoNPKTest {

    private static final Cultura SOJA = new Cultura(1L, "Soja", new BigDecimal("60"), new BigDecimal("50"), null, null, GrupoBotanico.LEGUMINOSA);
    private static final Cultura MILHO = new Cultura(2L, "Milho", new BigDecimal("70"), new BigDecimal("60"), null, null, GrupoBotanico.GRAMINEA);
    private static final Cultura TRIGO = new Cultura(3L, "Trigo", new BigDecimal("70"), new BigDecimal("60"), null, null, GrupoBotanico.GRAMINEA);

    private final CalculadorAdubacaoNPK calculador = new CalculadorAdubacaoNPK();

    /** Trechos das tabelas do manual, só com as linhas usadas nos testes. */
    private static ParametrosEmMemoria tabelas() {
        return new ParametrosEmMemoria()
                // P cruzado com a argila (Tabela 3.3)
                .comFaixa(AtributoSolo.FOSFORO, faixa("40.01", null), "0", "3", "Muito baixo")
                .comFaixa(AtributoSolo.FOSFORO, faixa("40.01", null), "10", "12.01", "Alto")
                .comFaixa(AtributoSolo.FOSFORO, faixa("25", "40.01"), "9", "13", "Médio")
                // K pela porcentagem na CTC (Tabela 3.4)
                .comFaixa(AtributoSolo.POTASSIO, Intervalo.QUALQUER, "0", "0.5", "Muito baixo")
                .comFaixa(AtributoSolo.POTASSIO, Intervalo.QUALQUER, "2.1", "3.1", "Alto")
                .comFaixa(AtributoSolo.POTASSIO, Intervalo.QUALQUER, "3.1", "10.1", "Muito alto")
                .comFaixa(AtributoSolo.POTASSIO, Intervalo.QUALQUER, "10.1", null, "Condição a evitar")
                // M.O. (Tabela 3.2, em g/dm³)
                .comFaixa(AtributoSolo.MATERIA_ORGANICA, Intervalo.QUALQUER, "15", "25", "Médio")
                .comFaixa(AtributoSolo.MATERIA_ORGANICA, Intervalo.QUALQUER, "25", "34.01", "Alto")
                // Soja, 4,1 a 5 t/ha (Tabelas 9.11 e 9.12)
                .comRegraNpk(1L, NutrienteNpk.P2O5, "Muito baixo", null, "4.1", "5.01", null, null)
                .comRegraNpk(1L, NutrienteNpk.P2O5, "Médio", null, "4.1", "5.01", "81", "100")
                .comRegraNpk(1L, NutrienteNpk.P2O5, "Alto", null, "4.1", "5.01", "61", "80")
                .comRegraNpk(1L, NutrienteNpk.K2O, "Muito baixo", null, "4.1", "5.01", null, null)
                .comRegraNpk(1L, NutrienteNpk.K2O, "Alto", null, "4.1", "5.01", "71", "90")
                .comRegraNpk(1L, NutrienteNpk.K2O, "Muito alto", null, "4.1", "5.01", "50", "70")
                .comRegraNpk(1L, NutrienteNpk.K2O, "Condição a evitar", null, "4.1", "5.01", "0", "0")
                // Milho, 8 a 12 t/ha (Tabelas 8.21 a 8.23)
                .comRegraNpk(2L, NutrienteNpk.N, null, GrupoBotanico.GRAMINEA, "8", "13", "121", "180")
                .comRegraNpk(2L, NutrienteNpk.N, null, GrupoBotanico.LEGUMINOSA, "8", "13", "61", "120")
                .comRegraNpk(2L, NutrienteNpk.P2O5, "Alto", null, "8", "13", "71", "90")
                .comRegraNpk(2L, NutrienteNpk.K2O, "Muito alto", null, "8", "13", "21", "40")
                // Trigo, 3,6 a 4,5 t/ha (Tabelas 8.29 a 8.31)
                .comRegraNpk(3L, NutrienteNpk.N, "Médio", GrupoBotanico.GRAMINEA, "3.6", "4.51", "91", "120")
                .comRegraNpk(3L, NutrienteNpk.N, "Alto", GrupoBotanico.GRAMINEA, "3.6", "4.51", "71", "100")
                .comRegraNpk(3L, NutrienteNpk.N, "Alto", GrupoBotanico.LEGUMINOSA, "3.6", "4.51", "51", "80")
                .comRegraNpk(3L, NutrienteNpk.P2O5, "Alto", null, "3.6", "4.51", "41", "50")
                .comRegraNpk(3L, NutrienteNpk.K2O, "Muito alto", null, "3.6", "4.51", "21", "30");
    }

    private static FaixaDose dose(String min, String max) {
        return new FaixaDose(new BigDecimal(min), new BigDecimal(max));
    }

    @Test
    @DisplayName("Soja recebe N zero por FBN; P e K saem em faixa, cada um da sua classe")
    void sojaComClassesIndependentes() {
        // Laudo do exemplo: P = 12 com argila 45 (Alto); K = 0,35 com T = 9,45, 3,7% da CTC (Muito alto)
        ResultadoAdubacao r = calcular(LaudoSoloBuilder.umLaudo().build(), SOJA, MILHO, "4.5");

        assertThat(r.doseNkgHa()).isEqualTo(FaixaDose.zero());
        assertThat(r.doseP2O5kgHa()).isEqualTo(dose("61", "80"));
        assertThat(r.doseK2OkgHa()).isEqualTo(dose("50", "70"));
        assertThat(r.classeFosforo()).isEqualTo("Alto");
        assertThat(r.classePotassio()).isEqualTo("Muito alto");
        assertThat(r.justificativaN()).isEqualTo("FBN ativada. Antecessora Gramínea compensada.");
        assertThat(r.justificativaK()).contains("3,7%");
    }

    @Test
    @DisplayName("O mesmo teor de P muda de classe com a argila")
    void classeDePDependeDaArgila() {
        assertThat(calcular(LaudoSoloBuilder.umLaudo().comP(12).comArgila(45).build(), SOJA, MILHO, "4.5").classeFosforo())
                .isEqualTo("Alto");
        assertThat(calcular(LaudoSoloBuilder.umLaudo().comP(12).comArgila(30).build(), SOJA, MILHO, "4.5").classeFosforo())
                .isEqualTo("Médio");
    }

    @Test
    @DisplayName("O mesmo K muda de classe com a CTC, porque é enquadrado pela porcentagem na CTC")
    void classeDeKUsaPorcentagemNaCtc() {
        LaudoSolo ctcBaixa = LaudoSoloBuilder.umLaudo().build(); // T = 9,45: 3,7%
        LaudoSolo ctcAlta = LaudoSoloBuilder.umLaudo().comCa(6).comMg(2).comHAl(6.65).build(); // T = 15: 2,3%

        assertThat(calcular(ctcBaixa, SOJA, MILHO, "4.5").classePotassio()).isEqualTo("Muito alto");
        assertThat(calcular(ctcAlta, SOJA, MILHO, "4.5").classePotassio()).isEqualTo("Alto");
    }

    @Test
    @DisplayName("K acima de 10% da CTC é Condição a evitar, com dose zero")
    void potassioEmCondicaoAEvitar() {
        // K = 1,2 e T = 10: 12%
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comK(1.2).comCa(3).comMg(0.8).comHAl(5).build();

        ResultadoAdubacao r = calcular(laudo, SOJA, MILHO, "4.5");

        assertThat(r.classePotassio()).isEqualTo("Condição a evitar");
        assertThat(r.doseK2OkgHa()).isEqualTo(FaixaDose.zero());
    }

    @Test
    @DisplayName("Milho após leguminosa recebe a faixa menor de N: crédito")
    void creditoDeNitrogenio() {
        ResultadoAdubacao r = calcular(LaudoSoloBuilder.umLaudo().build(), MILHO, SOJA, "9.5");

        assertThat(r.doseNkgHa()).isEqualTo(dose("61", "120"));
        assertThat(r.justificativaN()).contains("crédito").contains("Soja");
    }

    @Test
    @DisplayName("Milho após gramínea recebe a faixa maior de N: débito")
    void debitoDeNitrogenio() {
        ResultadoAdubacao r = calcular(LaudoSoloBuilder.umLaudo().build(), MILHO, TRIGO, "9.5");

        assertThat(r.doseNkgHa()).isEqualTo(dose("121", "180"));
        assertThat(r.justificativaN()).contains("débito").contains("Trigo");
    }

    @Test
    @DisplayName("No trigo, o N depende também da classe de M.O.")
    void trigoUsaMateriaOrganica() {
        assertThat(calcular(LaudoSoloBuilder.umLaudo().build(), TRIGO, MILHO, "4").doseNkgHa())
                .isEqualTo(dose("71", "100")); // M.O. 28,5: Alto
        assertThat(calcular(LaudoSoloBuilder.umLaudo().comMo(20).build(), TRIGO, MILHO, "4").doseNkgHa())
                .isEqualTo(dose("91", "120")); // M.O. 20: Médio
        assertThat(calcular(LaudoSoloBuilder.umLaudo().build(), TRIGO, SOJA, "4").doseNkgHa())
                .isEqualTo(dose("51", "80")); // Alto, após leguminosa
    }

    @Test
    @DisplayName("Milho e trigo sem cultura anterior são recusados: o manual não traz essa linha")
    void pousioRecusadoEmGramineas() {
        assertThatThrownBy(() -> calcular(LaudoSoloBuilder.umLaudo().build(), MILHO, null, "9.5"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("sem cultura anterior");
    }

    @Test
    @DisplayName("Soja sem cultura anterior segue normalmente")
    void pousioAceitoNaSoja() {
        ResultadoAdubacao r = calcular(LaudoSoloBuilder.umLaudo().build(), SOJA, null, "4.5");

        assertThat(r.doseNkgHa()).isEqualTo(FaixaDose.zero());
        assertThat(r.justificativaN()).contains("pousio");
    }

    @Test
    @DisplayName("Célula Inviável recusa o cenário e reúne os nutrientes afetados")
    void celulaInviavel() {
        // P = 2 com argila 45: Muito baixo; K = 0,03 com T = 9,13: 0,3%, Muito baixo
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comP(2).comK(0.03).build();

        assertThatThrownBy(() -> calcular(laudo, SOJA, MILHO, "4.5"))
                .isInstanceOfSatisfying(RegraNegocioException.class, e -> assertThat(e.erros())
                        .hasSize(2)
                        .allMatch(m -> m.contains("inviável"))
                        .anyMatch(m -> m.contains("P Muito baixo"))
                        .anyMatch(m -> m.contains("K Muito baixo")));
    }

    @Test
    @DisplayName("Os limites das colunas de produtividade seguem o livro: 5 t/ha fica em 4,1 a 5")
    void limiteDaFaixaDeRendimento() {
        assertThat(calcular(LaudoSoloBuilder.umLaudo().build(), SOJA, MILHO, "5").doseP2O5kgHa()).isEqualTo(dose("61", "80"));

        EntradaRecomendacao acima = entrada(LaudoSoloBuilder.umLaudo().build(), SOJA, MILHO, "5.01");
        assertThatThrownBy(() -> calculador.calcular(acima, tabelas()))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("rendimento de 5,01 t/ha");
    }

    @Test
    @DisplayName("Reúne todas as pendências em vez de parar na primeira")
    void acumulaPendencias() {
        EntradaRecomendacao entrada = entrada(LaudoSoloBuilder.umLaudo().build(), MILHO, SOJA, "9.5");

        assertThatThrownBy(() -> calculador.calcular(entrada, new ParametrosEmMemoria()))
                .isInstanceOfSatisfying(ParametroAusenteException.class, e -> assertThat(e.pendencias())
                        .hasSize(3)
                        .anyMatch(p -> p.contains("Fósforo"))
                        .anyMatch(p -> p.contains("Potássio"))
                        .anyMatch(p -> p.contains("Matéria orgânica")));
    }

    @Test
    @DisplayName("Faixas sobrepostas são erro de cadastro, não um cálculo silencioso")
    void faixasSobrepostas() {
        ParametrosEmMemoria p = tabelas().comFaixa(AtributoSolo.FOSFORO, faixa("40.01", null), "11", "20", "Muito alto");
        EntradaRecomendacao entrada = entrada(LaudoSoloBuilder.umLaudo().build(), SOJA, MILHO, "4.5");

        assertThatThrownBy(() -> calculador.calcular(entrada, p))
                .isInstanceOf(ParametroAusenteException.class)
                .hasMessageContaining("sobrepostas");
    }

    private ResultadoAdubacao calcular(LaudoSolo laudo, Cultura alvo, Cultura anterior, String rendimento) {
        return calculador.calcular(entrada(laudo, alvo, anterior, rendimento), tabelas());
    }

    private static EntradaRecomendacao entrada(LaudoSolo laudo, Cultura alvo, Cultura anterior, String rendimento) {
        return new EntradaRecomendacao(laudo, null, alvo, anterior, SistemaManejo.PLANTIO_DIRETO,
                new BigDecimal(rendimento), new BigDecimal("100"));
    }
}
