package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "agronomo_produtor")
@IdClass(VinculoJpaEntity.Chave.class)
public class VinculoJpaEntity {

    @Id
    @Column(name = "agronomo_id")
    private Long agronomoId;

    @Id
    @Column(name = "produtor_id")
    private Long produtorId;

    protected VinculoJpaEntity() {
    }

    public VinculoJpaEntity(Long agronomoId, Long produtorId) {
        this.agronomoId = agronomoId;
        this.produtorId = produtorId;
    }

    public Long agronomoId() {
        return agronomoId;
    }

    public Long produtorId() {
        return produtorId;
    }

    public record Chave(Long agronomoId, Long produtorId) implements Serializable {
    }
}
