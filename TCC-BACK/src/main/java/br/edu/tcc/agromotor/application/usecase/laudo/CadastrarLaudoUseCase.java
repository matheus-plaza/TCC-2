package br.edu.tcc.agromotor.application.usecase.laudo;

import br.edu.tcc.agromotor.application.dto.LaudoRequest;
import br.edu.tcc.agromotor.application.dto.LaudoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.domain.service.ClassificadorFertilidade;

/** RF03 / UC06: valida o laudo pela entidade de domínio e o persiste. */
@UseCase
public class CadastrarLaudoUseCase {

    private final LaudoRepository laudos;
    private final TalhaoRepository talhoes;
    private final ParametroRepository parametros;
    private final ControleAcesso acesso;

    public CadastrarLaudoUseCase(LaudoRepository laudos, TalhaoRepository talhoes, ParametroRepository parametros,
                                 ControleAcesso acesso) {
        this.laudos = laudos;
        this.talhoes = talhoes;
        this.parametros = parametros;
        this.acesso = acesso;
    }

    public LaudoResponse executar(UsuarioAutenticado ator, LaudoRequest r) {
        ContextoTalhao contexto = talhoes.contexto(r.talhaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Talhão", r.talhaoId()));
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());

        LaudoSolo laudo = new LaudoSolo(null, r.talhaoId(), r.dataAnalise(), ProfundidadeAmostra.deCodigo(r.profundidadeCm()),
                r.ph(), r.metodoPh(), r.moGDm3(), r.pMehlich(), r.kCmolc(), r.caCmolc(), r.mgCmolc(), r.alCmolc(), r.hAlCmolc(),
                r.argilaPorcentagem(), r.znMgDm3(), r.bMgDm3(), r.cuMgDm3(), r.mnMgDm3());
        LaudoSolo salvo = laudos.salvar(laudo);
        return LaudoResponse.de(salvo, contexto, new ClassificadorFertilidade(parametros).interpretar(salvo));
    }
}
