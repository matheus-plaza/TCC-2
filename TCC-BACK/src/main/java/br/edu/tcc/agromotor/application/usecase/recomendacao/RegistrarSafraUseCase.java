package br.edu.tcc.agromotor.application.usecase.recomendacao;

import br.edu.tcc.agromotor.application.dto.RegistroSafraRequest;
import br.edu.tcc.agromotor.application.dto.SafraResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.RegistroSafraRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

import java.time.LocalDateTime;

/**
 * Colheita e gasto com adubação de uma recomendação adotada, informados pelo produtor ou
 * pelo agrônomo. O registro fica à parte: a recomendação adotada continua imutável (RNF06).
 */
@UseCase
public class RegistrarSafraUseCase {

    private final RecomendacaoRepository recomendacoes;
    private final RegistroSafraRepository safras;
    private final TalhaoRepository talhoes;
    private final ControleAcesso acesso;

    public RegistrarSafraUseCase(RecomendacaoRepository recomendacoes, RegistroSafraRepository safras,
                                 TalhaoRepository talhoes, ControleAcesso acesso) {
        this.recomendacoes = recomendacoes;
        this.safras = safras;
        this.talhoes = talhoes;
        this.acesso = acesso;
    }

    public SafraResponse consultar(UsuarioAutenticado ator, Long recomendacaoId) {
        Recomendacao recomendacao = buscar(recomendacaoId);
        ContextoTalhao contexto = contextoAdotadoComAcesso(ator, recomendacao);
        return SafraResponse.de(recomendacao, contexto, safras.buscarPorRecomendacao(recomendacaoId).orElse(null));
    }

    public SafraResponse salvar(UsuarioAutenticado ator, Long recomendacaoId, RegistroSafraRequest r) {
        acesso.exigirPerfil(ator, PerfilUsuario.PRODUTOR, PerfilUsuario.AGRONOMO);
        Recomendacao recomendacao = buscar(recomendacaoId);
        ContextoTalhao contexto = contextoAdotadoComAcesso(ator, recomendacao);

        if (r.produtividadeColhidaTonHa() == null && r.dataColheita() == null && r.custoAdubacaoReais() == null) {
            safras.excluirPorRecomendacao(recomendacaoId);
            return SafraResponse.de(recomendacao, contexto, null);
        }
        if (r.dataColheita() != null && r.dataColheita().isBefore(recomendacao.dataAdocao().toLocalDate())) {
            throw new RegraNegocioException("A data da colheita não pode ser anterior à adoção da recomendação");
        }
        RegistroSafra salvo = safras.salvar(new RegistroSafra(null, recomendacaoId, r.produtividadeColhidaTonHa(),
                r.dataColheita(), r.custoAdubacaoReais(), ator.id(), LocalDateTime.now()));
        return SafraResponse.de(recomendacao, contexto, salvo);
    }

    private Recomendacao buscar(Long id) {
        return recomendacoes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recomendação", id));
    }

    private ContextoTalhao contextoAdotadoComAcesso(UsuarioAutenticado ator, Recomendacao recomendacao) {
        ContextoTalhao contexto = talhoes.contexto(recomendacao.entrada().laudo().talhaoId()).orElseThrow();
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        if (!recomendacao.adotada()) {
            throw new ConflitoException("A safra é registrada apenas em recomendações adotadas");
        }
        return contexto;
    }
}
