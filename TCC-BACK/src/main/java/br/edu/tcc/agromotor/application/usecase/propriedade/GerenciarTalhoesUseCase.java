package br.edu.tcc.agromotor.application.usecase.propriedade;

import br.edu.tcc.agromotor.application.dto.TalhaoRequest;
import br.edu.tcc.agromotor.application.dto.TalhaoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.ConflitoException;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.Propriedade;
import br.edu.tcc.agromotor.domain.model.Talhao;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.PropriedadeRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

import java.util.List;

/** RF02: cadastro dos talhões de uma propriedade. */
@UseCase
public class GerenciarTalhoesUseCase {

    private final TalhaoRepository talhoes;
    private final PropriedadeRepository propriedades;
    private final LaudoRepository laudos;
    private final ControleAcesso acesso;

    public GerenciarTalhoesUseCase(TalhaoRepository talhoes, PropriedadeRepository propriedades,
                                   LaudoRepository laudos, ControleAcesso acesso) {
        this.talhoes = talhoes;
        this.propriedades = propriedades;
        this.laudos = laudos;
        this.acesso = acesso;
    }

    public List<TalhaoResponse> listar(UsuarioAutenticado ator, Long propriedadeId) {
        return talhoes.listarContextos(acesso.escopo(ator), propriedadeId).stream().map(TalhaoResponse::de).toList();
    }

    public TalhaoResponse detalhar(UsuarioAutenticado ator, Long id) {
        return TalhaoResponse.de(contextoVisivel(ator, id));
    }

    public TalhaoResponse criar(UsuarioAutenticado ator, Long propriedadeId, TalhaoRequest request) {
        Propriedade propriedade = propriedades.buscarPorId(propriedadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Propriedade", propriedadeId));
        acesso.exigirAcessoAoProdutor(ator, propriedade.produtorId());
        exigirIdentificacaoLivre(propriedadeId, request.identificacao().trim(), null);
        Talhao salvo = talhoes.salvar(new Talhao(null, propriedadeId, request.identificacao().trim(),
                request.areaHectares(), request.texturaPredominante()));
        return TalhaoResponse.de(talhoes.contexto(salvo.id()).orElseThrow());
    }

    public TalhaoResponse atualizar(UsuarioAutenticado ator, Long id, TalhaoRequest request) {
        ContextoTalhao atual = contextoVisivel(ator, id);
        exigirIdentificacaoLivre(atual.propriedadeId(), request.identificacao().trim(), id);
        talhoes.salvar(new Talhao(id, atual.propriedadeId(), request.identificacao().trim(),
                request.areaHectares(), request.texturaPredominante()));
        return TalhaoResponse.de(talhoes.contexto(id).orElseThrow());
    }

    public void excluir(UsuarioAutenticado ator, Long id) {
        contextoVisivel(ator, id);
        if (laudos.existeParaTalhao(id)) {
            throw new ConflitoException("Este talhão possui laudos cadastrados");
        }
        talhoes.excluir(id);
    }

    private void exigirIdentificacaoLivre(Long propriedadeId, String identificacao, Long talhaoIgnorado) {
        if (talhoes.existeIdentificacao(propriedadeId, identificacao, talhaoIgnorado)) {
            throw new ConflitoException("Identificação do talhão: já existe \"%s\" nesta propriedade".formatted(identificacao));
        }
    }

    private ContextoTalhao contextoVisivel(UsuarioAutenticado ator, Long id) {
        ContextoTalhao contexto = talhoes.contexto(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Talhão", id));
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        return contexto;
    }
}
