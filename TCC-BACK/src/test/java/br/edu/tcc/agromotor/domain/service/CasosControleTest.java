package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.LaudoSoloBuilder;
import br.edu.tcc.agromotor.domain.ParametrosEmMemoria;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validação comparativa (seção 3.4 do TCC): compara a saída do motor com resultados
 * de referência obtidos fora do sistema. Cada linha de {@code casos-controle.csv} é
 * um caso; para validar com laudos reais anonimizados, basta acrescentar linhas com
 * os valores calculados à mão. Os casos cobrem as fórmulas e a tabela de gesso do manual.
 */
class CasosControleTest {

    /** Margem para resultados conferidos à mão com V1 arredondado. */
    private static final Offset<BigDecimal> TOLERANCIA = Offset.offset(new BigDecimal("0.01"));

    private final ParametrosEmMemoria parametros = new ParametrosEmMemoria().comGessoDoManual();

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/validacao/casos-controle.csv", delimiter = ';', numLinesToSkip = 1)
    void comparaComReferencia(String caso,
                              double ca, double mg, double k, double al, double hAl, String v2, String prnt,
                              double caSub, double mgSub, double kSub, double alSub, double hAlSub, double argilaSub,
                              String sb, String efetiva, String total, String v1, String m, String nc, String gesso) {
        LaudoSolo laudo = LaudoSoloBuilder.umLaudo().comCa(ca).comMg(mg).comK(k).comAl(al).comHAl(hAl).build();
        LaudoSolo camada = LaudoSoloBuilder.umLaudo().naCamada2040()
                .comCa(caSub).comMg(mgSub).comK(kSub).comAl(alSub).comHAl(hAlSub).comArgila(argilaSub).build();
        Cultura cultura = new Cultura("Controle", new BigDecimal(v2));

        IndicadoresSolo indicadores = laudo.indicadores();
        assertThat(indicadores.somaBases()).as("SB").isEqualByComparingTo(sb);
        assertThat(indicadores.ctcEfetiva()).as("t").isEqualByComparingTo(efetiva);
        assertThat(indicadores.ctcTotal()).as("T").isEqualByComparingTo(total);
        assertThat(indicadores.saturacaoBases()).as("V1%").isEqualByComparingTo(v1);
        assertThat(indicadores.saturacaoAluminio()).as("m%").isEqualByComparingTo(m);

        assertThat(new CalculadorCalagem().calcularNC(laudo, cultura, new BigDecimal(prnt)))
                .as("NC").isCloseTo(new BigDecimal(nc), TOLERANCIA);

        EntradaRecomendacao entrada = new EntradaRecomendacao(laudo, camada, cultura, null,
                SistemaManejo.PLANTIO_DIRETO, BigDecimal.ONE, new BigDecimal(prnt));
        assertThat(new CalculadorGessagem().calcular(entrada, parametros).doseKgHa())
                .as("NG").isEqualByComparingTo(gesso);
    }
}
