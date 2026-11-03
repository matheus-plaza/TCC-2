package br.edu.tcc.agromotor.application.dto;

import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.MetodoPh;
import br.edu.tcc.agromotor.domain.model.calculo.InterpretacaoAtributo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LaudoResponse(
        Long id,
        Long talhaoId,
        String talhaoIdentificacao,
        Long propriedadeId,
        String propriedadeNome,
        Long produtorId,
        String produtorNome,
        LocalDate dataAnalise,
        String profundidadeCm,
        BigDecimal ph,
        MetodoPh metodoPh,
        BigDecimal moGDm3,
        BigDecimal pMehlich,
        BigDecimal kCmolc,
        BigDecimal caCmolc,
        BigDecimal mgCmolc,
        BigDecimal alCmolc,
        BigDecimal hAlCmolc,
        BigDecimal argilaPorcentagem,
        BigDecimal znMgDm3,
        BigDecimal bMgDm3,
        BigDecimal cuMgDm3,
        BigDecimal mnMgDm3,
        IndicadoresSolo indicadores,
        List<ItemInterpretacao> interpretacao) {

    public record ItemInterpretacao(String atributo, String rotulo, BigDecimal valor, String unidade, String classe) {

        static ItemInterpretacao de(InterpretacaoAtributo i) {
            return new ItemInterpretacao(i.atributo().name(), i.atributo().rotulo(), i.valor(), i.atributo().unidade(), i.classe());
        }
    }

    /** {@code interpretacao} vazia nas listagens; o detalhe do laudo traz o enquadramento completo. */
    public static LaudoResponse de(LaudoSolo l, ContextoTalhao c, List<InterpretacaoAtributo> interpretacao) {
        return new LaudoResponse(l.id(), l.talhaoId(),
                c == null ? null : c.talhaoIdentificacao(),
                c == null ? null : c.propriedadeId(),
                c == null ? null : c.propriedadeNome(),
                c == null ? null : c.produtorId(),
                c == null ? null : c.produtorNome(),
                l.dataAnalise(), l.profundidade().codigo(), l.ph(), l.metodoPh(), l.moGDm3(), l.pMehlich(), l.kCmolc(),
                l.caCmolc(), l.mgCmolc(), l.alCmolc(), l.hAlCmolc(), l.argilaPorcentagem(), l.znMgDm3(), l.bMgDm3(),
                l.cuMgDm3(), l.mnMgDm3(),
                l.indicadores(),
                interpretacao.stream().map(ItemInterpretacao::de).toList());
    }
}
