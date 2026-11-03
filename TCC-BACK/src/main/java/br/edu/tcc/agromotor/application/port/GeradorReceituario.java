package br.edu.tcc.agromotor.application.port;

import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;

public interface GeradorReceituario {

    byte[] gerar(RecomendacaoResponseDTO recomendacao);
}
