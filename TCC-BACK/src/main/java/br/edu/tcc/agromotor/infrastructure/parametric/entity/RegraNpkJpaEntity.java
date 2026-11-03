package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.FaixaDose;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import br.edu.tcc.agromotor.domain.model.NutrienteNpk;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import br.edu.tcc.agromotor.domain.model.parametro.RegraNpk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;

@Entity
@Table(name = "nepar_regra_npk")
public class RegraNpkJpaEntity {

    @Id
    private Long id;

    @Column(name = "versao_id")
    private Long versaoId;

    @Column(name = "cultura_id")
    private Long culturaId;

    @Enumerated(EnumType.STRING)
    private NutrienteNpk nutriente;

    @Column(name = "classe_fertilidade")
    private String classeFertilidade;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "grupo_antecessor", columnDefinition = "grupo_botanico")
    private GrupoBotanico grupoAntecessor;

    @Column(name = "faixa_rendimento_min")
    private BigDecimal faixaRendimentoMin;

    @Column(name = "faixa_rendimento_max")
    private BigDecimal faixaRendimentoMax;

    @Column(name = "dose_min")
    private BigDecimal doseMin;

    @Column(name = "dose_max")
    private BigDecimal doseMax;

    protected RegraNpkJpaEntity() {
    }

    public RegraNpk paraDominio() {
        return new RegraNpk(culturaId, nutriente, classeFertilidade, grupoAntecessor,
                new Intervalo(faixaRendimentoMin, faixaRendimentoMax),
                doseMin == null ? null : new FaixaDose(doseMin, doseMax));
    }
}
