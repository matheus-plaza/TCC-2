package br.edu.tcc.agromotor.application.usecase.laudo;

import br.edu.tcc.agromotor.application.dto.IndicadoresRequest;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;

/**
 * Pré-visualização de SB, t, T, V% e m% na Tela A. As fórmulas ficam no domínio, e
 * não duplicadas no Angular (RNF01). Não acessa o banco nem grava nada.
 */
@UseCase
public class CalcularIndicadoresUseCase {

    public IndicadoresSolo executar(IndicadoresRequest r) {
        return IndicadoresSolo.de(r.caCmolc(), r.mgCmolc(), r.kCmolc(), r.alCmolc(), r.hAlCmolc());
    }
}
