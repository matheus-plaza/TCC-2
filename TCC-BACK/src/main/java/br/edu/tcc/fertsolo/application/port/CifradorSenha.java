package br.edu.tcc.fertsolo.application.port;

public interface CifradorSenha {

    String cifrar(String senha);

    boolean confere(String senha, String hash);
}
