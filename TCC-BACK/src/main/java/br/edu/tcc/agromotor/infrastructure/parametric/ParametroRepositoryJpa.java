package br.edu.tcc.agromotor.infrastructure.parametric;

import br.edu.tcc.agromotor.domain.model.AtributoSolo;
import br.edu.tcc.agromotor.domain.model.Micronutriente;
import br.edu.tcc.agromotor.domain.model.SistemaManejo;
import br.edu.tcc.agromotor.domain.model.VersaoParametros;
import br.edu.tcc.agromotor.domain.model.parametro.DoseGesso;
import br.edu.tcc.agromotor.domain.model.parametro.FaixaInterpretacao;
import br.edu.tcc.agromotor.domain.model.parametro.RegraCalagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraGessagem;
import br.edu.tcc.agromotor.domain.model.parametro.RegraMicronutriente;
import br.edu.tcc.agromotor.domain.model.parametro.RegraNpk;
import br.edu.tcc.agromotor.domain.repository.ParametroRepository;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.DoseGessoJpaEntity;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.FaixaInterpretacaoJpaEntity;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.RegraCalagemJpaEntity;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.RegraGessagemJpaEntity;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.RegraMicronutrienteJpaEntity;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.RegraNpkJpaEntity;
import br.edu.tcc.agromotor.infrastructure.parametric.entity.VersaoParametrosJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Busca nas tabelas paramétricas NEPAR. Toda consulta filtra pela versão vigente,
 * de modo que publicar uma nova versão altera o sistema sem recompilação.
 */
@Repository
public class ParametroRepositoryJpa implements ParametroRepository {

    private static final String VERSAO_VIGENTE =
            "(select v.id from VersaoParametrosJpaEntity v where v.vigente = true)";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<VersaoParametros> versaoVigente() {
        return em.createQuery("select v from VersaoParametrosJpaEntity v where v.vigente = true", VersaoParametrosJpaEntity.class)
                .getResultStream()
                .findFirst()
                .map(VersaoParametrosJpaEntity::paraDominio);
    }

    @Override
    public List<FaixaInterpretacao> faixasInterpretacao(AtributoSolo atributo) {
        return em.createQuery("select f from FaixaInterpretacaoJpaEntity f where f.atributo = :atributo and f.versaoId = "
                                + VERSAO_VIGENTE + " order by f.id",
                        FaixaInterpretacaoJpaEntity.class)
                .setParameter("atributo", atributo)
                .getResultStream()
                .map(FaixaInterpretacaoJpaEntity::paraDominio)
                .toList();
    }

    @Override
    public List<RegraNpk> regrasNpk(Long culturaId) {
        return em.createQuery("select r from RegraNpkJpaEntity r where r.culturaId = :culturaId and r.versaoId = "
                                + VERSAO_VIGENTE + " order by r.id",
                        RegraNpkJpaEntity.class)
                .setParameter("culturaId", culturaId)
                .getResultStream()
                .map(RegraNpkJpaEntity::paraDominio)
                .toList();
    }

    @Override
    public Optional<RegraGessagem> regraGessagem() {
        return em.createQuery("select g from RegraGessagemJpaEntity g where g.versaoId = " + VERSAO_VIGENTE,
                        RegraGessagemJpaEntity.class)
                .getResultStream()
                .findFirst()
                .map(RegraGessagemJpaEntity::paraDominio);
    }

    @Override
    public List<DoseGesso> dosesGesso() {
        return em.createQuery("select d from DoseGessoJpaEntity d where d.versaoId = " + VERSAO_VIGENTE + " order by d.argilaMin",
                        DoseGessoJpaEntity.class)
                .getResultStream()
                .map(DoseGessoJpaEntity::paraDominio)
                .toList();
    }

    @Override
    public Optional<RegraCalagem> regraCalagem(SistemaManejo manejo) {
        return em.createQuery("select r from RegraCalagemJpaEntity r where r.manejo = :manejo and r.versaoId = " + VERSAO_VIGENTE,
                        RegraCalagemJpaEntity.class)
                .setParameter("manejo", manejo)
                .getResultStream()
                .findFirst()
                .map(RegraCalagemJpaEntity::paraDominio);
    }

    @Override
    public List<RegraMicronutriente> regrasMicronutriente(Long culturaId, Micronutriente nutriente) {
        return em.createQuery("select m from RegraMicronutrienteJpaEntity m where m.culturaId = :culturaId "
                                + "and m.nutriente = :nutriente and m.versaoId = " + VERSAO_VIGENTE + " order by m.id",
                        RegraMicronutrienteJpaEntity.class)
                .setParameter("culturaId", culturaId)
                .setParameter("nutriente", nutriente)
                .getResultStream()
                .map(RegraMicronutrienteJpaEntity::paraDominio)
                .toList();
    }
}
