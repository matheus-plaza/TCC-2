package br.edu.tcc.agromotor.domain.exception;

import java.util.List;

/**
 * As tabelas paramétricas não cobrem o caso calculado (registro ausente ou faixas
 * sobrepostas). O motor nunca assume valores: lista o que falta e interrompe o cálculo.
 */
public class ParametroAusenteException extends RuntimeException {

    private final List<String> pendencias;

    public ParametroAusenteException(List<String> pendencias) {
        super(String.join("; ", pendencias));
        this.pendencias = List.copyOf(pendencias);
    }

    public ParametroAusenteException(String pendencia) {
        this(List.of(pendencia));
    }

    public List<String> pendencias() {
        return pendencias;
    }
}
