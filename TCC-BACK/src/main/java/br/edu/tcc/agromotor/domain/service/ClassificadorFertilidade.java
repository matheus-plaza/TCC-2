package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Decimais;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.calculo.InterpretacaoAtributo;
import br.edu.tcc.agromotor.domain.model.parametro.FaixaInterpretacao;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enquadra os teores do laudo nas classes de fertilidade (RF04). P é cruzado com a
 * argila e K é enquadrado pela sua porcentagem na CTC a pH 7,0; os limites vêm das
 * tabelas paramétricas.
 */
public final class ClassificadorFertilidade {

    private final ParametroRepository parametros;

    public ClassificadorFertilidade(ParametroRepository parametros) {
        this.parametros = parametros;
    }

    /** Interpretação completa do laudo. Valor não coberto pela tabela fica sem classe. */
    public List<InterpretacaoAtributo> interpretar(LaudoSolo laudo) {
        List<InterpretacaoAtributo> interpretacao = new ArrayList<>();
        var indicadores = laudo.indicadores();
        adicionar(interpretacao, laudo, laudo.metodoPh().atributo(), laudo.ph());
        adicionar(interpretacao, laudo, AtributoSolo.MATERIA_ORGANICA, laudo.moGDm3());
        adicionar(interpretacao, laudo, AtributoSolo.FOSFORO, laudo.pMehlich());
        adicionar(interpretacao, laudo, AtributoSolo.POTASSIO, laudo.kCmolc());
        adicionar(interpretacao, laudo, AtributoSolo.CALCIO, laudo.caCmolc());
        adicionar(interpretacao, laudo, AtributoSolo.MAGNESIO, laudo.mgCmolc());
        adicionar(interpretacao, laudo, AtributoSolo.SATURACAO_BASES, indicadores.saturacaoBases());
        adicionar(interpretacao, laudo, AtributoSolo.SATURACAO_ALUMINIO, indicadores.saturacaoAluminio());
        adicionar(interpretacao, laudo, AtributoSolo.ZINCO, laudo.znMgDm3());
        adicionar(interpretacao, laudo, AtributoSolo.BORO, laudo.bMgDm3());
        adicionar(interpretacao, laudo, AtributoSolo.COBRE, laudo.cuMgDm3());
        adicionar(interpretacao, laudo, AtributoSolo.MANGANES, laudo.mnMgDm3());
        return interpretacao;
    }

    /** Classe obrigatória para o cálculo: falta ou sobreposição de faixas interrompem. */
    public String classeObrigatoria(LaudoSolo laudo, AtributoSolo atributo, BigDecimal teor) {
        return faixa(laudo, atributo, teor)
                .map(FaixaInterpretacao::classe)
                .orElseThrow(() -> new ParametroAusenteException(
                        "Faixa de interpretação de %s para o teor %s%s".formatted(
                                atributo.rotulo(), Decimais.formatar(teor), descricaoCriterio(laudo, atributo))));
    }

    private void adicionar(List<InterpretacaoAtributo> lista, LaudoSolo laudo, AtributoSolo atributo, BigDecimal valor) {
        if (valor != null) {
            lista.add(new InterpretacaoAtributo(atributo, valor,
                    faixa(laudo, atributo, valor).map(FaixaInterpretacao::classe).orElse(null)));
        }
    }

    private Optional<FaixaInterpretacao> faixa(LaudoSolo laudo, AtributoSolo atributo, BigDecimal teor) {
        BigDecimal criterio = valorCriterio(laudo, atributo);
        BigDecimal valor = valorEnquadrado(laudo, atributo, teor);
        List<FaixaInterpretacao> aplicaveis = parametros.faixasInterpretacao(atributo).stream()
                .filter(f -> f.aplicaA(criterio, valor))
                .toList();
        if (aplicaveis.size() > 1) {
            throw new ParametroAusenteException(
                    "Faixas sobrepostas (%d) de %s para o teor %s%s".formatted(
                            aplicaveis.size(), atributo.rotulo(), Decimais.formatar(teor), descricaoCriterio(laudo, atributo)));
        }
        return aplicaveis.stream().findFirst();
    }

    /** O potássio é enquadrado pela porcentagem de K na CTC; os demais, pelo próprio teor. */
    private static BigDecimal valorEnquadrado(LaudoSolo laudo, AtributoSolo atributo, BigDecimal teor) {
        return atributo == AtributoSolo.POTASSIO ? laudo.porcentagemKNaCtc() : teor;
    }

    private static BigDecimal valorCriterio(LaudoSolo laudo, AtributoSolo atributo) {
        return atributo == AtributoSolo.FOSFORO ? laudo.argilaPorcentagem() : BigDecimal.ZERO;
    }

    private static String descricaoCriterio(LaudoSolo laudo, AtributoSolo atributo) {
        return switch (atributo) {
            case FOSFORO -> " e argila " + Decimais.formatar(laudo.argilaPorcentagem()) + "%";
            case POTASSIO -> " (" + Decimais.formatarPercentual(laudo.porcentagemKNaCtc()) + " da CTC)";
            default -> "";
        };
    }
}
