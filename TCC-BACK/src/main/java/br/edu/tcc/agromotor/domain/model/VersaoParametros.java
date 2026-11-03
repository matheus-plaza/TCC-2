package br.edu.tcc.agromotor.domain.model;

/**
 * Versão das tabelas paramétricas usada em um cálculo (RNF06).
 * {@code conferidaManual} indica se os valores foram conferidos com o Manual NEPAR.
 */
public record VersaoParametros(Long id, String codigo, String descricao, boolean conferidaManual) {
}
