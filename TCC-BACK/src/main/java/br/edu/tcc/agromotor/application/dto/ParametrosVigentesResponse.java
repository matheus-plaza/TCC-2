package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.VersaoParametros;

import java.math.BigDecimal;
import java.util.List;

/** Conteúdo da versão vigente das tabelas paramétricas, para consulta (RNF02). */
public record ParametrosVigentesResponse(
        VersaoParametros versao,
        List<CulturaResponse> culturas,
        List<Faixa> faixas,
        List<Npk> npk,
        Gessagem gessagem,
        List<Calagem> calagem,
        List<Micro> micronutrientes) {

    public record Faixa(String atributo, String rotulo, String unidade, BigDecimal criterioMin, BigDecimal criterioMax,
                        BigDecimal teorMin, BigDecimal teorMax, String classe) {
    }

    /** {@code doseMin} e {@code doseMax} nulos: a célula é "Inviável" no manual. */
    public record Npk(String cultura, String nutriente, String classe, String grupoAntecessor, BigDecimal rendimentoMin,
                      BigDecimal rendimentoMax, BigDecimal doseMin, BigDecimal doseMax) {
    }

    public record Gessagem(BigDecimal limiteMPorcentagem, BigDecimal limiteCaCmolc, List<DoseGesso> doses) {
    }

    public record DoseGesso(BigDecimal argilaMin, BigDecimal argilaMax, BigDecimal doseKgHa) {
    }

    public record Calagem(String manejo, String observacao) {
    }

    public record Micro(String cultura, String nutriente, String classe, BigDecimal doseKgHa) {
    }
}
