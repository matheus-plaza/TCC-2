package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoMicronutriente;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de uma recomendação: as colunas guardam os resultados consultáveis e
 * {@code snapshot_auditoria} guarda a cópia integral de entradas e resultado (RNF06).
 */
@Entity
@Table(name = "recomendacoes")
public class RecomendacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analise_id")
    private Long analiseId;

    @Column(name = "cultura_alvo_id")
    private Long culturaAlvoId;

    @Column(name = "cultura_anterior_id")
    private Long culturaAnteriorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grupo_anterior")
    private GrupoBotanico grupoAnterior;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "sistema_manejo")
    private SistemaManejo manejo;

    @Column(name = "expectativa_rendimento")
    private BigDecimal expectativaRendimento;

    @Column(name = "prnt_calcario")
    private BigDecimal prntCalcario;

    @Column(name = "v1_atual")
    private BigDecimal v1Atual;

    @Column(name = "necessidade_calcario_ton_ha")
    private BigDecimal necessidadeCalcarioTonHa;

    @Column(name = "necessidade_gesso_kg_ha")
    private BigDecimal necessidadeGessoKgHa;

    @Column(name = "dose_n_max_kg_ha")
    private BigDecimal doseNMaxKgHa;

    @Column(name = "dose_p2o5_max_kg_ha")
    private BigDecimal doseP2o5MaxKgHa;

    @Column(name = "dose_k2o_max_kg_ha")
    private BigDecimal doseK2oMaxKgHa;

    @Column(name = "dose_n_min_kg_ha")
    private BigDecimal doseNMinKgHa;

    @Column(name = "dose_p2o5_min_kg_ha")
    private BigDecimal doseP2o5MinKgHa;

    @Column(name = "dose_k2o_min_kg_ha")
    private BigDecimal doseK2oMinKgHa;

    @Column(name = "dose_zn_kg_ha")
    private BigDecimal doseZnKgHa;

    @Column(name = "dose_b_kg_ha")
    private BigDecimal doseBKgHa;

    @Column(name = "dose_cu_kg_ha")
    private BigDecimal doseCuKgHa;

    @Column(name = "dose_mn_kg_ha")
    private BigDecimal doseMnKgHa;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "status_recomendacao")
    private StatusRecomendacao status;

    @Column(name = "data_geracao")
    private LocalDateTime dataGeracao;

    @Column(name = "versao_parametros_id")
    private Long versaoParametrosId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_auditoria", columnDefinition = "jsonb")
    private String snapshotAuditoria;

    @Column(name = "gerado_por_id")
    private Long geradoPorId;

    @Column(name = "adotado_por_id")
    private Long adotadoPorId;

    @Column(name = "data_adocao")
    private LocalDateTime dataAdocao;

    protected RecomendacaoJpaEntity() {
    }

    public static RecomendacaoJpaEntity de(Recomendacao r, String snapshotJson) {
        ResultadoRecomendacao resultado = r.resultado();
        RecomendacaoJpaEntity e = new RecomendacaoJpaEntity();
        e.analiseId = r.entrada().laudo().id();
        e.culturaAlvoId = r.entrada().culturaAlvo().id();
        e.culturaAnteriorId = r.entrada().culturaAnterior() == null ? null : r.entrada().culturaAnterior().id();
        e.grupoAnterior = r.entrada().grupoAnteriorInformado();
        e.manejo = r.entrada().manejo();
        e.expectativaRendimento = r.entrada().expectativaRendimentoTonHa();
        e.prntCalcario = r.entrada().prntCalcario();
        e.v1Atual = resultado.indicadores().saturacaoBases();
        e.necessidadeCalcarioTonHa = resultado.calagem().necessidadeCalcarioTonHa();
        e.necessidadeGessoKgHa = resultado.gessagem().doseKgHa();
        e.doseNMinKgHa = resultado.adubacao().doseNkgHa().min();
        e.doseNMaxKgHa = resultado.adubacao().doseNkgHa().max();
        e.doseP2o5MinKgHa = resultado.adubacao().doseP2O5kgHa().min();
        e.doseP2o5MaxKgHa = resultado.adubacao().doseP2O5kgHa().max();
        e.doseK2oMinKgHa = resultado.adubacao().doseK2OkgHa().min();
        e.doseK2oMaxKgHa = resultado.adubacao().doseK2OkgHa().max();
        e.doseZnKgHa = dose(resultado, Micronutriente.ZINCO);
        e.doseBKgHa = dose(resultado, Micronutriente.BORO);
        e.doseCuKgHa = dose(resultado, Micronutriente.COBRE);
        e.doseMnKgHa = dose(resultado, Micronutriente.MANGANES);
        e.status = r.status();
        e.dataGeracao = r.dataGeracao();
        e.versaoParametrosId = resultado.versaoParametros().id();
        e.snapshotAuditoria = snapshotJson;
        e.geradoPorId = r.geradoPorId();
        e.adotadoPorId = r.adotadoPorId();
        e.dataAdocao = r.dataAdocao();
        return e;
    }

    /** A única mudança permitida em um registro: a adoção. */
    public void registrarAdocao(Recomendacao r) {
        status = r.status();
        adotadoPorId = r.adotadoPorId();
        dataAdocao = r.dataAdocao();
    }

    private static BigDecimal dose(ResultadoRecomendacao resultado, Micronutriente nutriente) {
        return resultado.micronutrientes().stream()
                .filter(m -> m.nutriente() == nutriente)
                .map(ResultadoMicronutriente::doseKgHa)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public Long id() {
        return id;
    }

    public StatusRecomendacao status() {
        return status;
    }

    public LocalDateTime dataGeracao() {
        return dataGeracao;
    }

    public String snapshotAuditoria() {
        return snapshotAuditoria;
    }

    public Long geradoPorId() {
        return geradoPorId;
    }

    public Long adotadoPorId() {
        return adotadoPorId;
    }

    public LocalDateTime dataAdocao() {
        return dataAdocao;
    }
}
