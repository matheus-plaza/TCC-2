package br.edu.tcc.agromotor.application.usecase.recomendacao;

import br.edu.tcc.agromotor.application.dto.CulturaResponse;
import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.agromotor.application.dto.SugestaoAntecessoraResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.FiltroRecomendacoes;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Consulta de recomendações no escopo do usuário. O produtor acessa apenas as
 * adotadas: as simulações são rascunhos de trabalho do agrônomo.
 */
@UseCase
public class ConsultarRecomendacoesUseCase {

    private final RecomendacaoRepository recomendacoes;
    private final TalhaoRepository talhoes;
    private final MontadorRecomendacao montador;
    private final ControleAcesso acesso;

    public ConsultarRecomendacoesUseCase(RecomendacaoRepository recomendacoes, TalhaoRepository talhoes,
                                         MontadorRecomendacao montador, ControleAcesso acesso) {
        this.recomendacoes = recomendacoes;
        this.talhoes = talhoes;
        this.montador = montador;
        this.acesso = acesso;
    }

    public List<RecomendacaoResponseDTO> listar(UsuarioAutenticado ator, FiltroRecomendacoes filtro) {
        FiltroRecomendacoes efetivo = ator.perfil() == PerfilUsuario.PRODUTOR
                ? new FiltroRecomendacoes(filtro.talhaoId(), filtro.laudoId(), StatusRecomendacao.ADOTADA, filtro.inicio(), filtro.fim())
                : filtro;
        return recomendacoes.listar(acesso.escopo(ator), efetivo).stream().map(montador::registrada).toList();
    }

    public RecomendacaoResponseDTO detalhar(UsuarioAutenticado ator, Long id) {
        return montador.registrada(buscarVisivel(ator, id));
    }

    /** Descarta uma simulação salva. Recomendação adotada é imutável (RNF06). */
    public void excluir(UsuarioAutenticado ator, Long id) {
        acesso.exigirPerfil(ator, PerfilUsuario.AGRONOMO);
        Recomendacao recomendacao = buscarVisivel(ator, id);
        if (recomendacao.adotada()) {
            throw new ConflitoException("Recomendação adotada não pode ser excluída");
        }
        recomendacoes.excluir(id);
    }

    /** Cultura da última recomendação adotada: é ela que gera o crédito/débito da próxima safra. */
    public Optional<SugestaoAntecessoraResponse> sugerirAntecessora(UsuarioAutenticado ator, Long talhaoId) {
        ContextoTalhao contexto = talhoes.contexto(talhaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Talhão", talhaoId));
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        return recomendacoes.ultimaAdotadaDoTalhao(talhaoId)
                .map(r -> new SugestaoAntecessoraResponse(CulturaResponse.de(r.entrada().culturaAlvo()), r.id(), r.dataAdocao()));
    }

    private Recomendacao buscarVisivel(UsuarioAutenticado ator, Long id) {
        Recomendacao recomendacao = recomendacoes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recomendação", id));
        ContextoTalhao contexto = talhoes.contexto(recomendacao.entrada().laudo().talhaoId()).orElseThrow();
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        if (ator.perfil() == PerfilUsuario.PRODUTOR && !recomendacao.adotada()) {
            throw new RecursoNaoEncontradoException("Recomendação", id);
        }
        return recomendacao;
    }
}
