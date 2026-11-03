package br.edu.tcc.agromotor.application.port;

import br.edu.tcc.agromotor.application.dto.SistemaInfoResponse.ContaDemonstracao;

import java.util.List;

/** Contas de exemplo oferecidas na tela de login; vazia fora do perfil "demo". */
public interface ModoDemonstracao {

    List<ContaDemonstracao> contas();
}
