package br.edu.tcc.agromotor.domain.model;

import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Acumula os erros de validação para reportar todos de uma vez. */
public final class Validacao {

    private final List<String> erros = new ArrayList<>();

    public Validacao obrigatorio(Object valor, String campo) {
        if (valor == null || (valor instanceof String texto && texto.isBlank())) {
            erros.add(campo + " é obrigatório");
        }
        return this;
    }

    public Validacao naoNegativo(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() < 0) {
            erros.add(campo + " não pode ser negativo");
        }
        return this;
    }

    public Validacao positivo(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() <= 0) {
            erros.add(campo + " deve ser maior que zero");
        }
        return this;
    }

    public Validacao entre(BigDecimal valor, BigDecimal minimo, BigDecimal maximo, String campo) {
        if (valor != null && (valor.compareTo(minimo) < 0 || valor.compareTo(maximo) > 0)) {
            erros.add("%s deve estar entre %s e %s".formatted(campo, minimo.toPlainString(), maximo.toPlainString()));
        }
        return this;
    }

    public Validacao verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            erros.add(mensagem);
        }
        return this;
    }

    public void lancarSeHouverErros() {
        if (!erros.isEmpty()) {
            throw new RegraNegocioException(erros);
        }
    }
}
