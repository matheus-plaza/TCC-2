package br.edu.tcc.agromotor.application.usecase.recomendacao;

import br.edu.tcc.agromotor.application.dto.RecomendacaoRequestDTO;
import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.agromotor.application.dto.UsuarioAutenticado;
import br.edu.tcc.agromotor.application.exception.RecursoNaoEncontradoException;
import br.edu.tcc.agromotor.application.usecase.ControleAcesso;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.PerfilUsuario;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;
import br.edu.tcc.agromotor.domain.repository.CulturaRepository;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.domain.service.EngineCalculoAgronomico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

/**
 * RF05 / UC08: executa o motor em memória, sem nenhuma escrita no banco. Também
 * registra um cenário como SIMULACAO, recalculando no servidor: o resultado gravado
 * nunca vem do cliente, de modo que a adoção (RF06) parte de um cálculo confiável.
 */
@UseCase
public class ProcessarRecomendacaoUseCase {

    private final LaudoRepository laudos;
    private final TalhaoRepository talhoes;
    private final CulturaRepository culturas;
    private final RecomendacaoRepository recomendacoes;
    private final EngineCalculoAgronomico engine;
    private final MontadorRecomendacao montador;
    private final ControleAcesso acesso;

    public ProcessarRecomendacaoUseCase(LaudoRepository laudos, TalhaoRepository talhoes, CulturaRepository culturas,
                                        RecomendacaoRepository recomendacoes, EngineCalculoAgronomico engine,
                                        MontadorRecomendacao montador, ControleAcesso acesso) {
        this.laudos = laudos;
        this.talhoes = talhoes;
        this.culturas = culturas;
        this.recomendacoes = recomendacoes;
        this.engine = engine;
        this.montador = montador;
        this.acesso = acesso;
    }

    public RecomendacaoResponseDTO simular(UsuarioAutenticado ator, RecomendacaoRequestDTO request) {
        acesso.exigirPerfil(ator, PerfilUsuario.AGRONOMO, PerfilUsuario.ADMIN);
        EntradaRecomendacao entrada = montarEntrada(ator, request);
        return montador.simulacao(entrada, engine.processar(entrada), LocalDateTime.now());
    }

    public RecomendacaoResponseDTO registrarSimulacao(UsuarioAutenticado ator, RecomendacaoRequestDTO request) {
        acesso.exigirPerfil(ator, PerfilUsuario.AGRONOMO);
        EntradaRecomendacao entrada = montarEntrada(ator, request);
        ResultadoRecomendacao resultado = engine.processar(entrada);
        return montador.registrada(recomendacoes.salvar(Recomendacao.simulacao(entrada, resultado, ator.id(), LocalDateTime.now())));
    }

    private EntradaRecomendacao montarEntrada(UsuarioAutenticado ator, RecomendacaoRequestDTO r) {
        LaudoSolo laudo = laudos.buscarPorId(r.analiseSoloId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laudo", r.analiseSoloId()));
        ContextoTalhao contexto = talhoes.contexto(laudo.talhaoId()).orElseThrow();
        acesso.exigirAcessoAoProdutor(ator, contexto.produtorId());

        Cultura alvo = culturas.buscarPorId(r.culturaAlvoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cultura", r.culturaAlvoId()));
        Cultura anterior = r.culturaAnteriorId() == null ? null : culturas.buscarPorId(r.culturaAnteriorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cultura", r.culturaAnteriorId()));
        BigDecimal prnt = r.prntCalcario() == null ? EntradaRecomendacao.PRNT_PADRAO : r.prntCalcario();

        return new EntradaRecomendacao(laudo, subsuperficieMaisProxima(ator, laudo), alvo, anterior, r.sistemaManejo(),
                r.expectativaRendimentoTonHa(), prnt, r.grupoAnterior());
    }

    /** Para a gessagem: laudo de 20-40 cm do mesmo talhão com a data mais próxima. */
    private LaudoSolo subsuperficieMaisProxima(UsuarioAutenticado ator, LaudoSolo laudo) {
        EscopoProdutores escopo = acesso.escopo(ator);
        return laudos.listar(escopo, new FiltroLaudos(laudo.talhaoId(), ProfundidadeAmostra.CAMADA_20_40, null, null)).stream()
                .min(Comparator.comparingLong(
                        (LaudoSolo l) -> Math.abs(ChronoUnit.DAYS.between(l.dataAnalise(), laudo.dataAnalise()))))
                .orElse(null);
    }
}
