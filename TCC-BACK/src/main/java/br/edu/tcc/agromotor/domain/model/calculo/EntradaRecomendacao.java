package br.edu.tcc.agromotor.domain.model.calculo;

import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.Validacao;

import java.math.BigDecimal;

/**
 * Cenário de uma recomendação. {@code culturaAnterior} e {@code grupoAnteriorInformado}
 * nulos significam pousio/palhada; o grupo é informado quando a cultura anterior não está
 * cadastrada. {@code laudoSubsuperficie} nulo significa que o talhão não tem laudo de 20-40 cm.
 */
public record EntradaRecomendacao(
        LaudoSolo laudo,
        LaudoSolo laudoSubsuperficie,
        Cultura culturaAlvo,
        Cultura culturaAnterior,
        SistemaManejo manejo,
        BigDecimal expectativaRendimentoTonHa,
        BigDecimal prntCalcario,
        GrupoBotanico grupoAnteriorInformado) {

    /** Base de cálculo definida no SRS quando o usuário não informa outro PRNT. */
    public static final BigDecimal PRNT_PADRAO = new BigDecimal("100.0");

    private static final BigDecimal PRNT_MAXIMO = new BigDecimal("200");

    public EntradaRecomendacao {
        new Validacao()
                .obrigatorio(laudo, "Laudo de solo")
                .obrigatorio(culturaAlvo, "Cultura alvo")
                .obrigatorio(manejo, "Sistema de manejo")
                .obrigatorio(expectativaRendimentoTonHa, "Expectativa de rendimento")
                .obrigatorio(prntCalcario, "PRNT do calcário")
                .positivo(expectativaRendimentoTonHa, "Expectativa de rendimento")
                .positivo(prntCalcario, "PRNT do calcário")
                .verdadeiro(prntCalcario == null || prntCalcario.compareTo(PRNT_MAXIMO) <= 0,
                        "PRNT do calcário deve ser no máximo 200%")
                .verdadeiro(laudo == null || laudo.profundidade() == ProfundidadeAmostra.CAMADA_0_20,
                        "A recomendação é calculada sobre um laudo da camada de 0-20 cm")
                .verdadeiro(laudoSubsuperficie == null
                                || laudoSubsuperficie.profundidade() == ProfundidadeAmostra.CAMADA_20_40,
                        "A gessagem é avaliada com um laudo da camada de 20-40 cm")
                .verdadeiro(culturaAnterior == null || grupoAnteriorInformado == null,
                        "Informe a cultura anterior ou o grupo dela, não os dois")
                .lancarSeHouverErros();
    }

    public EntradaRecomendacao(LaudoSolo laudo, LaudoSolo laudoSubsuperficie, Cultura culturaAlvo, Cultura culturaAnterior,
                               SistemaManejo manejo, BigDecimal expectativaRendimentoTonHa, BigDecimal prntCalcario) {
        this(laudo, laudoSubsuperficie, culturaAlvo, culturaAnterior, manejo, expectativaRendimentoTonHa, prntCalcario, null);
    }

    /** É o grupo que as tabelas de N usam; nulo significa pousio/palhada. */
    public GrupoBotanico grupoAnterior() {
        return culturaAnterior != null ? culturaAnterior.grupo() : grupoAnteriorInformado;
    }

    /** Como a cultura anterior aparece nas telas e no receituário. */
    public String descricaoAnterior() {
        if (culturaAnterior != null) {
            return culturaAnterior.nome();
        }
        if (grupoAnteriorInformado == null) {
            return "Pousio/palhada";
        }
        return grupoAnteriorInformado == GrupoBotanico.GRAMINEA ? "Outra gramínea" : "Outra leguminosa";
    }
}
