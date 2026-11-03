package br.edu.tcc.agromotor.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Laudo laboratorial de uma camada do solo. Imutável: o motor recebe o laudo e
 * devolve um resultado sem alterar seu estado.
 *
 * <p>Validação (RF03): pH em [3,0; 9,0], argila em [0; 100] e teores maiores que
 * zero. A exceção é o Al³⁺, em que zero é um resultado laboratorial válido e comum
 * em solos corrigidos.</p>
 */
public record LaudoSolo(
        Long id,
        Long talhaoId,
        LocalDate dataAnalise,
        ProfundidadeAmostra profundidade,
        BigDecimal ph,
        MetodoPh metodoPh,
        BigDecimal moGDm3,
        BigDecimal pMehlich,
        BigDecimal kCmolc,
        BigDecimal caCmolc,
        BigDecimal mgCmolc,
        BigDecimal alCmolc,
        BigDecimal hAlCmolc,
        BigDecimal argilaPorcentagem,
        BigDecimal znMgDm3,
        BigDecimal bMgDm3,
        BigDecimal cuMgDm3,
        BigDecimal mnMgDm3) {

    private static final BigDecimal PH_MINIMO = new BigDecimal("3.0");
    private static final BigDecimal PH_MAXIMO = new BigDecimal("9.0");

    public LaudoSolo {
        new Validacao()
                .obrigatorio(talhaoId, "Talhão")
                .obrigatorio(dataAnalise, "Data da coleta")
                .obrigatorio(profundidade, "Profundidade")
                .obrigatorio(ph, "pH")
                .obrigatorio(metodoPh, "Método do pH")
                .obrigatorio(moGDm3, "Matéria orgânica")
                .obrigatorio(pMehlich, "Fósforo")
                .obrigatorio(kCmolc, "Potássio")
                .obrigatorio(caCmolc, "Cálcio")
                .obrigatorio(mgCmolc, "Magnésio")
                .obrigatorio(alCmolc, "Alumínio")
                .obrigatorio(hAlCmolc, "Acidez potencial (H+Al)")
                .obrigatorio(argilaPorcentagem, "Argila")
                .entre(ph, PH_MINIMO, PH_MAXIMO, "pH")
                .entre(argilaPorcentagem, BigDecimal.ZERO, Decimais.CEM, "Argila (%)")
                .positivo(moGDm3, "Matéria orgânica")
                .positivo(pMehlich, "Fósforo")
                .positivo(kCmolc, "Potássio")
                .positivo(caCmolc, "Cálcio")
                .positivo(mgCmolc, "Magnésio")
                .naoNegativo(alCmolc, "Alumínio")
                .positivo(hAlCmolc, "Acidez potencial (H+Al)")
                .naoNegativo(znMgDm3, "Zinco")
                .naoNegativo(bMgDm3, "Boro")
                .naoNegativo(cuMgDm3, "Cobre")
                .naoNegativo(mnMgDm3, "Manganês")
                .lancarSeHouverErros();
    }

    /** O pH ideal das culturas (Tabela 5.1) só existe em CaCl₂: em água, o pH serve apenas para a classe. */
    public boolean phComparavelAoIdeal() {
        return metodoPh == MetodoPh.CACL2;
    }

    public BigDecimal somaBases() {
        return caCmolc.add(mgCmolc).add(kCmolc);
    }

    public BigDecimal ctcTotal() {
        return somaBases().add(hAlCmolc);
    }

    public IndicadoresSolo indicadores() {
        return IndicadoresSolo.de(caCmolc, mgCmolc, kCmolc, alCmolc, hAlCmolc);
    }

    /** Compara o V% exato com o limite, sem o arredondamento usado na apresentação. */
    public boolean saturacaoBasesAbaixoDe(BigDecimal limite) {
        return Decimais.CEM.multiply(somaBases()).compareTo(limite.multiply(ctcTotal())) < 0;
    }

    /** Compara o m% exato com o limite, sem o arredondamento usado na apresentação. */
    public boolean saturacaoAluminioAcimaDe(BigDecimal limite) {
        return Decimais.CEM.multiply(alCmolc).compareTo(limite.multiply(somaBases().add(alCmolc))) > 0;
    }

    /** K × 100 / T, com uma casa: é o valor que o manual enquadra nas classes de potássio. */
    public BigDecimal porcentagemKNaCtc() {
        return Decimais.percentual(kCmolc, ctcTotal());
    }

    public BigDecimal teor(Micronutriente nutriente) {
        return switch (nutriente) {
            case ZINCO -> znMgDm3;
            case BORO -> bMgDm3;
            case COBRE -> cuMgDm3;
            case MANGANES -> mnMgDm3;
        };
    }
}
