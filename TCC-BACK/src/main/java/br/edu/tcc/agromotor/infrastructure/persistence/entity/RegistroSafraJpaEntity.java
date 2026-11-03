package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.RegistroSafra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_safra")
public class RegistroSafraJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recomendacao_id")
    private Long recomendacaoId;

    @Column(name = "produtividade_colhida_ton_ha")
    private BigDecimal produtividadeColhidaTonHa;

    @Column(name = "data_colheita")
    private LocalDate dataColheita;

    @Column(name = "custo_adubacao_reais")
    private BigDecimal custoAdubacaoReais;

    @Column(name = "registrado_por_id")
    private Long registradoPorId;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    protected RegistroSafraJpaEntity() {
    }

    public static RegistroSafraJpaEntity de(RegistroSafra r, Long idExistente) {
        RegistroSafraJpaEntity e = new RegistroSafraJpaEntity();
        e.id = idExistente;
        e.recomendacaoId = r.recomendacaoId();
        e.produtividadeColhidaTonHa = r.produtividadeColhidaTonHa();
        e.dataColheita = r.dataColheita();
        e.custoAdubacaoReais = r.custoAdubacaoReais();
        e.registradoPorId = r.registradoPorId();
        e.atualizadoEm = r.atualizadoEm();
        return e;
    }

    public RegistroSafra paraDominio() {
        return new RegistroSafra(id, recomendacaoId, produtividadeColhidaTonHa, dataColheita, custoAdubacaoReais,
                registradoPorId, atualizadoEm);
    }

    public Long id() {
        return id;
    }
}
