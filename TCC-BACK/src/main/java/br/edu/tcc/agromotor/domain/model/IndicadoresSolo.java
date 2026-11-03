package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;

/**
 * Relações químicas calculadas a partir do laudo (RF04):
 * <pre>
 * SB = Ca + Mg + K
 * t  = SB + Al
 * T  = SB + (H + Al)
 * V% = SB / T × 100
 * m% = Al / t × 100
 * </pre>
 */
public record IndicadoresSolo(
        BigDecimal somaBases,
        BigDecimal ctcEfetiva,
        BigDecimal ctcTotal,
        BigDecimal saturacaoBases,
        BigDecimal saturacaoAluminio) {

    public static IndicadoresSolo de(BigDecimal ca, BigDecimal mg, BigDecimal k, BigDecimal al, BigDecimal hAl) {
        BigDecimal sb = ca.add(mg).add(k);
        BigDecimal t = sb.add(al);
        BigDecimal ctc = sb.add(hAl);
        return new IndicadoresSolo(
                Decimais.valor(sb),
                Decimais.valor(t),
                Decimais.valor(ctc),
                Decimais.percentual(sb, ctc),
                Decimais.percentual(al, t));
    }
}
