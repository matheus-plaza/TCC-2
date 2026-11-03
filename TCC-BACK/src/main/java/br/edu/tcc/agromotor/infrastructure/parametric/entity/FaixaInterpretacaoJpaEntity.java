package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.parametro.FaixaInterpretacao;
import br.edu.tcc.agromotor.domain.model.parametro.Intervalo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "nepar_faixa_interpretacao")
public class FaixaInterpretacaoJpaEntity {

    @Id
    private Long id;

    @Column(name = "versao_id")
    private Long versaoId;

    @Enumerated(EnumType.STRING)
    private AtributoSolo atributo;

    @Column(name = "criterio_min")
    private BigDecimal criterioMin;

    @Column(name = "criterio_max")
    private BigDecimal criterioMax;

    @Column(name = "teor_min")
    private BigDecimal teorMin;

    @Column(name = "teor_max")
    private BigDecimal teorMax;

    private String classe;

    protected FaixaInterpretacaoJpaEntity() {
    }

    public FaixaInterpretacao paraDominio() {
        return new FaixaInterpretacao(atributo, new Intervalo(criterioMin, criterioMax), new Intervalo(teorMin, teorMax), classe);
    }
}
