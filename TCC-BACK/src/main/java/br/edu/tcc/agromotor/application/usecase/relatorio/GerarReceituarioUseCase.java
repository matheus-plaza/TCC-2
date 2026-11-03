package br.edu.tcc.agromotor.application.usecase.relatorio;

import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.port.GeradorReceituario;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.application.usecase.recomendacao.ConsultarRecomendacoesUseCase;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;

/** RF08 / UC11: exporta o receituário de uma recomendação adotada. */
@UseCase
public class GerarReceituarioUseCase {

    private final ConsultarRecomendacoesUseCase consulta;
    private final GeradorReceituario gerador;

    public GerarReceituarioUseCase(ConsultarRecomendacoesUseCase consulta, GeradorReceituario gerador) {
        this.consulta = consulta;
        this.gerador = gerador;
    }

    public byte[] executar(UsuarioAutenticado ator, Long recomendacaoId) {
        RecomendacaoResponseDTO recomendacao = consulta.detalhar(ator, recomendacaoId);
        if (recomendacao.status() != StatusRecomendacao.ADOTADA) {
            throw new RegraNegocioException("O receituário é emitido apenas para recomendações adotadas");
        }
        return gerador.gerar(recomendacao);
    }
}
