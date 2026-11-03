package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.parametro.RegraMicronutriente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "nepar_regra_micronutriente")
public class RegraMicronutrienteJpaEntity {

    @Id
    private Long id;

    @Column(name = "versao_id")
    private Long versaoId;

    @Column(name = "cultura_id")
    private Long culturaId;

    @Enumerated(EnumType.STRING)
    private Micronutriente nutriente;

    private String classe;

    @Column(name = "dose_kg_ha")
    private BigDecimal doseKgHa;

    private String observacao;

    protected RegraMicronutrienteJpaEntity() {
    }

    public RegraMicronutriente paraDominio() {
        return new RegraMicronutriente(culturaId, nutriente, classe, doseKgHa, observacao);
    }
}
