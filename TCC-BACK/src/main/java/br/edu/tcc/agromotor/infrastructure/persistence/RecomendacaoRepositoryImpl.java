package br.edu.tcc.agromotor.infrastructure.persistence;

import br.edu.tcc.agromotor.domain.model.EscopoProdutores;
import br.edu.tcc.agromotor.domain.model.FiltroRecomendacoes;
import br.edu.tcc.agromotor.domain.model.MetodoPh;
import br.edu.tcc.agromotor.domain.model.Recomendacao;
import br.edu.tcc.agromotor.domain.model.StatusRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.EntradaRecomendacao;
import br.edu.tcc.agromotor.domain.model.calculo.ResultadoRecomendacao;
import br.edu.tcc.agromotor.domain.repository.RecomendacaoRepository;
import br.edu.tcc.agromotor.infrastructure.persistence.entity.RecomendacaoJpaEntity;
import br.edu.tcc.agromotor.infrastructure.persistence.jpa.RecomendacaoJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persiste a recomendação com a cópia integral do cenário e do resultado em JSONB.
 * A leitura reconstrói o registro a partir dessa cópia, nunca das tabelas atuais.
 */
@Repository
public class RecomendacaoRepositoryImpl implements RecomendacaoRepository {

    /** Conteúdo de {@code snapshot_auditoria} (RNF06). */
    public record SnapshotAuditoria(EntradaRecomendacao entrada, ResultadoRecomendacao resultado) {
    }

    private final RecomendacaoJpaRepository jpa;
    private final ObjectMapper json;

    @PersistenceContext
    private EntityManager em;

    public RecomendacaoRepositoryImpl(RecomendacaoJpaRepository jpa, ObjectMapper json) {
        this.jpa = jpa;
        this.json = json;
    }

    @Override
    public Recomendacao salvar(Recomendacao r) {
        if (r.id() == null) {
            return paraDominio(jpa.save(RecomendacaoJpaEntity.de(r, escrever(r))));
        }
        RecomendacaoJpaEntity existente = jpa.findById(r.id()).orElseThrow();
        existente.registrarAdocao(r);
        return paraDominio(jpa.saveAndFlush(existente));
    }

    @Override
    public Optional<Recomendacao> buscarPorId(Long id) {
        return jpa.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Recomendacao> listar(EscopoProdutores escopo, FiltroRecomendacoes filtro) {
        if (!escopo.irrestrito() && escopo.produtorIds().isEmpty()) {
            return List.of();
        }
        StringBuilder jpql = new StringBuilder("""
                select r from RecomendacaoJpaEntity r
                join LaudoJpaEntity l on l.id = r.analiseId
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
        if (filtro.laudoId() != null) {
            jpql.append(" and r.analiseId = :laudoId");
            parametros.put("laudoId", filtro.laudoId());
        }
        if (filtro.status() != null) {
            jpql.append(" and r.status = :status");
            parametros.put("status", filtro.status());
        }
        if (filtro.inicio() != null) {
            jpql.append(" and coalesce(r.dataAdocao, r.dataGeracao) >= :inicio");
            parametros.put("inicio", filtro.inicio().atStartOfDay());
        }
        if (filtro.fim() != null) {
            jpql.append(" and coalesce(r.dataAdocao, r.dataGeracao) < :fim");
            parametros.put("fim", filtro.fim().plusDays(1).atStartOfDay());
        }
        jpql.append(" order by coalesce(r.dataAdocao, r.dataGeracao) desc, r.id desc");

        var consulta = em.createQuery(jpql.toString(), RecomendacaoJpaEntity.class);
        parametros.forEach(consulta::setParameter);
        return consulta.getResultStream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<Recomendacao> ultimaAdotadaDoTalhao(Long talhaoId) {
        return em.createQuery("""
                        select r from RecomendacaoJpaEntity r
                        join LaudoJpaEntity l on l.id = r.analiseId
                        where l.talhaoId = :talhaoId and r.status = :status
                        order by r.dataAdocao desc, r.id desc
                        """, RecomendacaoJpaEntity.class)
                .setParameter("talhaoId", talhaoId)
                .setParameter("status", StatusRecomendacao.ADOTADA)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(this::paraDominio);
    }

    @Override
    public boolean existeParaLaudo(Long laudoId) {
        return jpa.existsByAnaliseId(laudoId);
    }

    @Override
    public void excluir(Long id) {
        jpa.deleteById(id);
    }

    /**
     * Snapshots anteriores à migração V5 guardam um número em cada dose de NPK. Na leitura,
     * o número vira uma faixa de mínimo igual ao máximo; o registro no banco não muda.
     */
    private JsonNode comDosesEmFaixa(JsonNode snapshot) {
        if (snapshot.at("/resultado/adubacao") instanceof ObjectNode adubacao) {
            for (String campo : List.of("doseNkgHa", "doseP2O5kgHa", "doseK2OkgHa")) {
                JsonNode dose = adubacao.get(campo);
                if (dose != null && dose.isNumber()) {
                    ObjectNode faixa = json.createObjectNode();
                    faixa.set("min", dose);
                    faixa.set("max", dose);
                    adubacao.set(campo, faixa);
                }
            }
        }
        return snapshot;
    }

    /**
     * Snapshots anteriores à migração V7 guardam o pH em {@code phCacl2}, único método aceito
     * até então. Na leitura, ele passa para o formato atual; o registro no banco não muda.
     */
    private JsonNode comMetodoDoPh(JsonNode snapshot) {
        for (String caminho : List.of("/entrada/laudo", "/entrada/laudoSubsuperficie")) {
            if (snapshot.at(caminho) instanceof ObjectNode laudo && laudo.has("phCacl2")) {
                laudo.set("ph", laudo.remove("phCacl2"));
                laudo.put("metodoPh", MetodoPh.CACL2.name());
            }
        }
        return snapshot;
    }

    private String escrever(Recomendacao r) {
        try {
            return json.writeValueAsString(new SnapshotAuditoria(r.entrada(), r.resultado()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao gerar o snapshot de auditoria", e);
        }
    }

    private Recomendacao paraDominio(RecomendacaoJpaEntity e) {
        try {
            SnapshotAuditoria snapshot = json.treeToValue(comMetodoDoPh(comDosesEmFaixa(json.readTree(e.snapshotAuditoria()))), SnapshotAuditoria.class);
            return new Recomendacao(e.id(), snapshot.entrada(), snapshot.resultado(), e.status(), e.dataGeracao(),
                    e.geradoPorId(), e.adotadoPorId(), e.dataAdocao());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Snapshot de auditoria inválido na recomendação " + e.id(), ex);
        }
    }
}
