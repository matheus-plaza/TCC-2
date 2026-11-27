package br.edu.tcc.fertsolo.infrastructure.persistence;

import br.edu.tcc.fertsolo.application.port.RegistroAuditoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class RegistroAuditoriaJpa implements RegistroAuditoria {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void registrar(Long atorId, String acao, Long alvoId) {
        em.createNativeQuery("INSERT INTO auditoria_administrativa (ator_id, acao, alvo_id, data_hora) VALUES (?1, ?2, ?3, ?4)")
                .setParameter(1, atorId)
                .setParameter(2, acao)
                .setParameter(3, alvoId)
                .setParameter(4, LocalDateTime.now())
                .executeUpdate();
    }
}
