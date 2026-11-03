package br.edu.tcc.agromotor.domain.service;

import br.edu.tcc.agromotor.domain.exception.ParametroAusenteException;
import br.edu.tcc.agromotor.domain.exception.RegraNegocioException;
import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.NutrienteNpk;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoAdubacao;
import br.edu.tcc.agromotor.domain.model.parametro.RegraNpk;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.edu.tcc.agromotor.domain.model.Decimais.formatar;
import static br.edu.tcc.agromotor.domain.model.Decimais.formatarPercentual;

/**
 * Adubação NPK pelas tabelas do manual, que dão a dose em faixas.
 * <ul>
 *   <li>P₂O₅: classe de P (teor × argila) e produtividade esperada;</li>
 *   <li>K₂O: classe de K (porcentagem de K na CTC) e produtividade esperada;</li>
 *   <li>N: leguminosa não recebe N mineral (FBN). Nas demais, a tabela cruza a cultura
 *   anterior e a produtividade; no trigo, também a classe de M.O.</li>
 * </ul>
 * Uma célula "Inviável" ou um cenário sem linha na tabela de N recusam a recomendação,
 * porque o manual não traz dose para eles.
 */
public final class CalculadorAdubacaoNPK implements CalculadorAgronomico<ResultadoAdubacao> {

    @Override
    public ResultadoAdubacao calcular(EntradaRecomendacao entrada, ParametroRepository parametros) {
        LaudoSolo laudo = entrada.laudo();
        ClassificadorFertilidade classificador = new ClassificadorFertilidade(parametros);
        Calculo calculo = new Calculo(entrada, parametros.regrasNpk(entrada.culturaAlvo().id()));

        String classeP = calculo.classe(classificador, AtributoSolo.FOSFORO, laudo.pMehlich());
        String classeK = calculo.classe(classificador, AtributoSolo.POTASSIO, laudo.kCmolc());
        String classeMo = calculo.classe(classificador, AtributoSolo.MATERIA_ORGANICA, laudo.moGDm3());

        FaixaDose doseP = calculo.dose(NutrienteNpk.P2O5, classeP, null, "P " + classeP);
        FaixaDose doseK = calculo.dose(NutrienteNpk.K2O, classeK, null, "K " + classeK);
        Nitrogenio n = calculo.nitrogenio(classeMo);

        calculo.falharSeNecessario();

        return new ResultadoAdubacao(n.dose(), doseP, doseK, classeP, classeK, classeMo, n.justificativa(),
                "Teor de P %s com argila de %s.".formatted(classeP, formatarPercentual(laudo.argilaPorcentagem())),
                "K de %s cmolc/dm³ equivale a %s da CTC: classe %s.".formatted(
                        formatar(laudo.kCmolc()), formatarPercentual(laudo.porcentagemKNaCtc()), classeK),
                calculo.memoria);
    }

    private record Nitrogenio(FaixaDose dose, String justificativa) {
    }

    /** Estado de uma execução: o calculador em si não guarda nada entre chamadas. */
    private static final class Calculo {

        private final EntradaRecomendacao entrada;
        private final List<RegraNpk> regras;
        private final List<String> pendencias = new ArrayList<>();
        private final List<String> impedimentos = new ArrayList<>();
        private final List<String> memoria = new ArrayList<>();

        private Calculo(EntradaRecomendacao entrada, List<RegraNpk> regras) {
            this.entrada = entrada;
            this.regras = regras;
        }

        private String classe(ClassificadorFertilidade classificador, AtributoSolo atributo, BigDecimal teor) {
            try {
                return classificador.classeObrigatoria(entrada.laudo(), atributo, teor);
            } catch (ParametroAusenteException e) {
                pendencias.addAll(e.pendencias());
                return null;
            }
        }

        private Nitrogenio nitrogenio(String classeMo) {
            Cultura alvo = entrada.culturaAlvo();
            GrupoBotanico grupo = entrada.grupoAnterior();
            if (alvo.leguminosa()) {
                memoria.add("N: %s é leguminosa, Fixação Biológica de Nitrogênio dispensa N mineral: 0,00 kg/ha".formatted(alvo.nome()));
                return new Nitrogenio(FaixaDose.zero(), justificativaFbn(grupo));
            }
            if (grupo == null) {
                impedimentos.add("O manual não traz dose de N para %s sem cultura anterior: selecione uma gramínea ou uma leguminosa."
                        .formatted(alvo.nome()));
                return new Nitrogenio(null, null);
            }
            FaixaDose dose = dose(NutrienteNpk.N, classeMo, grupo, "M.O. " + classeMo);
            String efeito = grupo == GrupoBotanico.LEGUMINOSA ? "crédito" : "débito";
            String anterior = entrada.culturaAnterior() == null
                    ? entrada.descricaoAnterior()
                    : "%s (%s)".formatted(entrada.descricaoAnterior(), grupo.rotulo());
            return new Nitrogenio(dose, "Cultura anterior %s: %s de N já considerado na tabela do manual.".formatted(anterior, efeito));
        }

        private static String justificativaFbn(GrupoBotanico grupo) {
            if (grupo == null) {
                return "FBN ativada. Antecessora: pousio/palhada.";
            }
            return grupo == GrupoBotanico.GRAMINEA
                    ? "FBN ativada. Antecessora Gramínea compensada."
                    : "FBN ativada. Antecessora Leguminosa.";
        }

        private FaixaDose dose(NutrienteNpk nutriente, String classe, GrupoBotanico antecessor, String condicao) {
            if (classe == null) {
                return null; // a falta da classe já foi registrada como pendência
            }
            Cultura alvo = entrada.culturaAlvo();
            BigDecimal rendimento = entrada.expectativaRendimentoTonHa();
            List<RegraNpk> aplicaveis = regras.stream()
                    .filter(r -> r.aplicaA(nutriente, classe, antecessor, rendimento))
                    .toList();
            if (aplicaveis.size() != 1) {
                String problema = aplicaveis.isEmpty() ? "Dose de" : "Regras sobrepostas de";
                pendencias.add("%s %s para %s, %s e rendimento de %s t/ha".formatted(
                        problema, nutriente.rotulo(), alvo.nome(), condicao, formatar(rendimento)));
                return null;
            }
            RegraNpk regra = aplicaveis.getFirst();
            if (regra.inviavel()) {
                impedimentos.add("Produtividade de %s t/ha é inviável para %s com %s no solo, segundo o manual.".formatted(
                        formatar(rendimento), alvo.nome(), condicao));
                return null;
            }
            memoria.add("%s: %s, %s, rendimento %s t/ha = %s kg/ha".formatted(
                    nutriente.rotulo(), alvo.nome(), descricaoDaLinha(regra, antecessor, condicao), formatar(rendimento), regra.dose().descricao()));
            return regra.dose();
        }

        private static String descricaoDaLinha(RegraNpk regra, GrupoBotanico antecessor, String condicao) {
            if (antecessor == null) {
                return condicao;
            }
            String anterior = "cultura anterior " + antecessor.rotulo();
            return regra.classeFertilidade() == null ? anterior : anterior + ", " + condicao;
        }

        private void falharSeNecessario() {
            if (!pendencias.isEmpty()) {
                throw new ParametroAusenteException(pendencias);
            }
            if (!impedimentos.isEmpty()) {
                throw new RegraNegocioException(impedimentos);
            }
        }
    }
}
