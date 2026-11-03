package br.edu.tcc.agromotor.infrastructure.persistence.entity;

import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.model.MetodoPh;
import br.edu.tcc.agromotor.domain.model.ProfundidadeAmostra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analises_solo")
public class LaudoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "talhao_id")
    private Long talhaoId;

    @Column(name = "data_analise")
    private LocalDate dataAnalise;

    @Column(name = "profundidade_cm")
    private String profundidadeCm;

    @Column(name = "ph")
    private BigDecimal ph;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_ph")
    private MetodoPh metodoPh;

    @Column(name = "mo_g_dm3")
    private BigDecimal moGDm3;

    @Column(name = "p_mehlich")
    private BigDecimal pMehlich;

    @Column(name = "k_cmolc")
    private BigDecimal kCmolc;

    @Column(name = "ca_cmolc")
    private BigDecimal caCmolc;

    @Column(name = "mg_cmolc")
    private BigDecimal mgCmolc;

    @Column(name = "al_cmolc")
    private BigDecimal alCmolc;

    @Column(name = "h_al_cmolc")
    private BigDecimal hAlCmolc;

    @Column(name = "argila_porcentagem")
    private BigDecimal argilaPorcentagem;

    @Column(name = "zn_mg_dm3")
    private BigDecimal znMgDm3;

    @Column(name = "b_mg_dm3")
    private BigDecimal bMgDm3;

    @Column(name = "cu_mg_dm3")
    private BigDecimal cuMgDm3;

    @Column(name = "mn_mg_dm3")
    private BigDecimal mnMgDm3;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    protected LaudoJpaEntity() {
    }

    public static LaudoJpaEntity de(LaudoSolo l) {
        LaudoJpaEntity e = new LaudoJpaEntity();
        e.id = l.id();
        e.talhaoId = l.talhaoId();
        e.dataAnalise = l.dataAnalise();
        e.profundidadeCm = l.profundidade().codigo();
        e.ph = l.ph();
        e.metodoPh = l.metodoPh();
        e.moGDm3 = l.moGDm3();
        e.pMehlich = l.pMehlich();
        e.kCmolc = l.kCmolc();
        e.caCmolc = l.caCmolc();
        e.mgCmolc = l.mgCmolc();
        e.alCmolc = l.alCmolc();
        e.hAlCmolc = l.hAlCmolc();
        e.argilaPorcentagem = l.argilaPorcentagem();
        e.znMgDm3 = l.znMgDm3();
        e.bMgDm3 = l.bMgDm3();
        e.cuMgDm3 = l.cuMgDm3();
        e.mnMgDm3 = l.mnMgDm3();
        e.dataCadastro = LocalDateTime.now();
        return e;
    }

    public LaudoSolo paraDominio() {
        return new LaudoSolo(id, talhaoId, dataAnalise, ProfundidadeAmostra.deCodigo(profundidadeCm), ph, metodoPh,
                moGDm3, pMehlich, kCmolc, caCmolc, mgCmolc, alCmolc, hAlCmolc, argilaPorcentagem, znMgDm3, bMgDm3, cuMgDm3, mnMgDm3);
    }
}
