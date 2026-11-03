package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Aritmética decimal do motor (RNF03): somente BigDecimal e arredondamento
 * HALF_UP aplicado na saída. Bases e doses saem com 2 casas; percentuais, com 1.
 */
public final class Decimais {

    public static final BigDecimal CEM = new BigDecimal("100");
    public static final int CASAS_VALOR = 2;
    public static final int CASAS_PERCENTUAL = 1;

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Decimais() {
    }

    public static BigDecimal valor(BigDecimal v) {
        return v == null ? null : v.setScale(CASAS_VALOR, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentualArredondado(BigDecimal v) {
        return v == null ? null : v.setScale(CASAS_PERCENTUAL, RoundingMode.HALF_UP);
    }

    /** parte / todo × 100. Faz uma única divisão, já na escala de saída. */
    public static BigDecimal percentual(BigDecimal parte, BigDecimal todo) {
        if (todo.signum() == 0) {
            return BigDecimal.ZERO.setScale(CASAS_PERCENTUAL);
        }
        return parte.multiply(CEM).divide(todo, CASAS_PERCENTUAL, RoundingMode.HALF_UP);
    }

    public static BigDecimal maximo(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static boolean menorQue(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) < 0;
    }

    public static boolean maiorQue(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    /** Formata no padrão brasileiro para justificativas e memória de cálculo. */
    public static String formatar(BigDecimal v) {
        return formatar(v, CASAS_VALOR);
    }

    public static String formatar(BigDecimal v, int casas) {
        if (v == null) {
            return "—";
        }
        String padrao = casas == 0 ? "#,##0" : "#,##0." + "0".repeat(casas);
        DecimalFormat formato = new DecimalFormat(padrao, DecimalFormatSymbols.getInstance(PT_BR));
        return formato.format(v.setScale(casas, RoundingMode.HALF_UP));
    }

    public static String formatarPercentual(BigDecimal v) {
        return formatar(v, CASAS_PERCENTUAL) + "%";
    }
}
