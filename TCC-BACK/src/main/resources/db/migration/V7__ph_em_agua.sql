-- O laudo pode trazer o pH em CaCl₂ ou em água. O método só muda a classe mostrada,
-- pela coluna correspondente da Tabela 3.2 do manual: nenhuma dose depende do pH.
-- Os laudos já gravados foram registrados em CaCl₂, o único método aceito até aqui.
ALTER TABLE analises_solo RENAME COLUMN ph_cacl2 TO ph;
ALTER TABLE analises_solo
    ADD COLUMN metodo_ph VARCHAR(10) NOT NULL DEFAULT 'CACL2',
    ADD CONSTRAINT ck_analises_metodo_ph CHECK (metodo_ph IN ('CACL2', 'AGUA'));
ALTER TABLE analises_solo ALTER COLUMN metodo_ph DROP DEFAULT;

ALTER TABLE nepar_faixa_interpretacao DROP CONSTRAINT ck_faixa_atributo;
ALTER TABLE nepar_faixa_interpretacao ADD CONSTRAINT ck_faixa_atributo CHECK (atributo IN ('PH', 'PH_AGUA',
    'MATERIA_ORGANICA', 'FOSFORO', 'POTASSIO', 'CALCIO', 'MAGNESIO', 'SATURACAO_BASES', 'SATURACAO_ALUMINIO', 'ZINCO', 'BORO'));

-- Tabela 3.2, p. 40. pH em H₂O, com a mesma convenção da coluna de CaCl₂ na V6
INSERT INTO nepar_faixa_interpretacao (versao_id, atributo, criterio_min, criterio_max, teor_min, teor_max, classe)
SELECT v.id, 'PH_AGUA', NULL, NULL, f.tmin, f.tmax, f.classe
  FROM nepar_versao_parametros v
 CROSS JOIN (VALUES
    (0::numeric, 4.7::numeric, 'Muito baixo'),
    (4.7, 5.2, 'Baixo'),
    (5.2, 5.7, 'Médio'),
    (5.7, 6.21, 'Alto'),
    (6.21, 6.71, 'Muito alto'),
    (6.71, NULL, 'Condição a evitar')
 ) AS f(tmin, tmax, classe)
 WHERE v.codigo = 'NEPAR-2019.1';
