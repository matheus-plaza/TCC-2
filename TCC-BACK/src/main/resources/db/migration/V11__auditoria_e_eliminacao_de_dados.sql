-- =====================================================================
-- V11: registro das ações administrativas e eliminação dos dados a pedido do titular.
-- =====================================================================

-- Sem chave estrangeira em ator_id e alvo_id: o registro permanece depois que a conta é excluída.
CREATE TABLE auditoria_administrativa (
    id        BIGSERIAL   PRIMARY KEY,
    ator_id   BIGINT      NOT NULL,
    acao      VARCHAR(40) NOT NULL,
    alvo_id   BIGINT,
    data_hora TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_auditoria_data_hora ON auditoria_administrativa (data_hora DESC);

-- Quando o agrônomo exclui a conta, a recomendação adotada continua com o produtor, sem o autor.
ALTER TABLE recomendacoes DROP CONSTRAINT ck_recomendacoes_adocao;
ALTER TABLE recomendacoes ADD CONSTRAINT ck_recomendacoes_adocao CHECK (
    (status = 'SIMULACAO' AND adotado_por_id IS NULL AND data_adocao IS NULL)
    OR (status = 'ADOTADA' AND data_adocao IS NOT NULL));

-- A recomendação adotada continua imutável. A única exceção é a eliminação de conta, que a
-- aplicação marca na própria transação (fertsolo.eliminacao_titular): ela pode apagar a
-- recomendação do produtor excluído ou retirar dela o autor excluído, e nada além disso.
CREATE OR REPLACE FUNCTION proteger_recomendacao_adotada() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'ADOTADA' THEN
        IF current_setting('fertsolo.eliminacao_titular', true) = 'on' THEN
            IF TG_OP = 'DELETE' THEN
                RETURN OLD;
            END IF;
            IF (to_jsonb(NEW) - 'gerado_por_id' - 'adotado_por_id') = (to_jsonb(OLD) - 'gerado_por_id' - 'adotado_por_id')
                AND (NEW.gerado_por_id IS NULL OR NEW.gerado_por_id = OLD.gerado_por_id)
                AND (NEW.adotado_por_id IS NULL OR NEW.adotado_por_id = OLD.adotado_por_id) THEN
                RETURN NEW;
            END IF;
        END IF;
        RAISE EXCEPTION 'Recomendação adotada é imutável (id = %)', OLD.id
            USING ERRCODE = 'restrict_violation';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
