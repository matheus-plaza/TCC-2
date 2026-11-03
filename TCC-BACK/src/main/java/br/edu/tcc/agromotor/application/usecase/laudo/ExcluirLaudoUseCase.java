package br.edu.tcc.agromotor.application.usecase.laudo;

import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

/** Só exclui laudo sem recomendação: o histórico agronômico não pode perder a origem. */
@UseCase
public class ExcluirLaudoUseCase {

    private final LaudoRepository laudos;
    private final TalhaoRepository talhoes;
    private final RecomendacaoRepository recomendacoes;
    private final ControleAcesso acesso;

    public ExcluirLaudoUseCase(LaudoRepository laudos, TalhaoRepository talhoes, RecomendacaoRepository recomendacoes,
                               ControleAcesso acesso) {
        this.laudos = laudos;
        this.talhoes = talhoes;
        this.recomendacoes = recomendacoes;
        this.acesso = acesso;
    }

    public void executar(UsuarioAutenticado ator, Long id) {
        LaudoSolo laudo = laudos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Laudo", id));
        ContextoTalhao contexto = talhoes.contexto(laudo.talhaoId()).orElseThrow();
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        if (recomendacoes.existeParaLaudo(id)) {
            throw new ConflitoException("Este laudo já foi usado em recomendações e não pode ser excluído");
        }
        laudos.excluir(id);
    }
}
