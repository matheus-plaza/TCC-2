package br.edu.tcc.fertsolo.application.usecase.recomendacao;

import br.edu.tcc.fertsolo.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.fertsolo.application.dto.UsuarioAutenticado;
import br.edu.tcc.fertsolo.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.fertsolo.application.usecase.ControleAcesso;
import br.edu.tcc.fertsolo.application.usecase.UseCase;
import br.edu.tcc.fertsolo.domain.model.ContextoTalhao;
import br.edu.tcc.fertsolo.domain.model.PerfilUsuario;
import br.edu.tcc.fertsolo.domain.model.Recomendacao;
import br.edu.tcc.fertsolo.domain.repository.RecomendacaoRepository;
import br.edu.tcc.fertsolo.domain.repository.TalhaoRepository;

import java.time.LocalDateTime;

/**
 * RF06 / UC09: SIMULACAO → ADOTADA. A adoção é a decisão técnica, por isso é exclusiva
 * do agrônomo vinculado ao produtor, e passa a compor o histórico do talhão.
 */
@UseCase
public class AdotarRecomendacaoUseCase {

    private final RecomendacaoRepository recomendacoes;
    private final TalhaoRepository talhoes;
    private final MontadorRecomendacao montador;
    private final ControleAcesso acesso;

    public AdotarRecomendacaoUseCase(RecomendacaoRepository recomendacoes, TalhaoRepository talhoes,
                                     MontadorRecomendacao montador, ControleAcesso acesso) {
        this.recomendacoes = recomendacoes;
        this.talhoes = talhoes;
        this.montador = montador;
        this.acesso = acesso;
    }

    public RecomendacaoResponseDTO executar(UsuarioAutenticado ator, Long id) {
        acesso.exigirPerfil(ator, PerfilUsuario.AGRONOMO);
        Recomendacao recomendacao = recomendacoes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recomendação", id));
        ContextoTalhao contexto = talhoes.contexto(recomendacao.entrada().laudo().talhaoId()).orElseThrow();
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        return montador.registrada(recomendacoes.salvar(recomendacao.adotar(ator.id(), LocalDateTime.now())));
    }
}
