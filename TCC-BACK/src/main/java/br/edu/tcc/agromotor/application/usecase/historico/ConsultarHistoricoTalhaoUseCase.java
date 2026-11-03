package br.edu.tcc.agromotor.application.usecase.historico;

import br.edu.tcc.agromotor.application.dto.HistoricoTalhaoResponse;
import br.edu.tcc.agromotor.application.dto.HistoricoTalhaoResponse.DosesResumo;
import br.edu.tcc.agromotor.application.dto.HistoricoTalhaoResponse.Evento;
import br.edu.tcc.agromotor.application.dto.HistoricoTalhaoResponse.LaudoResumo;
import br.edu.tcc.agromotor.application.dto.HistoricoTalhaoResponse.TipoEvento;
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
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.RegistroSafraRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** RF07: linha do tempo de laudos, recomendações adotadas e colheitas de um talhão, por período. */
@UseCase
public class ConsultarHistoricoTalhaoUseCase {

    private final TalhaoRepository talhoes;
    private final LaudoRepository laudos;
    private final RecomendacaoRepository recomendacoes;
    private final RegistroSafraRepository safras;
    private final ControleAcesso acesso;

    public ConsultarHistoricoTalhaoUseCase(TalhaoRepository talhoes, LaudoRepository laudos,
                                           RecomendacaoRepository recomendacoes, RegistroSafraRepository safras,
                                           ControleAcesso acesso) {
        this.talhoes = talhoes;
        this.laudos = laudos;
        this.recomendacoes = recomendacoes;
        this.safras = safras;
        this.acesso = acesso;
    }

    public HistoricoTalhaoResponse executar(UsuarioAutenticado ator, Long talhaoId, LocalDate inicio, LocalDate fim) {
        ContextoTalhao contexto = talhoes.contexto(talhaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Talhão", talhaoId));
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());
        EscopoProdutores escopo = acesso.escopo(ator);

        List<Evento> eventos = new ArrayList<>();
        laudos.listar(escopo, new FiltroLaudos(talhaoId, null, inicio, fim))
                .forEach(l -> eventos.add(eventoDeLaudo(l)));
        recomendacoes.listar(escopo, new FiltroRecomendacoes(talhaoId, null, StatusRecomendacao.ADOTADA, inicio, fim))
                .forEach(r -> eventos.add(eventoDeRecomendacao(r)));
        adicionarColheitas(eventos, escopo, contexto, inicio, fim);
        eventos.sort(Comparator.comparing(Evento::data).reversed());
        return new HistoricoTalhaoResponse(TalhaoResponse.de(contexto), eventos);
    }

    /** O período vale para a data da colheita, que não é a da adoção da recomendação. */
    private void adicionarColheitas(List<Evento> eventos, EscopoProdutores escopo, ContextoTalhao contexto,
                                    LocalDate inicio, LocalDate fim) {
        List<Recomendacao> adotadas = recomendacoes.listar(escopo,
                new FiltroRecomendacoes(contexto.talhaoId(), null, StatusRecomendacao.ADOTADA, null, null));
        Map<Long, RegistroSafra> registros = safras.porRecomendacoes(adotadas.stream().map(Recomendacao::id).toList());
        for (Recomendacao r : adotadas) {
            RegistroSafra registro = registros.get(r.id());
            if (registro != null && registro.temColheita()
                    && (inicio == null || !registro.dataColheita().isBefore(inicio))
                    && (fim == null || !registro.dataColheita().isAfter(fim))) {
                eventos.add(new Evento(TipoEvento.COLHEITA, registro.dataColheita(), r.id(),
                        "Colheita de " + r.entrada().culturaAlvo().nome(), null, null, null,
                        SafraResponse.de(r, contexto, registro)));
            }
        }
    }

    private static Evento eventoDeLaudo(LaudoSolo l) {
        return new Evento(TipoEvento.LAUDO, l.dataAnalise(), l.id(),
                "Laudo de solo (%s cm)".formatted(l.profundidade().codigo()), null,
                new LaudoResumo(l.profundidade().codigo(), l.ph(), l.metodoPh(), l.indicadores().saturacaoBases(),
                        l.indicadores().saturacaoAluminio(), l.moGDm3(), l.pMehlich(), l.kCmolc()),
                null, null);
    }

    private static Evento eventoDeRecomendacao(Recomendacao r) {
        var resultado = r.resultado();
        String cultura = r.entrada().culturaAlvo().nome();
        return new Evento(TipoEvento.RECOMENDACAO_ADOTADA, r.dataAdocao().toLocalDate(), r.id(),
                "Recomendação adotada para " + cultura,
                "Manejo: " + r.entrada().manejo().rotulo(),
                null,
                new DosesResumo(cultura, resultado.calagem().necessidadeCalcarioTonHa(), resultado.gessagem().doseKgHa(),
                        resultado.adubacao().doseNkgHa(), resultado.adubacao().doseP2O5kgHa(), resultado.adubacao().doseK2OkgHa()),
                null);
    }
}
