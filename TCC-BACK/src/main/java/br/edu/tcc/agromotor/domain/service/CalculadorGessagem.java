package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.Decimais;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoGessagem;
import br.edu.tcc.agromotor.domain.model.parametro.DoseGesso;
import br.edu.tcc.agromotor.domain.model.parametro.RegraGessagem;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static br.edu.tcc.agromotor.domain.model.Decimais.formatar;
import static br.edu.tcc.agromotor.domain.model.Decimais.formatarPercentual;

/**
 * Gessagem avaliada na camada de 20-40 cm (manual, p. 56). Indicada se m% &gt; limite
 * ou Ca²⁺ &lt; limite. A dose vem da tabela por faixa de argila dessa camada.
 */
public final class CalculadorGessagem implements CalculadorAgronomico<ResultadoGessagem> {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public ResultadoGessagem calcular(EntradaRecomendacao entrada, ParametroRepository parametros) {
        LaudoSolo camada = entrada.laudoSubsuperficie();
        BigDecimal zero = BigDecimal.ZERO.setScale(Decimais.CASAS_VALOR);
        if (camada == null) {
            return new ResultadoGessagem(false, false, zero,
                    "Gessagem não avaliada: o talhão não tem laudo da camada de 20-40 cm.", List.of());
        }

        RegraGessagem regra = parametros.regraGessagem()
                .orElseThrow(() -> new ParametroAusenteException("Critérios da gessagem"));
        IndicadoresSolo indicadores = camada.indicadores();
        List<String> motivos = new ArrayList<>();
        if (camada.saturacaoAluminioAcimaDe(regra.limiteMPorcentagem())) {
            motivos.add("m%% %s > %s".formatted(
                    formatarPercentual(indicadores.saturacaoAluminio()), formatarPercentual(regra.limiteMPorcentagem())));
        }
        if (Decimais.menorQue(camada.caCmolc(), regra.limiteCaCmolc())) {
            motivos.add("Ca²⁺ %s < %s cmolc/dm³".formatted(formatar(camada.caCmolc()), formatar(regra.limiteCaCmolc())));
        }

        String dadosCamada = "laudo 20-40 cm de %s (m%% %s, Ca²⁺ %s)".formatted(
                camada.dataAnalise().format(DATA), formatarPercentual(indicadores.saturacaoAluminio()), formatar(camada.caCmolc()));
        if (motivos.isEmpty()) {
            return new ResultadoGessagem(true, false, zero,
                    "Gessagem não necessária: nenhum critério acionado na camada de 20-40 cm.",
                    List.of("Gessagem: não indicada pelo " + dadosCamada));
        }

        BigDecimal dose = doseDaArgila(camada.argilaPorcentagem(), parametros);
        return new ResultadoGessagem(true, true, dose,
                "Indicada na camada de 20-40 cm: " + String.join("; ", motivos) + ".",
                List.of("Gessagem: indicada pelo " + dadosCamada,
                        "Gesso: argila de %s na camada = %s kg/ha".formatted(
                                formatarPercentual(camada.argilaPorcentagem()), formatar(dose))));
    }

    private static BigDecimal doseDaArgila(BigDecimal argila, ParametroRepository parametros) {
        List<DoseGesso> aplicaveis = parametros.dosesGesso().stream()
                .filter(d -> d.argila().contem(argila))
                .toList();
        if (aplicaveis.size() != 1) {
            String problema = aplicaveis.isEmpty() ? "Dose de gesso" : "Doses sobrepostas de gesso";
            throw new ParametroAusenteException("%s para argila de %s".formatted(problema, formatarPercentual(argila)));
        }
        return Decimais.valor(aplicaveis.getFirst().doseKgHa());
    }
}
