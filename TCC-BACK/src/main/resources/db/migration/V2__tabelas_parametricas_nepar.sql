-- =====================================================================
-- V2: tabelas paramétricas além da nepar_regra_npk (RNF02), todas ligadas a
-- uma versão (RNF06). O motor lê somente a versão vigente; publicar uma nova
-- edição é inserir uma nova versão, sem recompilar nem perder o histórico.
--
-- Convenção dos intervalos: [min, max). O mínimo é inclusivo, o máximo é
-- exclusivo e um limite NULL significa "sem limite" naquele lado.
-- =====================================================================

CREATE TABLE nepar_versao_parametros (
    id               BIGSERIAL PRIMARY KEY,
    codigo           VARCHAR(40) NOT NULL UNIQUE,
    descricao        TEXT        NOT NULL,
    conferida_manual BOOLEAN     NOT NULL DEFAULT FALSE, -- valores conferidos com o manual impresso
    vigente          BOOLEAN     NOT NULL DEFAULT FALSE,
    data_registro    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uk_nepar_versao_vigente ON nepar_versao_parametros (vigente) WHERE vigente;

-- Enquadramento em classes de fertilidade (RF04). O critério cruza o teor com
-- a argila (fósforo) ou com a CTC a pH 7,0 (potássio).
CREATE TABLE nepar_faixa_interpretacao (
    id           BIGSERIAL PRIMARY KEY,
    versao_id    BIGINT       NOT NULL REFERENCES nepar_versao_parametros (id),
    atributo     VARCHAR(30)  NOT NULL,
    criterio_min NUMERIC(8,3),
    criterio_max NUMERIC(8,3),
    teor_min     NUMERIC(8,3),
    teor_max     NUMERIC(8,3),
    classe       VARCHAR(20)  NOT NULL,
    CONSTRAINT ck_faixa_atributo CHECK (atributo IN ('PH', 'MATERIA_ORGANICA', 'FOSFORO', 'POTASSIO', 'CALCIO',
        'MAGNESIO', 'SATURACAO_BASES', 'SATURACAO_ALUMINIO', 'ZINCO', 'BORO')),
    CONSTRAINT ck_faixa_criterio CHECK (criterio_max IS NULL OR criterio_min IS NULL OR criterio_max > criterio_min),
    CONSTRAINT ck_faixa_teor CHECK (teor_max IS NULL OR teor_min IS NULL OR teor_max > teor_min)
);
CREATE INDEX ix_faixa_versao_atributo ON nepar_faixa_interpretacao (versao_id, atributo);

-- Doses de NPK: cada nutriente é procurado pela sua própria classe. P2O5 pela
-- classe de P, K2O pela classe de K e N pela classe de matéria orgânica.
ALTER TABLE nepar_regra_npk
    ADD COLUMN versao_id BIGINT NOT NULL REFERENCES nepar_versao_parametros (id),
    ALTER COLUMN cultura_id SET NOT NULL,
    ADD CONSTRAINT ck_npk_rendimento CHECK (faixa_rendimento_max > faixa_rendimento_min),
    ADD CONSTRAINT ck_npk_doses CHECK (dose_n >= 0 AND dose_p2o5 >= 0 AND dose_k2o >= 0);
CREATE INDEX ix_npk_versao_cultura ON nepar_regra_npk (versao_id, cultura_id);

-- Crédito (negativo) ou débito (positivo) de N pela cultura antecessora.
CREATE TABLE nepar_ajuste_nitrogenio (
    id               BIGSERIAL PRIMARY KEY,
    versao_id        BIGINT         NOT NULL REFERENCES nepar_versao_parametros (id),
    cultura_id       BIGINT         NOT NULL REFERENCES culturas (id),
    grupo_antecessor grupo_botanico NOT NULL,
    ajuste_kg_ha     NUMERIC(6,2)   NOT NULL,
    CONSTRAINT uk_ajuste_n UNIQUE (versao_id, cultura_id, grupo_antecessor)
);

-- Gessagem: critérios em 20-40 cm e fator de dose. Uma linha por versão.
CREATE TABLE nepar_regra_gessagem (
    id                   BIGSERIAL PRIMARY KEY,
    versao_id            BIGINT       NOT NULL UNIQUE REFERENCES nepar_versao_parametros (id),
    limite_al_cmolc      NUMERIC(5,3) NOT NULL,
    limite_v_porcentagem NUMERIC(5,2) NOT NULL,
    limite_ca_cmolc      NUMERIC(5,3) NOT NULL,
    fator_argila_kg_ha   NUMERIC(6,2) NOT NULL
);

-- Aplicação do calcário por manejo e argila. Teto NULL = sem teto por aplicação.
CREATE TABLE nepar_regra_calagem (
    id          BIGSERIAL PRIMARY KEY,
    versao_id   BIGINT         NOT NULL REFERENCES nepar_versao_parametros (id),
    manejo      sistema_manejo NOT NULL,
    argila_min  NUMERIC(5,2),
    argila_max  NUMERIC(6,2),
    teto_ton_ha NUMERIC(5,2),
    observacao  TEXT           NOT NULL,
    CONSTRAINT ck_calagem_argila CHECK (argila_max IS NULL OR argila_min IS NULL OR argila_max > argila_min)
);
CREATE INDEX ix_calagem_versao_manejo ON nepar_regra_calagem (versao_id, manejo);

-- Micronutrientes: teor do laudo abaixo do nível crítico aciona a dose.
CREATE TABLE nepar_regra_micronutriente (
    id           BIGSERIAL PRIMARY KEY,
    versao_id    BIGINT       NOT NULL REFERENCES nepar_versao_parametros (id),
    cultura_id   BIGINT       NOT NULL REFERENCES culturas (id),
    nutriente    VARCHAR(10)  NOT NULL,
    teor_critico NUMERIC(6,2) NOT NULL,
    dose_kg_ha   NUMERIC(6,2) NOT NULL,
    CONSTRAINT ck_micro_nutriente CHECK (nutriente IN ('ZINCO', 'BORO')),
    CONSTRAINT uk_micro UNIQUE (versao_id, cultura_id, nutriente)
);
