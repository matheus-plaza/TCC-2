-- Resultado de campo de uma recomendação adotada: a produtividade colhida, comparada com
-- a esperada, e o gasto com adubação no talhão. Fica em tabela própria porque a
-- recomendação adotada é imutável (RNF06), e o resultado só existe depois dela.
CREATE TABLE registros_safra (
    id                           BIGSERIAL PRIMARY KEY,
    recomendacao_id              BIGINT    NOT NULL UNIQUE REFERENCES recomendacoes (id),
    produtividade_colhida_ton_ha NUMERIC(5,2),
    data_colheita                DATE,
    custo_adubacao_reais         NUMERIC(12,2),
    registrado_por_id            BIGINT    REFERENCES usuarios (id) ON DELETE SET NULL,
    atualizado_em                TIMESTAMP NOT NULL,
    CONSTRAINT ck_safra_colheita CHECK ((produtividade_colhida_ton_ha IS NULL) = (data_colheita IS NULL)),
    CONSTRAINT ck_safra_produtividade CHECK (produtividade_colhida_ton_ha > 0),
    CONSTRAINT ck_safra_custo CHECK (custo_adubacao_reais >= 0),
    CONSTRAINT ck_safra_preenchida CHECK (produtividade_colhida_ton_ha IS NOT NULL OR custo_adubacao_reais IS NOT NULL)
);
