package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.parametro.RegraCalagem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "nepar_regra_calagem")
public class RegraCalagemJpaEntity {

    @Id
    private Long id;

    @Column(name = "versao_id")
    private Long versaoId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "sistema_manejo")
    private SistemaManejo manejo;

    private String observacao;

    protected RegraCalagemJpaEntity() {
    }

    public RegraCalagem paraDominio() {
        return new RegraCalagem(manejo, observacao);
    }
}
