package br.edu.tcc.agromotor.infrastructure.parametric.entity;

import br.edu.tcc.agromotor.domain.model.VersaoParametros;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nepar_versao_parametros")
public class VersaoParametrosJpaEntity {

    @Id
    private Long id;

    private String codigo;

    private String descricao;

    @Column(name = "conferida_manual")
    private boolean conferidaManual;

    private boolean vigente;

    protected VersaoParametrosJpaEntity() {
    }

    public VersaoParametros paraDominio() {
        return new VersaoParametros(id, codigo, descricao, conferidaManual);
    }
}
