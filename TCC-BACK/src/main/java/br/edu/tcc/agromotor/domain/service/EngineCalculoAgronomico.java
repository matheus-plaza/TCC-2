package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.VersaoParametros;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoAdubacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoCalagem;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoGessagem;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoMicronutriente;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static br.edu.tcc.agromotor.domain.model.Decimais.formatar;
import static br.edu.tcc.agromotor.domain.model.Decimais.formatarPercentual;

/**
 * Motor de decisão agronômica. Orquestra as estratégias de cálculo em memória, sem
 * efeitos colaterais; se faltarem parâmetros, reúne as pendências de todos os
 * cálculos em uma única {@link ParametroAusenteException}.
 */
public final class EngineCalculoAgronomico {

    private final ParametroRepository parametros;
    private final CalculadorAgronomico<ResultadoCalagem> calagem;
    private final CalculadorAgronomico<ResultadoGessagem> gessagem;
    private final CalculadorAgronomico<ResultadoAdubacao> adubacao;
    private final CalculadorAgronomico<List<ResultadoMicronutriente>> micronutrientes;

    public EngineCalculoAgronomico(ParametroRepository parametros) {
        this(parametros, new CalculadorCalagem(), new CalculadorGessagem(), new CalculadorAdubacaoNPK(),
                new CalculadorMicronutrientes());
    }

    public EngineCalculoAgronomico(ParametroRepository parametros,
                                   CalculadorAgronomico<ResultadoCalagem> calagem,
                                   CalculadorAgronomico<ResultadoGessagem> gessagem,
                                   CalculadorAgronomico<ResultadoAdubacao> adubacao,
                                   CalculadorAgronomico<List<ResultadoMicronutriente>> micronutrientes) {
        this.parametros = Objects.requireNonNull(parametros);
        this.calagem = Objects.requireNonNull(calagem);
        this.gessagem = Objects.requireNonNull(gessagem);
        this.adubacao = Objects.requireNonNull(adubacao);
        this.micronutrientes = Objects.requireNonNull(micronutrientes);
    }

    public ResultadoRecomendacao processar(EntradaRecomendacao entrada) {
        List<String> pendencias = new ArrayList<>();
        VersaoParametros versao = parametros.versaoVigente().orElse(null);
        if (versao == null) {
            pendencias.add("Versão vigente das tabelas paramétricas");
        }

        ResultadoCalagem resultadoCalagem = executar(calagem, entrada, pendencias);
        ResultadoGessagem resultadoGessagem = executar(gessagem, entrada, pendencias);
        ResultadoAdubacao resultadoAdubacao = executar(adubacao, entrada, pendencias);
        List<ResultadoMicronutriente> resultadoMicro = executar(micronutrientes, entrada, pendencias);

        if (!pendencias.isEmpty()) {
            throw new ParametroAusenteException(pendencias);
        }

        return new ResultadoRecomendacao(
                entrada.laudo().indicadores(),
                resultadoCalagem,
                resultadoGessagem,
                resultadoAdubacao,
                resultadoMicro,
                avisos(resultadoCalagem, resultadoGessagem, resultadoMicro),
                memoriaCalculo(entrada, resultadoCalagem, resultadoGessagem, resultadoAdubacao, resultadoMicro),
                versao);
    }

    private <R> R executar(CalculadorAgronomico<R> calculador, EntradaRecomendacao entrada, List<String> pendencias) {
        try {
            return calculador.calcular(entrada, parametros);
        } catch (ParametroAusenteException e) {
            pendencias.addAll(e.pendencias());
            return null;
        }
    }

    private static List<String> avisos(ResultadoCalagem calagem, ResultadoGessagem gessagem,
                                       List<ResultadoMicronutriente> micronutrientes) {
        List<String> avisos = new ArrayList<>();
        if (calagem.necessario() && calagem.necessidadeCalcarioTonHa().signum() == 0) {
            avisos.add(calagem.justificativa());
        }
        if (!gessagem.avaliada()) {
            avisos.add(gessagem.justificativa());
        }
        micronutrientes.stream()
                .filter(m -> m.previstoNoManual() && !m.avaliado())
                .forEach(m -> avisos.add(m.justificativa()));
        return avisos;
    }

    private static List<String> memoriaCalculo(EntradaRecomendacao entrada, ResultadoCalagem calagem,
                                               ResultadoGessagem gessagem, ResultadoAdubacao adubacao,
                                               List<ResultadoMicronutriente> micronutrientes) {
        LaudoSolo laudo = entrada.laudo();
        IndicadoresSolo ind = laudo.indicadores();
        List<String> memoria = new ArrayList<>();
        memoria.add("SB = Ca + Mg + K = %s + %s + %s = %s cmolc/dm³".formatted(
                formatar(laudo.caCmolc()), formatar(laudo.mgCmolc()), formatar(laudo.kCmolc()), formatar(ind.somaBases())));
        memoria.add("t = SB + Al = %s + %s = %s cmolc/dm³".formatted(
                formatar(ind.somaBases()), formatar(laudo.alCmolc()), formatar(ind.ctcEfetiva())));
        memoria.add("T = SB + (H+Al) = %s + %s = %s cmolc/dm³".formatted(
                formatar(ind.somaBases()), formatar(laudo.hAlCmolc()), formatar(ind.ctcTotal())));
        memoria.add("V1%% = SB / T × 100 = %s".formatted(formatarPercentual(ind.saturacaoBases())));
        memoria.add("m%% = Al / t × 100 = %s".formatted(formatarPercentual(ind.saturacaoAluminio())));
        memoria.add("K na CTC = K / T × 100 = %s".formatted(formatarPercentual(laudo.porcentagemKNaCtc())));
        memoria.addAll(calagem.memoria());
        memoria.addAll(gessagem.memoria());
        memoria.addAll(adubacao.memoria());
        micronutrientes.stream()
                .filter(ResultadoMicronutriente::avaliado)
                .forEach(m -> memoria.add(m.nutriente().simbolo() + ": " + m.justificativa()));
        return memoria;
    }
}
