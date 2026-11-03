-- As tabelas de N usam só o grupo da cultura anterior. Quando ela não está cadastrada,
-- a recomendação guarda apenas o grupo informado.
ALTER TABLE recomendacoes
    ADD COLUMN grupo_anterior VARCHAR(12),
    ADD CONSTRAINT ck_recomendacoes_grupo_anterior CHECK (
        grupo_anterior IS NULL OR (grupo_anterior IN ('GRAMINEA', 'LEGUMINOSA') AND cultura_anterior_id IS NULL));
