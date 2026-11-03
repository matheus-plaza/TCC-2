package br.edu.tcc.agromotor.domain;

import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.NutrienteNpk;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.VersaoParametros;
import br.edu.tcc.agromotor.domain.model.parametro.DoseGesso;
import br.edu.tcc.agromotor.domain.model.parametro.FaixaInterpretacao;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import br.edu.tcc.agromotor.domain.model.parametro.RegraCalagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraGessagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraMicronutriente;
import br.edu.tcc.agromotor.domain.model.parametro.RegraNpk;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tabelas paramétricas em memória para testar o motor sem banco. Cada teste cadastra
 * só as linhas que usa: aqui se verifica a lógica de cruzamento das tabelas. O conteúdo
 * publicado do manual é verificado em {@code ManualNeparIntegracaoTest}.
 */
public final class ParametrosEmMemoria implements ParametroRepository {

    private VersaoParametros versao = new VersaoParametros(1L, "TESTE", "Parâmetros de teste", false);
    private final List<FaixaInterpretacao> faixas = new ArrayList<>();
    private final List<RegraNpk> regrasNpk = new ArrayList<>();
    private final List<RegraCalagem> regrasCalagem = new ArrayList<>();
    private final List<RegraMicronutriente> micros = new ArrayList<>();
    private final List<DoseGesso> dosesGesso = new ArrayList<>();
    private RegraGessagem gessagem;

    public static BigDecimal d(String valor) {
        return valor == null ? null : new BigDecimal(valor);
    }

    public static Intervalo faixa(String min, String max) {
        return new Intervalo(d(min), d(max));
    }

    public ParametrosEmMemoria semVersao() {
        versao = null;
        return this;
    }

    public ParametrosEmMemoria comFaixa(AtributoSolo atributo, Intervalo criterio, String teorMin, String teorMax, String classe) {
        faixas.add(new FaixaInterpretacao(atributo, criterio, faixa(teorMin, teorMax), classe));
        return this;
    }

    /** {@code doseMin} nulo representa uma célula "Inviável". */
    public ParametrosEmMemoria comRegraNpk(Long culturaId, NutrienteNpk nutriente, String classe, GrupoBotanico grupo,
                                           String rendMin, String rendMax, String doseMin, String doseMax) {
        regrasNpk.add(new RegraNpk(culturaId, nutriente, classe, grupo, faixa(rendMin, rendMax),
                doseMin == null ? null : new FaixaDose(d(doseMin), d(doseMax))));
        return this;
    }

    public ParametrosEmMemoria comGessagem(String limiteM, String limiteCa) {
        gessagem = new RegraGessagem(d(limiteM), d(limiteCa));
        return this;
    }

    public ParametrosEmMemoria comDoseGesso(String argilaMin, String argilaMax, String dose) {
        dosesGesso.add(new DoseGesso(faixa(argilaMin, argilaMax), d(dose)));
        return this;
    }

    /** Critérios e doses de gesso da p. 56 do manual. */
    public ParametrosEmMemoria comGessoDoManual() {
        return comGessagem("20", "0.5")
                .comDoseGesso("0", "20.01", "700")
                .comDoseGesso("20.01", "40.01", "1200")
                .comDoseGesso("40.01", "60.01", "2200")
                .comDoseGesso("60.01", null, "3200");
    }

    public ParametrosEmMemoria comRegraCalagem(SistemaManejo manejo, String observacao) {
        regrasCalagem.add(new RegraCalagem(manejo, observacao));
        return this;
    }

    public ParametrosEmMemoria comMicro(Long culturaId, Micronutriente nutriente, String classe, String dose, String observacao) {
        micros.add(new RegraMicronutriente(culturaId, nutriente, classe, d(dose), observacao));
        return this;
    }

    @Override
    public Optional<VersaoParametros> versaoVigente() {
        return Optional.ofNullable(versao);
    }

    @Override
    public List<FaixaInterpretacao> faixasInterpretacao(AtributoSolo atributo) {
        return faixas.stream().filter(f -> f.atributo() == atributo).toList();
    }

    @Override
    public List<RegraNpk> regrasNpk(Long culturaId) {
        return regrasNpk.stream().filter(r -> r.culturaId().equals(culturaId)).toList();
    }

    @Override
    public Optional<RegraGessagem> regraGessagem() {
        return Optional.ofNullable(gessagem);
    }

    @Override
    public List<DoseGesso> dosesGesso() {
        return List.copyOf(dosesGesso);
    }

    @Override
    public Optional<RegraCalagem> regraCalagem(SistemaManejo manejo) {
        return regrasCalagem.stream().filter(r -> r.manejo() == manejo).findFirst();
    }

    @Override
    public List<RegraMicronutriente> regrasMicronutriente(Long culturaId, Micronutriente nutriente) {
        return micros.stream()
                .filter(m -> m.culturaId().equals(culturaId) && m.nutriente() == nutriente)
                .toList();
    }
}
