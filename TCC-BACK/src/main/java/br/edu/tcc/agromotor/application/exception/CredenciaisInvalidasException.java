package br.edu.tcc.agromotor.application.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
