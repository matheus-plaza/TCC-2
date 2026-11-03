package br.edu.tcc.agromotor.domain.model;

import java.util.Set;

/**
 * Produtores cujos dados um usuário pode ver (RNF05). {@code irrestrito} vale
 * para o administrador; nos demais perfis, a visão fica limitada a {@code produtorIds}.
 */
public record EscopoProdutores(boolean irrestrito, Set<Long> produtorIds) {

    public static EscopoProdutores total() {
        return new EscopoProdutores(true, Set.of());
    }

    public static EscopoProdutores de(Set<Long> produtorIds) {
        return new EscopoProdutores(false, Set.copyOf(produtorIds));
    }

    public boolean permite(Long produtorId) {
        return irrestrito || produtorIds.contains(produtorId);
    }
}
