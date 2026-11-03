package br.edu.tcc.fertsolo.domain.model;

import java.time.LocalDate;

/** Filtros opcionais da consulta de recomendações; campo nulo não restringe. */
public record FiltroRecomendacoes(Long talhaoId, Long laudoId, StatusRecomendacao status, LocalDate inicio, LocalDate fim) {
}
