package br.edu.tcc.agromotor.domain.model.parametro;

import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.NutrienteNpk;

import java.math.BigDecimal;

/**
 * Uma célula das tabelas de dose do manual. P₂O₅ é procurado pela classe de P, K₂O
 * pela classe de K e, no trigo, N pela classe de matéria orgânica.
 *
 * @param classeFertilidade nula quando a tabela não depende de classe (N do milho)
 * @param grupoAntecessor   nulo quando a tabela não depende da cultura anterior
 * @param dose              nula quando o manual marca a célula como "Inviável"
 */
public record RegraNpk(
        Long culturaId,
        NutrienteNpk nutriente,
        String classeFertilidade,
        GrupoBotanico grupoAntecessor,
        Intervalo rendimento,
        FaixaDose dose) {

    public boolean aplicaA(NutrienteNpk nutrienteBuscado, String classe, GrupoBotanico antecessor, BigDecimal expectativaRendimento) {
        return nutriente == nutrienteBuscado
                && (classeFertilidade == null || classeFertilidade.equalsIgnoreCase(classe))
                && (grupoAntecessor == null || grupoAntecessor == antecessor)
                && rendimento.contem(expectativaRendimento);
    }

    public boolean inviavel() {
        return dose == null;
    }
}
