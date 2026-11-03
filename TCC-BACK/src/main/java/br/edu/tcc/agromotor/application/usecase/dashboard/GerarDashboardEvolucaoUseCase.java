package br.edu.tcc.agromotor.application.usecase.dashboard;

import br.edu.tcc.agromotor.application.dto.EvolucaoResponse;
import br.edu.tcc.agromotor.application.dto.SafraResponse;
import br.edu.tcc.agromotor.application.dto.TalhaoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.FiltroRecomendacoes;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.RegistroSafraRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** UC10: formata o histórico em séries (eixo de datas) para as bibliotecas de gráfico. */
@UseCase
public class GerarDashboardEvolucaoUseCase {

    private final TalhaoRepository talhoes;
    private final LaudoRepository laudos;
    private final RecomendacaoRepository recomendacoes;
    private final RegistroSafraRepository safras;
    private final ControleAcesso acesso;

    public GerarDashboardEvolucaoUseCase(TalhaoRepository talhoes, LaudoRepository laudos,
                                         RecomendacaoRepository recomendacoes, RegistroSafraRepository safras,
                                         ControleAcesso acesso) {
        this.talhoes = talhoes;
        this.laudos = laudos;
        this.recomendacoes = recomendacoes;
        this.safras = safras;
        this.acesso = acesso;
    }

    public EvolucaoResponse executar(UsuarioAutenticado ator, Long talhaoId) {
        ContextoTalhao contexto = talhoes.contexto(talhaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Talhão", talhaoId));
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        EscopoProdutores escopo = acesso.escopo(ator);

        List<EvolucaoResponse.Ponto> pontos = laudos
                .listar(escopo, new FiltroLaudos(talhaoId, ProfundidadeAmostra.CAMADA_0_20, null, null)).stream()
                .sorted(Comparator.comparing(l -> l.dataAnalise()))
                .map(l -> new EvolucaoResponse.Ponto(l.dataAnalise(), l.id(), l.ph(), l.metodoPh(), l.moGDm3(), l.pMehlich(),
                        l.kCmolc(), l.caCmolc(), l.mgCmolc(), l.indicadores().saturacaoBases(), l.indicadores().saturacaoAluminio(),
                        l.indicadores().ctcTotal()))
                .toList();
        List<Recomendacao> emOrdem = recomendacoes
                .listar(escopo, new FiltroRecomendacoes(talhaoId, null, StatusRecomendacao.ADOTADA, null, null)).stream()
                .sorted(Comparator.comparing(r -> r.dataAdocao()))
                .toList();
        Map<Long, RegistroSafra> registros = safras.porRecomendacoes(emOrdem.stream().map(Recomendacao::id).toList());
        List<SafraResponse> safrasRegistradas = emOrdem.stream()
                .filter(r -> registros.containsKey(r.id()))
                .map(r -> SafraResponse.de(r, contexto, registros.get(r.id())))
                .toList();
        List<EvolucaoResponse.Adotada> adotadas = emOrdem.stream()
                .map(r -> new EvolucaoResponse.Adotada(r.dataAdocao().toLocalDate(), r.id(), r.entrada().culturaAlvo().nome(),
                        r.resultado().calagem().necessidadeCalcarioTonHa(), r.resultado().gessagem().doseKgHa(),
                        r.resultado().adubacao().doseNkgHa(), r.resultado().adubacao().doseP2O5kgHa(),
                        r.resultado().adubacao().doseK2OkgHa()))
                .toList();
        return new EvolucaoResponse(TalhaoResponse.de(contexto), pontos, adotadas, safrasRegistradas);
    }
}
