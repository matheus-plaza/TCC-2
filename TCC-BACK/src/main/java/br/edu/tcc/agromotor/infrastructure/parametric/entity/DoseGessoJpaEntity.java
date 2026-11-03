package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.parametro.DoseGesso;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "nepar_dose_gesso")
public class DoseGessoJpaEntity {

    @Id
    private Long id;

    @Column(name = "versao_id")
    private Long versaoId;

    @Column(name = "argila_min")
    private BigDecimal argilaMin;

    @Column(name = "argila_max")
    private BigDecimal argilaMax;

    @Column(name = "dose_kg_ha")
    private BigDecimal doseKgHa;

    protected DoseGessoJpaEntity() {
    }

    public DoseGesso paraDominio() {
        return new DoseGesso(new Intervalo(argilaMin, argilaMax), doseKgHa);
    }
}
