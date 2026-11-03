package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.Talhao;
import br.edu.tcc.agromotor.domain.model.TexturaSolo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "talhoes")
public class TalhaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "propriedade_id")
    private Long propriedadeId;

    private String identificacao;

    @Column(name = "area_hectares")
    private BigDecimal areaHectares;

    @Enumerated(EnumType.STRING)
    @Column(name = "textura_predominante")
    private TexturaSolo texturaPredominante;

    protected TalhaoJpaEntity() {
    }

    public static TalhaoJpaEntity de(Talhao t) {
        TalhaoJpaEntity e = new TalhaoJpaEntity();
        e.id = t.id();
        e.propriedadeId = t.propriedadeId();
        e.identificacao = t.identificacao();
        e.areaHectares = t.areaHectares();
        e.texturaPredominante = t.texturaPredominante();
        return e;
    }

    public Talhao paraDominio() {
        return new Talhao(id, propriedadeId, identificacao, areaHectares, texturaPredominante);
    }
}
