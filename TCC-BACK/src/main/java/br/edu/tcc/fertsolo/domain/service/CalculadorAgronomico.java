package br.edu.tcc.fertsolo.domain.service;

import br.edu.tcc.fertsolo.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.fertsolo.domain.repository.ParametroRepository;

/**
 * Estratégia de cálculo (padrão Strategy): recebe o cenário e devolve um resultado,
 * sem estado e sem alterar a entrada, para que cada cálculo seja testado isoladamente.
 *
 * @param <R> tipo do resultado
 */
@FunctionalInterface
public interface CalculadorAgronomico<R> {

    R calcular(EntradaRecomendacao entrada, ParametroRepository parametros);
}
