-- =====================================================================
-- V3: extensões exigidas pelos requisitos que o esquema base não cobre.
--   RNF05: vínculo agrônomo-produtor
--   RF02:  textura predominante do talhão
--   RF03:  validação no banco, além do domínio; Zn e B no laudo
--   RNF06: snapshot, versão dos parâmetros e autoria das recomendações
-- =====================================================================

ALTER TABLE usuarios ADD COLUMN registro_profissional VARCHAR(30); -- CREA, usado no receituário

CREATE TABLE agronomo_produtor (
    agronomo_id  BIGINT    NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    produtor_id  BIGINT    NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    data_vinculo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (agronomo_id, produtor_id),
    CONSTRAINT ck_vinculo_distintos CHECK (agronomo_id <> produtor_id)
);
CREATE INDEX ix_vinculo_produtor ON agronomo_produtor (produtor_id);

ALTER TABLE propriedades
    ALTER COLUMN uf SET NOT NULL,
    ADD CONSTRAINT ck_propriedades_uf CHECK (uf = 'PR');
CREATE INDEX ix_propriedades_produtor ON propriedades (produtor_id);

ALTER TABLE talhoes
    ADD COLUMN textura_predominante VARCHAR(20),
    ADD CONSTRAINT ck_talhoes_textura CHECK (textura_predominante IN ('LEVE', 'MEDIA', 'ARGILOSA', 'MUITO_ARGILOSA')),
    ADD CONSTRAINT ck_talhoes_area CHECK (area_hectares > 0),
    ADD CONSTRAINT uk_talhoes_identificacao UNIQUE (propriedade_id, identificacao);

ALTER TABLE analises_solo
    ADD COLUMN zn_mg_dm3 NUMERIC(6,2),
    ADD COLUMN b_mg_dm3 NUMERIC(6,2),
    ALTER COLUMN profundidade_cm SET NOT NULL,
    ADD CONSTRAINT ck_analises_profundidade CHECK (profundidade_cm IN ('0-20', '20-40')),
    ADD CONSTRAINT ck_analises_ph CHECK (ph_cacl2 BETWEEN 3 AND 9),
    ADD CONSTRAINT ck_analises_argila CHECK (argila_porcentagem BETWEEN 0 AND 100),
    ADD CONSTRAINT ck_analises_teores CHECK (
        mo_g_dm3 > 0 AND p_mehlich > 0 AND k_cmolc > 0 AND ca_cmolc > 0 AND mg_cmolc > 0
        AND al_cmolc >= 0 AND h_al_cmolc > 0
        AND (zn_mg_dm3 IS NULL OR zn_mg_dm3 >= 0) AND (b_mg_dm3 IS NULL OR b_mg_dm3 >= 0));
CREATE INDEX ix_analises_talhao_data ON analises_solo (talhao_id, data_analise DESC);

ALTER TABLE recomendacoes
    ADD COLUMN prnt_calcario        NUMERIC(6,2) NOT NULL,
    ADD COLUMN dose_zn_kg_ha        NUMERIC(6,2),
    ADD COLUMN dose_b_kg_ha         NUMERIC(6,2),
    ADD COLUMN versao_parametros_id BIGINT NOT NULL REFERENCES nepar_versao_parametros (id),
    ADD COLUMN snapshot_auditoria   JSONB  NOT NULL,          -- entradas, parâmetros e resultado do momento
    ADD COLUMN gerado_por_id        BIGINT REFERENCES usuarios (id),
    ADD COLUMN adotado_por_id       BIGINT REFERENCES usuarios (id),
    ADD COLUMN data_adocao          TIMESTAMP,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN data_geracao SET NOT NULL,
    ADD CONSTRAINT ck_recomendacoes_adocao CHECK (
        (status = 'SIMULACAO' AND adotado_por_id IS NULL AND data_adocao IS NULL)
        OR (status = 'ADOTADA' AND adotado_por_id IS NOT NULL AND data_adocao IS NOT NULL));
CREATE INDEX ix_recomendacoes_analise ON recomendacoes (analise_id);
CREATE INDEX ix_recomendacoes_status ON recomendacoes (status, data_adocao DESC);

-- Recomendação adotada é cópia imutável para auditoria (RNF06): o próprio banco
-- recusa qualquer alteração ou exclusão, mesmo fora da aplicação.
CREATE FUNCTION proteger_recomendacao_adotada() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'ADOTADA' THEN
        RAISE EXCEPTION 'Recomendação adotada é imutável (id = %)', OLD.id
            USING ERRCODE = 'restrict_violation';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recomendacao_imutavel
    BEFORE UPDATE OR DELETE ON recomendacoes
    FOR EACH ROW EXECUTE FUNCTION proteger_recomendacao_adotada();
