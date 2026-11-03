package br.edu.tcc.fertsolo.domain.model;

import java.time.LocalDate;

/** Filtros opcionais da consulta de laudos; campo nulo não restringe. */
public record FiltroLaudos(Long talhaoId, ProfundidadeAmostra profundidade, LocalDate inicio, LocalDate fim) {
}
