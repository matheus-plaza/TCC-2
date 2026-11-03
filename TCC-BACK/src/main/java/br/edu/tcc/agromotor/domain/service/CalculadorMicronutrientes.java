package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.Decimais;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoMicronutriente;
import br.edu.tcc.agromotor.domain.model.parametro.RegraMicronutriente;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.edu.tcc.agromotor.domain.model.Decimais.formatar;

/**
 * Micronutrientes pela classe do teor no solo: abaixo do nível crítico (Alto), a dose
 * de correção da tabela; a partir dele, zero. O manual só traz dose para a soja, e os
 * teores são opcionais no laudo, então a falta de um ou outro deixa o nutriente
 * "não avaliado", sem interromper a recomendação.
 */
public final class CalculadorMicronutrientes implements CalculadorAgronomico<List<ResultadoMicronutriente>> {

    @Override
    public List<ResultadoMicronutriente> calcular(EntradaRecomendacao entrada, ParametroRepository parametros) {
        LaudoSolo laudo = entrada.laudo();
        Cultura alvo = entrada.culturaAlvo();
        ClassificadorFertilidade classificador = new ClassificadorFertilidade(parametros);
        List<ResultadoMicronutriente> resultados = new ArrayList<>();
        for (Micronutriente nutriente : Micronutriente.values()) {
            BigDecimal teor = laudo.teor(nutriente);
            List<RegraMicronutriente> regras = parametros.regrasMicronutriente(alvo.id(), nutriente);
            if (regras.isEmpty()) {
                resultados.add(new ResultadoMicronutriente(nutriente, false, teor, null, null,
                        "O manual não indica dose de %s para %s.".formatted(nutriente.rotulo(), alvo.nome()), false));
            } else if (teor == null) {
                resultados.add(new ResultadoMicronutriente(nutriente, false, null, null, null,
                        "%s não informado no laudo.".formatted(nutriente.rotulo()), true));
            } else {
                resultados.add(avaliar(nutriente, teor, regras, classificador.classeObrigatoria(laudo, atributo(nutriente), teor), alvo));
            }
        }
        return resultados;
    }

    private static ResultadoMicronutriente avaliar(Micronutriente nutriente, BigDecimal teor, List<RegraMicronutriente> regras,
                                                   String classe, Cultura alvo) {
        RegraMicronutriente regra = regras.stream()
                .filter(r -> r.classe().equalsIgnoreCase(classe))
                .findFirst()
                .orElseThrow(() -> new ParametroAusenteException(
                        "Dose de %s para %s na classe %s".formatted(nutriente.rotulo(), alvo.nome(), classe)));
        BigDecimal dose = Decimais.valor(regra.doseKgHa());
        String justificativa = "Teor de %s mg/dm³, classe %s: %s kg/ha.".formatted(formatar(teor), classe, formatar(dose));
        if (regra.observacao() != null) {
            justificativa += " " + regra.observacao();
        }
        return new ResultadoMicronutriente(nutriente, true, teor, classe, dose, justificativa, true);
    }

    private static AtributoSolo atributo(Micronutriente nutriente) {
        return switch (nutriente) {
            case ZINCO -> AtributoSolo.ZINCO;
            case BORO -> AtributoSolo.BORO;
            case COBRE -> AtributoSolo.COBRE;
            case MANGANES -> AtributoSolo.MANGANES;
        };
    }
}
