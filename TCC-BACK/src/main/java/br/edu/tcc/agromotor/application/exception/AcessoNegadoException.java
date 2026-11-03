package br.edu.tcc.agromotor.application.exception;

/** O usuário autenticado não tem permissão para a operação ou para o recurso (RNF05). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
