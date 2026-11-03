package br.edu.tcc.agromotor.application.usecase.dashboard;

import br.edu.tcc.agromotor.application.dto.ResumoPainelResponse;
import br.edu.tcc.agromotor.application.dto.ResumoPainelResponse.LaudoRecente;
import br.edu.tcc.agromotor.application.dto.ResumoPainelResponse.RecomendacaoRecente;
import br.edu.tcc.agromotor.application.dto.SafraResponse;
import br.edu.tcc.agromotor.application.dto.TalhaoResponse;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.FiltroRecomendacoes;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.PropriedadeRepository;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.RegistroSafraRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Indicadores do painel, calculados apenas sobre os dados visíveis ao usuário. */
@UseCase
public class GerarResumoPainelUseCase {

    private static final int RECENTES = 5;

    private final PropriedadeRepository propriedades;
    private final TalhaoRepository talhoes;
    private final LaudoRepository laudos;
    private final RecomendacaoRepository recomendacoes;
    private final RegistroSafraRepository safras;
    private final ControleAcesso acesso;

    public GerarResumoPainelUseCase(PropriedadeRepository propriedades, TalhaoRepository talhoes, LaudoRepository laudos,
                                    RecomendacaoRepository recomendacoes, RegistroSafraRepository safras, ControleAcesso acesso) {
        this.propriedades = propriedades;
        this.talhoes = talhoes;
        this.laudos = laudos;
        this.recomendacoes = recomendacoes;
        this.safras = safras;
        this.acesso = acesso;
    }

    public ResumoPainelResponse executar(UsuarioAutenticado ator) {
        EscopoProdutores escopo = acesso.escopo(ator);
        List<ContextoTalhao> contextos = talhoes.listarContextos(escopo, null);
        Map<Long, ContextoTalhao> porTalhao = contextos.stream()
                .collect(Collectors.toMap(ContextoTalhao::talhaoId, Function.identity()));
        List<LaudoSolo> todosLaudos = laudos.listar(escopo, new FiltroLaudos(null, null, null, null));
        List<Recomendacao> todas = recomendacoes.listar(escopo, new FiltroRecomendacoes(null, null,
                ator.perfil() == PerfilUsuario.PRODUTOR ? StatusRecomendacao.ADOTADA : null, null, null));
        Set<Long> talhoesComLaudo = todosLaudos.stream().map(LaudoSolo::talhaoId).collect(Collectors.toSet());
        Map<Long, RegistroSafra> registros = safras.porRecomendacoes(todas.stream().map(Recomendacao::id).toList());

        return new ResumoPainelResponse(
                propriedades.listar(escopo).size(),
                contextos.size(),
                contextos.stream().map(ContextoTalhao::areaHectares).reduce(BigDecimal.ZERO, BigDecimal::add),
                todosLaudos.size(),
                (int) todas.stream().filter(Recomendacao::adotada).count(),
                (int) todas.stream().filter(r -> !r.adotada()).count(),
                (int) registros.values().stream().filter(RegistroSafra::temColheita).count(),
                todosLaudos.stream().limit(RECENTES).map(l -> laudoRecente(l, porTalhao.get(l.talhaoId()))).toList(),
                todas.stream().limit(RECENTES).map(r -> recomendacaoRecente(r, porTalhao.get(r.entrada().laudo().talhaoId()))).toList(),
                todas.stream()
                        .filter(r -> registros.containsKey(r.id()) && registros.get(r.id()).temColheita())
                        .sorted(Comparator.comparing((Recomendacao r) -> registros.get(r.id()).dataColheita()).reversed())
                        .limit(RECENTES)
                        .map(r -> SafraResponse.de(r, porTalhao.get(r.entrada().laudo().talhaoId()), registros.get(r.id())))
                        .toList(),
                contextos.stream().filter(c -> !talhoesComLaudo.contains(c.talhaoId())).map(TalhaoResponse::de).toList());
    }

    private static LaudoRecente laudoRecente(LaudoSolo l, ContextoTalhao c) {
        return new LaudoRecente(l.id(), l.talhaoId(), l.dataAnalise(), l.profundidade().codigo(), c.talhaoIdentificacao(),
                c.propriedadeNome(), l.indicadores().saturacaoBases(), l.ph(), l.metodoPh());
    }

    private static RecomendacaoRecente recomendacaoRecente(Recomendacao r, ContextoTalhao c) {
        return new RecomendacaoRecente(r.id(),
                (r.adotada() ? r.dataAdocao() : r.dataGeracao()).toLocalDate(),
                r.status().name(), r.entrada().culturaAlvo().nome(), c.talhaoIdentificacao(), c.propriedadeNome(),
                r.resultado().calagem().necessidadeCalcarioTonHa());
    }
}
