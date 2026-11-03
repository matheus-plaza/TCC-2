package br.edu.tcc.agromotor.domain.model.calculo;

import br.edu.tcc.agromotor.domain.model.IndicadoresSolo;
import br.edu.tcc.agromotor.domain.model.VersaoParametros;

import java.util.List;

/** Recomendação completa proposta pelo motor, com a memória de cálculo para auditoria. */
public record ResultadoRecomendacao(
        IndicadoresSolo indicadores,
        ResultadoCalagem calagem,
        ResultadoGessagem gessagem,
        ResultadoAdubacao adubacao,
        List<ResultadoMicronutriente> micronutrientes,
        List<String> avisos,
        List<String> memoriaCalculo,
        VersaoParametros versaoParametros) {
}
