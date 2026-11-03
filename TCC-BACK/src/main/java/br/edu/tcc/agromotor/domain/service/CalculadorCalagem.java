package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.Decimais;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.Validacao;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoCalagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraCalagem;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static br.edu.tcc.agromotor.domain.model.Decimais.formatar;
import static br.edu.tcc.agromotor.domain.model.Decimais.formatarPercentual;

/**
 * Necessidade de Calagem pelo método da Saturação por Bases (manual, p. 50):
 * <pre>NC (t/ha) = (V2 − V1) × T / PRNT</pre>
 * O manual indica o calcário quando V1 está abaixo de um limite próprio da cultura.
 * Com V1 acima desse limite, a dose é zero, mesmo que a fórmula dê um valor positivo.
 */
public final class CalculadorCalagem implements CalculadorAgronomico<ResultadoCalagem> {

    /**
     * Resultado da fórmula em t/ha, com 2 casas (HALF_UP). Como V1 = 100 × SB / T,
     * o numerador equivale a V2 × T − 100 × SB: essa forma dispensa a divisão de V1
     * e mantém a conta exata até a única divisão final.
     */
    public BigDecimal calcularNC(LaudoSolo laudo, Cultura cultura, BigDecimal prnt) {
        new Validacao()
                .obrigatorio(cultura.vAlvo(), "V% alvo da cultura")
                .obrigatorio(prnt, "PRNT do calcário")
                .positivo(prnt, "PRNT do calcário")
                .lancarSeHouverErros();
        BigDecimal numerador = cultura.vAlvo().multiply(laudo.ctcTotal())
                .subtract(Decimais.CEM.multiply(laudo.somaBases()));
        if (numerador.signum() <= 0) {
            return BigDecimal.ZERO.setScale(Decimais.CASAS_VALOR);
        }
        return numerador.divide(prnt, Decimais.CASAS_VALOR, RoundingMode.HALF_UP);
    }

    @Override
    public ResultadoCalagem calcular(EntradaRecomendacao entrada, ParametroRepository parametros) {
        LaudoSolo laudo = entrada.laudo();
        Cultura cultura = entrada.culturaAlvo();
        BigDecimal prnt = entrada.prntCalcario();
        BigDecimal v1 = laudo.indicadores().saturacaoBases();
        BigDecimal v2 = cultura.vAlvo();
        BigDecimal t = laudo.indicadores().ctcTotal();

        if (cultura.vMinimo() == null) {
            throw new ParametroAusenteException("Limite de V% da calagem para " + cultura.nome());
        }
        RegraCalagem regra = parametros.regraCalagem(entrada.manejo())
                .orElseThrow(() -> new ParametroAusenteException("Regra de calagem para " + entrada.manejo().rotulo()));

        BigDecimal nc = calcularNC(laudo, cultura, prnt);
        boolean abaixoDoLimite = laudo.saturacaoBasesAbaixoDe(cultura.vMinimo());
        boolean abaixoDoPh = laudo.phComparavelAoIdeal() && cultura.phMinIdeal() != null
                && Decimais.menorQue(laudo.ph(), cultura.phMinIdeal());
        boolean necessario = abaixoDoLimite || abaixoDoPh;
        BigDecimal dose = abaixoDoLimite ? nc : BigDecimal.ZERO.setScale(Decimais.CASAS_VALOR);

        List<String> memoria = new ArrayList<>();
        memoria.add("NC = (V2 − V1) × T / PRNT = (%s − %s) × %s / %s = %s t/ha".formatted(
                formatarPercentual(v2), formatarPercentual(v1), formatar(t), formatarPercentual(prnt), formatar(nc)));
        if (!abaixoDoLimite) {
            memoria.add("Calagem: V1%% de %s não está abaixo do limite de %s da cultura: dose de %s t/ha".formatted(
                    formatarPercentual(v1), formatarPercentual(cultura.vMinimo()), formatar(dose)));
        }

        return new ResultadoCalagem(necessario, Decimais.percentualArredondado(v2), nc, dose,
                necessario ? regra.observacao() : "Calagem não necessária para esta cultura.",
                justificativa(laudo, cultura, v1, v2, abaixoDoLimite, abaixoDoPh),
                memoria);
    }

    private static String justificativa(LaudoSolo laudo, Cultura cultura, BigDecimal v1, BigDecimal v2,
                                        boolean abaixoDoLimite, boolean abaixoDoPh) {
        BigDecimal limite = cultura.vMinimo();
        if (abaixoDoLimite) {
            return "V1%% atual (%s) abaixo do limite de %s da cultura: elevar para V2%% de %s.".formatted(
                    formatarPercentual(v1), formatarPercentual(limite), formatarPercentual(v2));
        }
        if (abaixoDoPh) {
            return ("pH %s abaixo do ideal de %s (%s), mas V1%% (%s) não está abaixo do limite de %s: "
                    + "o método da saturação por bases não indica dose. Avaliar tecnicamente.")
                    .formatted(formatar(laudo.ph()), cultura.nome(), formatar(cultura.phMinIdeal()),
                            formatarPercentual(v1), formatarPercentual(limite));
        }
        return "V1%% atual (%s) não está abaixo do limite de %s da cultura.".formatted(
                formatarPercentual(v1), formatarPercentual(limite));
    }
}
