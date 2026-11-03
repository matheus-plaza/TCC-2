package br.edu.tcc.agromotor.domain;

import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.MetodoPh;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Monta laudos para os testes. Parte do laudo de exemplo da especificação (Tela A).
 * {@code BigDecimal.valueOf} usa a representação decimal do literal: 3.2 vira
 * exatamente 3.2, sem levar erro de ponto flutuante para o motor.
 */
public final class LaudoSoloBuilder {

    private Long id = 105L;
    private Long talhaoId = 1L;
    private LocalDate data = LocalDate.of(2026, 10, 1);
    private ProfundidadeAmostra profundidade = ProfundidadeAmostra.CAMADA_0_20;
    private BigDecimal ph = new BigDecimal("5.2");
    private MetodoPh metodoPh = MetodoPh.CACL2;
    private BigDecimal mo = new BigDecimal("28.5");
    private BigDecimal p = new BigDecimal("12.0");
    private BigDecimal k = new BigDecimal("0.35");
    private BigDecimal ca = new BigDecimal("3.2");
    private BigDecimal mg = new BigDecimal("1.1");
    private BigDecimal al = new BigDecimal("0.1");
    private BigDecimal hAl = new BigDecimal("4.8");
    private BigDecimal argila = new BigDecimal("45.0");
    private BigDecimal zn;
    private BigDecimal b;
    private BigDecimal cu;
    private BigDecimal mn;

    private LaudoSoloBuilder() {
    }

    public static LaudoSoloBuilder umLaudo() {
        return new LaudoSoloBuilder();
    }

    public LaudoSoloBuilder comId(Long valor) {
        id = valor;
        return this;
    }

    public LaudoSoloBuilder comData(LocalDate valor) {
        data = valor;
        return this;
    }

    public LaudoSoloBuilder naCamada2040() {
        profundidade = ProfundidadeAmostra.CAMADA_20_40;
        return this;
    }

    public LaudoSoloBuilder comPh(double valor) {
        ph = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comPhEmAgua(double valor) {
        ph = BigDecimal.valueOf(valor);
        metodoPh = MetodoPh.AGUA;
        return this;
    }

    public LaudoSoloBuilder comMo(double valor) {
        mo = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comP(double valor) {
        p = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comK(double valor) {
        k = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comCa(double valor) {
        ca = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comMg(double valor) {
        mg = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comAl(double valor) {
        al = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comHAl(double valor) {
        hAl = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comArgila(double valor) {
        argila = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comZn(double valor) {
        zn = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comB(double valor) {
        b = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comCu(double valor) {
        cu = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSoloBuilder comMn(double valor) {
        mn = BigDecimal.valueOf(valor);
        return this;
    }

    public LaudoSolo build() {
        return new LaudoSolo(id, talhaoId, data, profundidade, ph, metodoPh, mo, p, k, ca, mg, al, hAl, argila, zn, b, cu, mn);
    }
}
