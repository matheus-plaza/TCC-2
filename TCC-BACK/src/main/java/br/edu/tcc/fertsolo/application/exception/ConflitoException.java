package br.edu.tcc.fertsolo.application.exception;

/** A operação conflita com o estado atual dos dados (duplicidade, registros vinculados). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
