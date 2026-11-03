package br.edu.tcc.agromotor.domain.model.calculo;

import br.edu.tcc.agromotor.domain.model.AtributoSolo;

import java.math.BigDecimal;

/** Enquadramento de um atributo do laudo. {@code classe} é nula se a tabela não cobrir o valor. */
public record InterpretacaoAtributo(AtributoSolo atributo, BigDecimal valor, String classe) {
}
