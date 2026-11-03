package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.Propriedade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "propriedades")
public class PropriedadeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produtor_id")
    private Long produtorId;

    private String nome;

    private String municipio;

    private String uf;

    protected PropriedadeJpaEntity() {
    }

    public static PropriedadeJpaEntity de(Propriedade p) {
        PropriedadeJpaEntity e = new PropriedadeJpaEntity();
        e.id = p.id();
        e.produtorId = p.produtorId();
        e.nome = p.nome();
        e.municipio = p.municipio();
        e.uf = p.uf();
        return e;
    }

    public Propriedade paraDominio() {
        return new Propriedade(id, produtorId, nome, municipio, uf);
    }
}
