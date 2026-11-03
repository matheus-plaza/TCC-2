package br.edu.tcc.agromotor.application.usecase.recomendacao;

import br.edu.tcc.agromotor.application.dto.CulturaResponse;
import br.edu.tcc.agromotor.application.dto.LaudoResponse;
import br.edu.tcc.agromotor.application.dto.RecomendacaoResponseDTO;
import br.edu.tcc.agromotor.application.dto.TalhaoResponse;
import br.edu.tcc.agromotor.application.usecase.UseCase;
import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.model.Usuario;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoAdubacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoCalagem;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoGessagem;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.domain.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Converte o cenário e o resultado do motor no contrato da API. */
@UseCase
public class MontadorRecomendacao {

    private final TalhaoRepository talhoes;
    private final UsuarioRepository usuarios;

    public MontadorRecomendacao(TalhaoRepository talhoes, UsuarioRepository usuarios) {
        this.talhoes = talhoes;
        this.usuarios = usuarios;
    }

    public RecomendacaoResponseDTO simulacao(EntradaRecomendacao entrada, ResultadoRecomendacao resultado, LocalDateTime agora) {
        return montar(null, StatusRecomendacao.SIMULACAO, entrada, resultado, agora, null, null);
    }

    public RecomendacaoResponseDTO registrada(Recomendacao r) {
        return montar(r.id(), r.status(), r.entrada(), r.resultado(), r.dataGeracao(), r.dataAdocao(),
                r.adotadoPorId() != null ? r.adotadoPorId() : r.geradoPorId());
    }

    private RecomendacaoResponseDTO montar(Long id, StatusRecomendacao status, EntradaRecomendacao entrada,
                                           ResultadoRecomendacao resultado, LocalDateTime dataGeracao,
                                           LocalDateTime dataAdocao, Long responsavelId) {
        LaudoSolo laudo = entrada.laudo();
        ContextoTalhao contexto = talhoes.contexto(laudo.talhaoId()).orElse(null);
        IndicadoresSolo ind = resultado.indicadores();
        ResultadoCalagem c = resultado.calagem();
        ResultadoGessagem g = resultado.gessagem();
        ResultadoAdubacao a = resultado.adubacao();

        return new RecomendacaoResponseDTO(
                id,
                status,
                laudo.id(),
                new RecomendacaoResponseDTO.ResumoQuimico(ind.somaBases(), ind.ctcEfetiva(), ind.ctcTotal(),
                        ind.saturacaoBases(), ind.saturacaoAluminio()),
                new RecomendacaoResponseDTO.Calagem(c.necessario(), c.v2AlvoPorcentagem(), c.necessidadeCalcarioTonHa(),
                        c.ncCalculadaTonHa(), c.observacao(), c.justificativa()),
                new RecomendacaoResponseDTO.Gessagem(g.necessario(), g.doseKgHa(), g.avaliada(), g.justificativa()),
                new RecomendacaoResponseDTO.Adubacao(a.doseNkgHa(), a.doseP2O5kgHa(), a.doseK2OkgHa(), a.justificativaN(),
                        a.classeFosforo(), a.justificativaP(), a.classePotassio(), a.justificativaK(), a.classeMateriaOrganica()),
                resultado.micronutrientes().stream()
                        .map(m -> new RecomendacaoResponseDTO.Micronutriente(m.nutriente().rotulo(), m.nutriente().simbolo(),
                                m.avaliado(), m.teor(), m.classe(), m.doseKgHa(), m.justificativa()))
                        .toList(),
                CulturaResponse.de(entrada.culturaAlvo()),
                CulturaResponse.de(entrada.culturaAnterior()),
                entrada.grupoAnterior(),
                entrada.descricaoAnterior(),
                entrada.manejo(),
                entrada.expectativaRendimentoTonHa(),
                entrada.prntCalcario(),
                contexto == null ? null : TalhaoResponse.de(contexto),
                LaudoResponse.de(laudo, contexto, List.of()),
                entrada.laudoSubsuperficie() == null ? null : entrada.laudoSubsuperficie().id(),
                resultado.versaoParametros(),
                dataGeracao,
                dataAdocao,
                responsavel(responsavelId),
                resultado.avisos(),
                resultado.memoriaCalculo());
    }

    private RecomendacaoResponseDTO.Responsavel responsavel(Long id) {
        if (id == null) {
            return null;
        }
        return usuarios.buscarPorId(id)
                .map((Usuario u) -> new RecomendacaoResponseDTO.Responsavel(u.id(), u.nome(), u.registroProfissional()))
                .orElse(null);
    }
}
