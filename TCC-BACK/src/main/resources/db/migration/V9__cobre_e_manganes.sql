-- Cobre e manganês na soja: completa o uso das Tabelas 3.5 (p. 41) e 9.13 (p. 122).
-- Os teores são opcionais no laudo, como os de zinco e boro.
ALTER TABLE analises_solo
    ADD COLUMN cu_mg_dm3 NUMERIC(6,2),
    ADD COLUMN mn_mg_dm3 NUMERIC(6,2),
    ADD CONSTRAINT ck_analises_cu_mn CHECK ((cu_mg_dm3 IS NULL OR cu_mg_dm3 >= 0) AND (mn_mg_dm3 IS NULL OR mn_mg_dm3 >= 0));

ALTER TABLE recomendacoes
    ADD COLUMN dose_cu_kg_ha NUMERIC(6,2),
    ADD COLUMN dose_mn_kg_ha NUMERIC(6,2);

ALTER TABLE nepar_faixa_interpretacao DROP CONSTRAINT ck_faixa_atributo;
ALTER TABLE nepar_faixa_interpretacao ADD CONSTRAINT ck_faixa_atributo CHECK (atributo IN ('PH', 'PH_AGUA',
    'MATERIA_ORGANICA', 'FOSFORO', 'POTASSIO', 'CALCIO', 'MAGNESIO', 'SATURACAO_BASES', 'SATURACAO_ALUMINIO', 'ZINCO', 'BORO',
    'COBRE', 'MANGANES'));

ALTER TABLE nepar_regra_micronutriente DROP CONSTRAINT ck_micro_nutriente;
ALTER TABLE nepar_regra_micronutriente ADD CONSTRAINT ck_micro_nutriente
    CHECK (nutriente IN ('ZINCO', 'BORO', 'COBRE', 'MANGANES'));

-- Tabela 3.5, p. 41 (Mehlich-1, mg/dm³), com a mesma convenção da V6: um teor entre
-- classes fica na classe de baixo
INSERT INTO nepar_faixa_interpretacao (versao_id, atributo, criterio_min, criterio_max, teor_min, teor_max, classe)
SELECT v.id, f.atributo, NULL, NULL, f.tmin, f.tmax, f.classe
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    ('COBRE', 0::numeric, 0.2::numeric, 'Muito baixo'),
    ('COBRE', 0.2, 0.6, 'Baixo'),
    ('COBRE', 0.6, 0.9, 'Médio'),
    ('COBRE', 0.9, 3.01, 'Alto'),
    ('COBRE', 3.01, 20.01, 'Muito alto'),
    ('COBRE', 20.01, NULL, 'Condição a evitar'),
    ('MANGANES', 0, 5, 'Muito baixo'),
    ('MANGANES', 5, 16, 'Baixo'),
    ('MANGANES', 16, 31, 'Médio'),
    ('MANGANES', 31, 100.01, 'Alto'),
    ('MANGANES', 100.01, 200.01, 'Muito alto'),
    ('MANGANES', 200.01, NULL, 'Condição a evitar')
 ) AS f(atributo, tmin, tmax, classe)
 WHERE v.codigo = 'NEPAR-2019.1';

-- Tabela 9.13, p. 122. O manual pede restrição na análise de manganês, então toda dose
-- de Mn leva essa orientação na justificativa e no receituário.
INSERT INTO nepar_regra_micronutriente (versao_id, cultura_id, nutriente, classe, dose_kg_ha, observacao)
SELECT v.id, c.id, m.nutriente, m.classe, m.dose,
       CASE WHEN m.nutriente = 'MANGANES' AND m.dose > 0
            THEN concat_ws(' ', m.observacao,
                 'O manual pede restrição na análise de Mn no solo, por falta de calibração específica: aplicar só com sintomas de deficiência na planta. A deficiência costuma vir de calagem excessiva e pH acima de 6,0; quando ela ocorre, o manual recomenda a aplicação foliar, em duas aplicações que somam 0,5 a 1 kg/ha de Mn.')
            ELSE m.observacao END
  FROM nepar_versao_parametros v
  JOIN culturas c ON c.nome = 'Soja'
 CROSS JOIN (VALUES
    ('COBRE', 'Muito baixo', 1.0::numeric, 'A tabela do manual começa em Baixo; foi usada a dose dessa linha.'),
    ('COBRE', 'Baixo', 1.0, NULL),
    ('COBRE', 'Médio', 0.5, NULL),
    ('COBRE', 'Alto', 0, NULL),
    ('COBRE', 'Muito alto', 0, NULL),
    ('COBRE', 'Condição a evitar', 0, NULL),
    ('MANGANES', 'Muito baixo', 6.0, 'A tabela do manual começa em Baixo; foi usada a dose dessa linha.'),
    ('MANGANES', 'Baixo', 6.0, NULL),
    ('MANGANES', 'Médio', 3.0, NULL),
    ('MANGANES', 'Alto', 0, NULL),
    ('MANGANES', 'Muito alto', 0, NULL),
    ('MANGANES', 'Condição a evitar', 0, NULL)
 ) AS m(nutriente, classe, dose, observacao)
 WHERE v.codigo = 'NEPAR-2019.1';
