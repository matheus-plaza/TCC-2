package br.edu.tcc.agromotor.application.usecase.laudo;

import br.edu.tcc.agromotor.application.dto.LaudoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.domain.service.ClassificadorFertilidade;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** UC07: consulta dos laudos, sempre restrita ao escopo do usuário. RF04 no detalhe. */
@UseCase
public class ConsultarLaudosUseCase {

    private final LaudoRepository laudos;
    private final TalhaoRepository talhoes;
    private final ParametroRepository parametros;
    private final ControleAcesso acesso;

    public ConsultarLaudosUseCase(LaudoRepository laudos, TalhaoRepository talhoes, ParametroRepository parametros,
                                  ControleAcesso acesso) {
        this.laudos = laudos;
        this.talhoes = talhoes;
        this.parametros = parametros;
        this.acesso = acesso;
    }

    public List<LaudoResponse> listar(UsuarioAutenticado ator, FiltroLaudos filtro) {
        EscopoProdutores escopo = acesso.escopo(ator);
        Map<Long, ContextoTalhao> contextos = talhoes.listarContextos(escopo, null).stream()
                .collect(Collectors.toMap(ContextoTalhao::talhaoId, Function.identity()));
        return laudos.listar(escopo, filtro).stream()
                .map(l -> LaudoResponse.de(l, contextos.get(l.talhaoId()), List.of()))
                .toList();
    }

    public LaudoResponse detalhar(UsuarioAutenticado ator, Long id) {
        LaudoSolo laudo = laudos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Laudo", id));
        ContextoTalhao contexto = talhoes.contexto(laudo.talhaoId()).orElseThrow();
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        return LaudoResponse.de(laudo, contexto, new ClassificadorFertilidade(parametros).interpretar(laudo));
    }
}
