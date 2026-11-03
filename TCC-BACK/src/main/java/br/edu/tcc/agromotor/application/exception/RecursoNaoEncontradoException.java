package br.edu.tcc.agromotor.application.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("%s não encontrado(a): %s".formatted(recurso, id));
    }
}
