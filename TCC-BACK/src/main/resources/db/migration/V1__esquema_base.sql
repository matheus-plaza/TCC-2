-- =====================================================================
-- V1: esquema base, conforme a seção 6.2 da especificação (SRS).
-- As extensões necessárias aos requisitos ficam nas migrações seguintes.
-- =====================================================================

-- Enums para controle de fluxo e perfis
CREATE TYPE perfil_usuario AS ENUM ('ADMIN', 'AGRONOMO', 'PRODUTOR');
CREATE TYPE grupo_botanico AS ENUM ('LEGUMINOSA', 'GRAMINEA');
CREATE TYPE sistema_manejo AS ENUM ('PLANTIO_DIRETO', 'CONVENCIONAL');
CREATE TYPE status_recomendacao AS ENUM ('SIMULACAO', 'ADOTADA');

-- 1. Tabela de Usuários
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil perfil_usuario NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela de Propriedades Rurais
CREATE TABLE propriedades (
    id BIGSERIAL PRIMARY KEY,
    produtor_id BIGINT NOT NULL REFERENCES usuarios(id),
    nome VARCHAR(150) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    uf VARCHAR(2) DEFAULT 'PR'
);

-- 3. Tabela de Talhões
CREATE TABLE talhoes (
    id BIGSERIAL PRIMARY KEY,
    propriedade_id BIGINT NOT NULL REFERENCES propriedades(id),
    identificacao VARCHAR(100) NOT NULL,
    area_hectares NUMERIC(10,2) NOT NULL
);

-- 4. Tabela de Culturas (Paramétrica de Requisitos)
CREATE TABLE culturas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    v_alvo NUMERIC(5,2) NOT NULL, -- V2% exigido pela cultura (ex: 70.0)
    ph_min_ideal NUMERIC(3,2) NOT NULL,
    ph_max_ideal NUMERIC(3,2) NOT NULL,
    grupo grupo_botanico NOT NULL
);

-- 5. Tabela de Análises de Solo (Laudos)
CREATE TABLE analises_solo (
    id BIGSERIAL PRIMARY KEY,
    talhao_id BIGINT NOT NULL REFERENCES talhoes(id),
    data_analise DATE NOT NULL,
    profundidade_cm VARCHAR(20) DEFAULT '0-20',
    ph_cacl2 NUMERIC(4,2) NOT NULL,
    mo_g_dm3 NUMERIC(5,2) NOT NULL,
    p_mehlich NUMERIC(5,2) NOT NULL,
    k_cmolc NUMERIC(5,3) NOT NULL,
    ca_cmolc NUMERIC(5,3) NOT NULL,
    mg_cmolc NUMERIC(5,3) NOT NULL,
    al_cmolc NUMERIC(5,3) NOT NULL,
    h_al_cmolc NUMERIC(5,3) NOT NULL,
    argila_porcentagem NUMERIC(5,2) NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tabela Paramétrica de Recomendação NPK (Espelho NEPAR)
CREATE TABLE nepar_regra_npk (
    id BIGSERIAL PRIMARY KEY,
    cultura_id BIGINT REFERENCES culturas(id),
    classe_fertilidade VARCHAR(20) NOT NULL, -- Muito Baixo, Baixo, Médio, Alto
    faixa_rendimento_min NUMERIC(5,2) NOT NULL,
    faixa_rendimento_max NUMERIC(5,2) NOT NULL,
    dose_n NUMERIC(6,2) NOT NULL,
    dose_p2o5 NUMERIC(6,2) NOT NULL,
    dose_k2o NUMERIC(6,2) NOT NULL
);

-- 7. Tabela de Recomendações Geradas (Histórico / Rastreabilidade)
CREATE TABLE recomendacoes (
    id BIGSERIAL PRIMARY KEY,
    analise_id BIGINT NOT NULL REFERENCES analises_solo(id),
    cultura_alvo_id BIGINT NOT NULL REFERENCES culturas(id),
    cultura_anterior_id BIGINT REFERENCES culturas(id),
    manejo sistema_manejo NOT NULL,
    expectativa_rendimento NUMERIC(5,2) NOT NULL,

    -- Resultados Calculados pelo Motor
    v1_atual NUMERIC(5,2) NOT NULL,
    necessidade_calcario_ton_ha NUMERIC(5,2) NOT NULL,
    necessidade_gesso_kg_ha NUMERIC(6,2) NOT NULL,
    dose_n_kg_ha NUMERIC(6,2) NOT NULL,
    dose_p2o5_kg_ha NUMERIC(6,2) NOT NULL,
    dose_k2o_kg_ha NUMERIC(6,2) NOT NULL,

    status status_recomendacao DEFAULT 'SIMULACAO',
    data_geracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
