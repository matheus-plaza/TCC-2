package br.edu.tcc.fertsolo.application.dto;

import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.model.VersaoParametros;

import java.util.List;

/** Dados públicos do sistema, usados para avisar quando os parâmetros não são definitivos. */
public record SistemaInfoResponse(VersaoParametros versaoParametros, List<ContaDemonstracao> contasDemonstracao) {

    public record ContaDemonstracao(PerfilUsuario perfil, String nome, String email, String senha) {
    }
}
