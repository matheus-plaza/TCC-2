package br.edu.tcc.agromotor.application.port;

public interface CifradorSenha {

    String cifrar(String senha);

    boolean confere(String senha, String hash);
}
