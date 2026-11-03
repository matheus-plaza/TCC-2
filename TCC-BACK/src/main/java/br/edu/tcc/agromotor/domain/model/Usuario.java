package br.edu.tcc.agromotor.domain.model;

import java.time.LocalDateTime;

public record Usuario(
        Long id,
        String nome,
        String email,
        String senhaHash,
        PerfilUsuario perfil,
        String registroProfissional,
        LocalDateTime dataCriacao) {

    public Usuario {
        new Validacao()
                .obrigatorio(nome, "Nome")
                .obrigatorio(email, "E-mail")
                .obrigatorio(senhaHash, "Senha")
                .obrigatorio(perfil, "Perfil")
                .lancarSeHouverErros();
    }

    public Usuario comDados(String novoNome, String novoEmail, String novoRegistro) {
        return new Usuario(id, novoNome, novoEmail, senhaHash, perfil, novoRegistro, dataCriacao);
    }

    public Usuario comSenha(String novaSenhaHash) {
        return new Usuario(id, nome, email, novaSenhaHash, perfil, registroProfissional, dataCriacao);
    }
}
