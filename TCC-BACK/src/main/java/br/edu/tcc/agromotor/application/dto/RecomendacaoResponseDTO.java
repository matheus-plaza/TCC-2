package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.model.VersaoParametros;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato da seção 8.1 da especificação. Os campos {@code id}, {@code status},
 * {@code analiseSoloId}, {@code resumoQuimico}, {@code calagem}, {@code gessagem} e
 * {@code adubacao} seguem o exemplo, mas as doses de NPK são faixas, como no manual;
 * os demais acrescentam o que as telas, o receituário e a auditoria precisam (RNF06).
 */
public record RecomendacaoResponseDTO(
        Long id,
        StatusRecomendacao status,
        Long analiseSoloId,
        ResumoQuimico resumoQuimico,
        Calagem calagem,
        Gessagem gessagem,
        Adubacao adubacao,
        List<Micronutriente> micronutrientes,
        CulturaResponse culturaAlvo,
        CulturaResponse culturaAnterior,
        GrupoBotanico grupoAnterior,
        String antecessora,
        SistemaManejo sistemaManejo,
        BigDecimal expectativaRendimentoTonHa,
        BigDecimal prntCalcario,
        TalhaoResponse talhao,
        LaudoResponse laudo,
        Long laudoSubsuperficieId,
        VersaoParametros versaoParametros,
        LocalDateTime dataGeracao,
        LocalDateTime dataAdocao,
        Responsavel responsavel,
        List<String> avisos,
        List<String> memoriaCalculo) {

    public record ResumoQuimico(
            BigDecimal somaBases,
            BigDecimal ctcEfetiva,
            BigDecimal ctcTotal,
            BigDecimal v1Porcentagem,
            BigDecimal mPorcentagem) {
    }

    public record Calagem(
            boolean necessario,
            BigDecimal v2AlvoPorcentagem,
            BigDecimal necessidadeCalcarioTonHa,
            BigDecimal ncCalculadaTonHa,
            String observacao,
            String justificativa) {
    }

    public record Gessagem(boolean necessario, BigDecimal doseKgHa, boolean avaliada, String justificativa) {
    }

    public record Adubacao(
            FaixaDose doseNkgHa,
            FaixaDose doseP2O5kgHa,
            FaixaDose doseK2OkgHa,
            String justificativaN,
            String classeFosforo,
            String justificativaP,
            String classePotassio,
            String justificativaK,
            String classeMateriaOrganica) {
    }

    public record Micronutriente(
            String nutriente,
            String simbolo,
            boolean avaliado,
            BigDecimal teor,
            String classe,
            BigDecimal doseKgHa,
            String justificativa) {
    }

    public record Responsavel(Long id, String nome, String registroProfissional) {
    }
}
