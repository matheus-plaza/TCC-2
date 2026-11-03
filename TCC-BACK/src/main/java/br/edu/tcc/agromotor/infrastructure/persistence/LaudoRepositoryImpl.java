package br.edu.tcc.agromotor.infrastructure.persistence;

import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroLaudos;
import br.edu.tcc.agromotor.domain.model.LaudoSolo;
import br.edu.tcc.agromotor.domain.repository.LaudoRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.LaudoJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.LaudoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class LaudoRepositoryImpl implements LaudoRepository {

    private final LaudoJpaRepository jpa;

    @PersistenceContext
    private EntityManager em;

    public LaudoRepositoryImpl(LaudoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LaudoSolo salvar(LaudoSolo laudo) {
        return jpa.save(LaudoJpaEntity.de(laudo)).paraDominio();
    }

    @Override
    public Optional<LaudoSolo> buscarPorId(Long id) {
        return jpa.findById(id).map(LaudoJpaEntity::paraDominio);
    }

    @Override
    public List<LaudoSolo> listar(EscopoProdutores escopo, FiltroLaudos filtro) {
        if (!escopo.irrestrito() && escopo.produtorIds().isEmpty()) {
            return List.of();
        }
        StringBuilder jpql = new StringBuilder("""
                select l from LaudoJpaEntity l
                join TalhaoJpaEntity t on t.id = l.talhaoId
                join PropriedadeJpaEntity p on p.id = t.propriedadeId
                where 1 = 1
                """);
        Map<String, Object> parametros = new HashMap<>();
        if (!escopo.irrestrito()) {
            jpql.append(" and p.produtorId in :produtores");
            parametros.put("produtores", escopo.produtorIds());
        }
        if (filtro.talhaoId() != null) {
            jpql.append(" and l.talhaoId = :talhaoId");
            parametros.put("talhaoId", filtro.talhaoId());
        }
        if (filtro.profundidade() != null) {
            jpql.append(" and l.profundidadeCm = :profundidade");
            parametros.put("profundidade", filtro.profundidade().codigo());
        }
        if (filtro.inicio() != null) {
            jpql.append(" and l.dataAnalise >= :inicio");
            parametros.put("inicio", filtro.inicio());
        }
        if (filtro.fim() != null) {
            jpql.append(" and l.dataAnalise <= :fim");
            parametros.put("fim", filtro.fim());
        }
        jpql.append(" order by l.dataAnalise desc, l.id desc");

        var consulta = em.createQuery(jpql.toString(), LaudoJpaEntity.class);
        parametros.forEach(consulta::setParameter);
        return consulta.getResultStream().map(LaudoJpaEntity::paraDominio).toList();
    }

    @Override
    public boolean existeParaTalhao(Long talhaoId) {
        return jpa.existsByTalhaoId(talhaoId);
    }

    @Override
    public void excluir(Long id) {
        jpa.deleteById(id);
    }
}
