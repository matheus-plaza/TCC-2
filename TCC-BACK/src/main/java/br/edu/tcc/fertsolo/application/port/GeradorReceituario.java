package br.edu.tcc.fertsolo.application.port;

import br.edu.tcc.fertsolo.application.dto.RecomendacaoResponseDTO;

public interface GeradorReceituario {

    byte[] gerar(RecomendacaoResponseDTO recomendacao);
}
