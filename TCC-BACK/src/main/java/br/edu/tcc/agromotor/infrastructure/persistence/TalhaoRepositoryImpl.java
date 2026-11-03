package br.edu.tcc.agromotor.infrastructure.persistence;

import br.edu.tcc.agromotor.domain.model.ContextoTalhao;
import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.Talhao;
import br.edu.tcc.agromotor.domain.repository.TalhaoRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.TalhaoJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.TalhaoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TalhaoRepositoryImpl implements TalhaoRepository {

    private static final String CONTEXTO = """
            select new br.edu.tcc.agromotor.domain.model.ContextoTalhao(
                t.id, t.identificacao, t.areaHectares, t.texturaPredominante,
                p.id, p.nome, p.municipio, p.uf, u.id, u.nome)
            from TalhaoJpaEntity t
            join PropriedadeJpaEntity p on p.id = t.propriedadeId
            join UsuarioJpaEntity u on u.id = p.produtorId
            """;

    private final TalhaoJpaRepository jpa;

    @PersistenceContext
    private EntityManager em;

    public TalhaoRepositoryImpl(TalhaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Talhao salvar(Talhao talhao) {
        return jpa.saveAndFlush(TalhaoJpaEntity.de(talhao)).paraDominio();
    }

    @Override
    public Optional<ContextoTalhao> contexto(Long talhaoId) {
        return em.createQuery(CONTEXTO + " where t.id = :id", ContextoTalhao.class)
                .setParameter("id", talhaoId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<ContextoTalhao> listarContextos(EscopoProdutores escopo, Long propriedadeId) {
        if (!escopo.irrestrito() && escopo.produtorIds().isEmpty()) {
            return List.of();
        }
        StringBuilder jpql = new StringBuilder(CONTEXTO).append(" where 1 = 1");
        if (!escopo.irrestrito()) {
            jpql.append(" and p.produtorId in :produtores");
        }
        if (propriedadeId != null) {
            jpql.append(" and p.id = :propriedadeId");
        }
        jpql.append(" order by p.nome, t.identificacao");

        var consulta = em.createQuery(jpql.toString(), ContextoTalhao.class);
        if (!escopo.irrestrito()) {
            consulta.setParameter("produtores", escopo.produtorIds());
        }
        if (propriedadeId != null) {
            consulta.setParameter("propriedadeId", propriedadeId);
        }
        return consulta.getResultList();
    }

    @Override
    public boolean existeParaPropriedade(Long propriedadeId) {
        return jpa.existsByPropriedadeId(propriedadeId);
    }

    @Override
    public boolean existeIdentificacao(Long propriedadeId, String identificacao, Long talhaoIgnorado) {
        return talhaoIgnorado == null
                ? jpa.existsByPropriedadeIdAndIdentificacao(propriedadeId, identificacao)
                : jpa.existsByPropriedadeIdAndIdentificacaoAndIdNot(propriedadeId, identificacao, talhaoIgnorado);
    }

    @Override
    public void excluir(Long id) {
        jpa.deleteById(id);
    }
}
