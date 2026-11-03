package br.edu.tcc.agromotor.domain.exception;

import java.util.List;

/** Dados que violam uma regra do domínio. Reúne todas as violações encontradas. */
public class RegraNegocioException extends RuntimeException {

    private final List<String> erros;

    public RegraNegocioException(List<String> erros) {
        super(String.join("; ", erros));
        this.erros = List.copyOf(erros);
    }

    public RegraNegocioException(String erro) {
        this(List.of(erro));
    }

    public List<String> erros() {
        return erros;
    }
}
