package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.parametro.RegraGessagem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "nepar_regra_gessagem")
public class RegraGessagemJpaEntity {

    @Id
    private Long id;

    @Column(name = "versao_id")
    private Long versaoId;

    @Column(name = "limite_m_porcentagem")
    private BigDecimal limiteMPorcentagem;

    @Column(name = "limite_ca_cmolc")
    private BigDecimal limiteCaCmolc;

    protected RegraGessagemJpaEntity() {
    }

    public RegraGessagem paraDominio() {
        return new RegraGessagem(limiteMPorcentagem, limiteCaCmolc);
    }
}
