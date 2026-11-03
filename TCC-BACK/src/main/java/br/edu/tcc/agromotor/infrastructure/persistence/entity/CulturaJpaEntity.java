package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.Cultura;
import br.edu.tcc.agromotor.domain.model.GrupoBotanico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;

@Entity
@Table(name = "culturas")
public class CulturaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(name = "v_alvo")
    private BigDecimal vAlvo;

    @Column(name = "v_minimo")
    private BigDecimal vMinimo;

    @Column(name = "ph_min_ideal")
    private BigDecimal phMinIdeal;

    @Column(name = "ph_max_ideal")
    private BigDecimal phMaxIdeal;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "grupo_botanico")
    private GrupoBotanico grupo;

    protected CulturaJpaEntity() {
    }

    public Cultura paraDominio() {
        return new Cultura(id, nome, vAlvo, vMinimo, phMinIdeal, phMaxIdeal, grupo);
    }
}
