package br.edu.tcc.agromotor.domain.model.parametro;

import java.math.BigDecimal;

/** Dose de gesso conforme a faixa de argila, em %. */
public record DoseGesso(Intervalo argila, BigDecimal doseKgHa) {
}
