package br.edu.tcc.agromotor.application.port;

import br.edu.tcc.agromotor.domain.model.Usuario;

import java.time.Instant;

public interface GeradorToken {

    TokenGerado gerar(Usuario usuario);

    record TokenGerado(String token, Instant expiraEm) {
    }
}
