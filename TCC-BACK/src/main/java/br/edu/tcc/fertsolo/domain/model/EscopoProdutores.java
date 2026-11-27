package br.edu.tcc.fertsolo.domain.model;

import java.util.Set;

/**
 * Produtores cujos dados um usuário pode ver (RNF05), limitados a {@code produtorIds}.
 * {@code irrestrito} serve apenas às rotinas internas, como a carga da demonstração.
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
