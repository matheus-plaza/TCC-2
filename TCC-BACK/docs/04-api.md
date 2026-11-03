# API REST

Base: `http://localhost:8080/api/v1`. Com o sistema no ar, o Swagger está em <http://localhost:8080/swagger-ui.html>, e a especificação OpenAPI, em `/v3/api-docs`.

> Os exemplos foram capturados do sistema em execução, com os dados de demonstração. Os laudos são fictícios, e as doses saem das tabelas do manual, versão `NEPAR-2019.1`.

## 1. Convenções

| Item | Regra |
|---|---|
| Formato | JSON em UTF-8, exceto o receituário, que é PDF |
| Autenticação | Cabeçalho `Authorization: Bearer <token>` |
| Números decimais | Número JSON, sempre por extenso: `80.00`, nunca `8E+1` |
| Data | `AAAA-MM-DD` |
| Data e hora | `AAAA-MM-DDThh:mm:ss`, sem fuso |
| Expiração do token | Instante em UTC, como `2026-11-02T09:14:30Z` |
| Campo opcional ausente | Retorna `null` |
| Enums | Em maiúsculas, como `PLANTIO_DIRETO` |

### Formato dos erros

Todos os erros usam Problem Details (RFC 9457), com o tipo `application/problem+json`. Mostre ao usuário a lista `erros`: cada mensagem identifica o campo ou o parâmetro incorreto.

Nos erros de validação, a propriedade `campos` associa as mensagens ao nome de cada campo enviado, para a interface destacá-lo no formulário. A lista `erros` segue a ordem dos campos no corpo.

```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Matéria orgânica: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99); Alumínio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)",
  "instance": "/api/v1/laudos",
  "erros": [
    "Matéria orgânica: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99)",
    "Alumínio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)"
  ],
  "campos": {
    "moGDm3": ["Matéria orgânica: use até 3 dígitos inteiros e 2 casas decimais (máximo 999,99)"],
    "alCmolc": ["Alumínio: use até 2 dígitos inteiros e 3 casas decimais (máximo 99,999)"]
  }
}
```

| Status | Título | Quando ocorre |
|---|---|---|
| 400 | Dados inválidos | Campo ausente ou fora do formato |
| 400 | Requisição malformada | JSON inválido, enum desconhecido, parâmetro de URL inválido ou ausente |
| 401 | Não autenticado | Sem token, token inválido ou login incorreto |
| 403 | Acesso negado | Perfil sem permissão ou dado de outro produtor |
| 404 | Recurso não encontrado | Registro inexistente |
| 409 | Conflito | Duplicidade, registros vinculados ou recomendação adotada |
| 422 | Regra de negócio violada | Formato correto, mas a regra foi violada |
| 422 | Parâmetros NEPAR não cadastrados | As tabelas não cobrem o caso calculado |

## 2. Primeiros passos

As contas de demonstração estão em [`application-demo.properties`](../src/main/resources/application-demo.properties).

```bash
# 1. Login: copie o valor de "token" da resposta
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"SEU_EMAIL","senha":"SUA_SENHA"}'
```

```bash
# 2. Chamada autenticada
curl http://localhost:8080/api/v1/propriedades -H "Authorization: Bearer SEU_TOKEN"
```

```bash
# 3. Simulação: não grava nada
curl -X POST http://localhost:8080/api/v1/recomendacoes/simular -H "Authorization: Bearer SEU_TOKEN" -H "Content-Type: application/json" -d '{"analiseSoloId":4,"culturaAlvoId":1,"culturaAnteriorId":3,"sistemaManejo":"PLANTIO_DIRETO","expectativaRendimentoTonHa":4.5,"prntCalcario":100.0}'
```

No Swagger, faça o login, clique em **Authorize** e cole o token.

## 3. Permissões por rota

Legenda: **Sim** = permitido; **Escopo** = permitido só para dados de produtores no escopo do usuário; **Não** = 403.

| Rota | Público | Produtor | Agrônomo | Admin |
|---|---|---|---|---|
| `POST /auth/login` | Sim | Sim | Sim | Sim |
| `POST /auth/registro` | Sim, exceto perfil `ADMIN` | | | |
| `GET /auth/me` | Não | Sim | Sim | Sim |
| `GET /sistema/info` | Sim | Sim | Sim | Sim |
| `GET /culturas` | Não | Sim | Sim | Sim |
| `GET /parametros/vigentes` | Não | Sim | Sim | Sim |
| `GET /usuarios` | Não | Não | Seus produtores | Todos |
| `POST /usuarios` | Não | Não | Só produtor | Todos os perfis |
| `PUT /usuarios/{id}` | Não | A própria conta | A própria conta | Qualquer conta |
| `DELETE /usuarios/{id}` | Não | Não | Não | Exceto a própria |
| `GET /vinculos` | Não | Os seus | Os seus | Todos |
| `POST /vinculos` | Não | Autoriza por e-mail | Não | Sim |
| `DELETE /vinculos` | Não | Os seus | Os seus | Todos |
| `GET, POST /propriedades` | Não | Os seus dados | Escopo | Escopo total |
| `GET, PUT, DELETE /propriedades/{id}` | Não | Os seus dados | Escopo | Escopo total |
| `POST /propriedades/{id}/talhoes` | Não | Os seus dados | Escopo | Escopo total |
| `GET /talhoes` e `GET, PUT, DELETE /talhoes/{id}` | Não | Os seus dados | Escopo | Escopo total |
| `GET /talhoes/{id}/historico` | Não | Os seus dados | Escopo | Escopo total |
| `GET /talhoes/{id}/sugestao-antecessora` | Não | Os seus dados | Escopo | Escopo total |
| `POST /laudos/indicadores` | Não | Sim | Sim | Sim |
| `GET, POST /laudos` e `GET, DELETE /laudos/{id}` | Não | Os seus dados | Escopo | Escopo total |
| `POST /recomendacoes/simular` | Não | **Não** | Escopo | Escopo total |
| `POST /recomendacoes` | Não | **Não** | Escopo | **Não** |
| `POST /recomendacoes/{id}/adotar` | Não | **Não** | Escopo | **Não** |
| `GET /recomendacoes` e `GET /recomendacoes/{id}` | Não | Só adotadas | Escopo | Escopo total |
| `DELETE /recomendacoes/{id}` | Não | **Não** | Escopo | **Não** |
| `GET /recomendacoes/{id}/safra` | Não | Os seus dados | Escopo | Escopo total |
| `PUT /recomendacoes/{id}/safra` | Não | Os seus dados | Escopo | **Não** |
| `GET /relatorios/receituario/{id}` | Não | Só adotadas | Escopo | Escopo total |
| `GET /dashboard/resumo` | Não | Os seus dados | Escopo | Escopo total |
| `GET /dashboard/evolucao` | Não | Os seus dados | Escopo | Escopo total |

O produtor recebe 404, e não 403, ao pedir uma simulação do seu próprio talhão. Isso esconde o rascunho do agrônomo.

A interface web pode ser mais restrita que a API. Por exemplo, a tela "Tabelas NEPAR" aparece só para agrônomo e administrador, mas a rota aceita qualquer usuário autenticado.

## 4. Autenticação (`AuthController`)

### `POST /auth/login`

| Campo | Tipo | Regras |
|---|---|---|
| `email` | texto | Obrigatório, formato de e-mail |
| `senha` | texto | Obrigatório |

**200: `TokenResponse`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-02T09:14:30.173816Z",
  "usuario": {
    "id": 2,
    "nome": "Carla Mendes",
    "email": "agronomo@exemplo.com",
    "perfil": "AGRONOMO",
    "registroProfissional": "PR-000000/D",
    "dataCriacao": "2026-11-01T13:36:52.051046"
  }
}
```

Erros: 400 (formato) e 401 (e-mail ou senha inválidos).

### `POST /auth/registro`

Corpo `UsuarioRequest`:

| Campo | Tipo | Regras |
|---|---|---|
| `nome` | texto | Obrigatório, até 150 caracteres |
| `email` | texto | Obrigatório, até 150 caracteres |
| `senha` | texto | Obrigatório, de 8 a 72 caracteres |
| `perfil` | enum | Obrigatório: `PRODUTOR` ou `AGRONOMO` |
| `registroProfissional` | texto | Opcional, até 30 caracteres |

**201: `TokenResponse`**, já autenticado. Erros: 400, 409 (e-mail já usado) e 422 (perfil `ADMIN`).

### `GET /auth/me`

**200: `UsuarioResponse`**, com os dados da própria conta.

## 5. Sistema e parâmetros (`ParametroController`)

### `GET /sistema/info`

Público. Informa a versão dos parâmetros, para que a interface avise se os valores não tiverem sido conferidos com o manual.

```json
{
  "versaoParametros": {
    "id": 2,
    "codigo": "NEPAR-2019.1",
    "descricao": "Manual de Adubação e Calagem para o Estado do Paraná, 2ª ed. (Pauletti e Motta, 2019, SBCS/NEPAR).",
    "conferidaManual": true
  },
  "contasDemonstracao": [
    { "perfil": "AGRONOMO", "nome": "Carla Mendes", "email": "...", "senha": "..." }
  ]
}
```

`contasDemonstracao` traz dados apenas no perfil `demo`. Nos demais, é uma lista vazia.

### `GET /culturas`

```json
[
  { "id": 1, "nome": "Soja",  "vAlvo": 60.00, "vMinimo": 50.00, "phMinIdeal": 5.00, "phMaxIdeal": 5.50, "grupo": "LEGUMINOSA" },
  { "id": 2, "nome": "Milho", "vAlvo": 70.00, "vMinimo": 60.00, "phMinIdeal": 5.00, "phMaxIdeal": 5.50, "grupo": "GRAMINEA" },
  { "id": 3, "nome": "Trigo", "vAlvo": 70.00, "vMinimo": 60.00, "phMinIdeal": 5.00, "phMaxIdeal": 5.50, "grupo": "GRAMINEA" }
]
```

`vAlvo` é o V2 da calagem, e `vMinimo`, o limite abaixo do qual o calcário é indicado.

### `GET /parametros/vigentes`

Conteúdo da versão vigente: `ParametrosVigentesResponse`.

| Campo | Conteúdo |
|---|---|
| `versao` | Versão vigente |
| `culturas` | Lista de culturas |
| `faixas` | `atributo`, `rotulo`, `unidade`, `criterioMin`, `criterioMax`, `teorMin`, `teorMax`, `classe` |
| `npk` | `cultura`, `nutriente`, `classe`, `grupoAntecessor`, `rendimentoMin`, `rendimentoMax`, `doseMin`, `doseMax` |
| `gessagem` | `limiteMPorcentagem`, `limiteCaCmolc` e `doses`, com `argilaMin`, `argilaMax` e `doseKgHa` |
| `calagem` | `manejo`, `observacao` |
| `micronutrientes` | `cultura`, `nutriente`, `classe`, `doseKgHa` |

Limite nulo em uma faixa significa "sem limite". As faixas são semiabertas: `[min, max)`. "Até 20%" no manual aparece como `argilaMax: 20.01`.

Cada item de `npk` é uma célula do manual. `doseMin` e `doseMax` nulos significam "Inviável":

```json
{ "cultura": "Soja", "nutriente": "K₂O", "classe": "Baixo", "grupoAntecessor": null, "rendimentoMin": 5.01, "rendimentoMax": null, "doseMin": null, "doseMax": null }
```

Nas faixas de `POTASSIO`, `teorMin` e `teorMax` são a porcentagem de K na CTC a pH 7,0, indicada em `unidade`.

## 6. Usuários e vínculos (`UsuarioController`)

### `GET /usuarios`

Parâmetro opcional `perfil`, aplicado só para o administrador. **200:** lista de `UsuarioResponse`.

### `POST /usuarios`

Corpo `UsuarioRequest`, como no registro. O agrônomo só pode criar `PRODUTOR` e fica vinculado a ele.

**201: `UsuarioResponse`**. Erros: 400, 403 e 409.

### `PUT /usuarios/{id}`

Corpo `UsuarioAtualizacaoRequest`:

| Campo | Regras |
|---|---|
| `nome` | Obrigatório |
| `email` | Obrigatório |
| `senha` | Opcional, de 8 a 72 caracteres. Sem valor, mantém a atual |
| `registroProfissional` | Opcional. Vazio apaga o valor |

O perfil não pode ser alterado. **200: `UsuarioResponse`**. Erros: 400, 403, 404 e 409.

### `DELETE /usuarios/{id}`

**204**. Erros: 403, 404 e 409 (a própria conta, produtor com propriedades ou usuário com registros vinculados).

### `GET /vinculos`

**200:** lista de `VinculoResponse`: `{ "agronomo": UsuarioResponse, "produtor": UsuarioResponse }`.

### `POST /vinculos`

| Quem envia | Corpo |
|---|---|
| Produtor | `{ "agronomoEmail": "agronomo@exemplo.com" }` |
| Administrador | `{ "agronomoId": 2, "produtorId": 3 }` |
| Agrônomo | Recebe 403: "O vínculo é autorizado pelo produtor ou pelo administrador" |

**201: `VinculoResponse`**. Erros: 400, 403, 404 e 422 (e-mail não é de agrônomo ou perfil incorreto).

### `DELETE /vinculos?agronomoId=&produtorId=`

Os dois parâmetros são obrigatórios. **204**. Erro: 403 para quem não participa do vínculo.

## 7. Propriedades e talhões (`PropriedadeController`)

### Propriedades

| Rota | Corpo | Sucesso | Erros |
|---|---|---|---|
| `GET /propriedades` | | 200, lista | |
| `GET /propriedades/{id}` | | 200 | 403, 404 |
| `POST /propriedades` | `PropriedadeRequest` | 201 | 400, 403, 404, 422 |
| `PUT /propriedades/{id}` | `PropriedadeRequest` | 200 | 400, 403, 404 |
| `DELETE /propriedades/{id}` | | 204 | 403, 404, 409 (tem talhões) |

`PropriedadeRequest`:

| Campo | Regras |
|---|---|
| `produtorId` | Obrigatório para agrônomo e admin ao criar. Ignorado para produtor e na atualização |
| `nome` | Obrigatório, até 150 caracteres |
| `municipio` | Obrigatório, até 100 caracteres |

`PropriedadeResponse`:

```json
{
  "id": 1,
  "produtorId": 3,
  "produtorNome": "José Antunes",
  "nome": "Sítio Boa Esperança",
  "municipio": "Santa Helena",
  "uf": "PR",
  "quantidadeTalhoes": 2,
  "areaTotalHectares": 50.50
}
```

### Talhões

| Rota | Corpo | Sucesso | Erros |
|---|---|---|---|
| `GET /talhoes?propriedadeId=` | | 200, lista | |
| `GET /talhoes/{id}` | | 200 | 403, 404 |
| `POST /propriedades/{propriedadeId}/talhoes` | `TalhaoRequest` | 201 | 400, 403, 404, 409 |
| `PUT /talhoes/{id}` | `TalhaoRequest` | 200 | 400, 403, 404, 409 |
| `DELETE /talhoes/{id}` | | 204 | 403, 404, 409 (tem laudos) |

`TalhaoRequest`:

| Campo | Regras |
|---|---|
| `identificacao` | Obrigatório, até 100 caracteres, único na propriedade |
| `areaHectares` | Obrigatório, no mínimo 0,01, até 8 dígitos inteiros e 2 decimais |
| `texturaPredominante` | Opcional: `LEVE`, `MEDIA`, `ARGILOSA`, `MUITO_ARGILOSA` |

`TalhaoResponse`:

```json
{
  "id": 1,
  "identificacao": "Gleba 01",
  "areaHectares": 32.50,
  "texturaPredominante": "ARGILOSA",
  "propriedadeId": 1,
  "propriedadeNome": "Sítio Boa Esperança",
  "municipio": "Santa Helena",
  "uf": "PR",
  "produtorId": 3,
  "produtorNome": "José Antunes"
}
```

### `GET /talhoes/{id}/historico?inicio=&fim=`

Datas opcionais. Linha do tempo com laudos e recomendações adotadas: `HistoricoTalhaoResponse`.

```json
{
  "talhao": { "id": 1, "identificacao": "Gleba 01" },
  "eventos": [
    {
      "tipo": "LAUDO",
      "data": "2026-09-15",
      "referenciaId": 4,
      "titulo": "Laudo de solo (0-20 cm)",
      "detalhe": null,
      "laudo": { "profundidadeCm": "0-20", "ph": 5.20, "metodoPh": "CACL2", "v": 49.2, "m": 2.1, "mo": 28.50, "p": 12.00, "k": 0.350 },
      "doses": null
    },
    {
      "tipo": "RECOMENDACAO_ADOTADA",
      "data": "2026-04-20",
      "referenciaId": 6,
      "titulo": "Recomendação adotada para Trigo",
      "detalhe": "Manejo: Plantio Direto",
      "laudo": null,
      "doses": {
        "cultura": "Trigo",
        "calcarioTonHa": 2.64,
        "gessoKgHa": 0.00,
        "nKgHa": { "min": 31.00, "max": 50.00 },
        "p2o5KgHa": { "min": 31.00, "max": 40.00 },
        "k2oKgHa": { "min": 10.00, "max": 20.00 }
      }
    }
  ]
}
```

O objeto `talhao` foi reduzido neste exemplo. Na resposta real, é um `TalhaoResponse` completo.

### `GET /talhoes/{id}/sugestao-antecessora`

Cultura da última recomendação adotada no talhão, para pré-selecionar a antecessora.

**200:**

```json
{
  "cultura": { "id": 3, "nome": "Trigo", "vAlvo": 70.00, "vMinimo": 60.00, "phMinIdeal": 5.00, "phMaxIdeal": 5.50, "grupo": "GRAMINEA" },
  "recomendacaoId": 6,
  "dataAdocao": "2026-04-20T10:00:00"
}
```

**204:** o talhão ainda não tem recomendação adotada. Erros: 403 e 404.

## 8. Laudos (`LaudoController`)

### `POST /laudos`

Corpo `LaudoRequest`:

| Campo | Unidade | Formato (400) | Regra de negócio (422) |
|---|---|---|---|
| `talhaoId` | | Obrigatório | |
| `dataAnalise` | | Obrigatório, não pode ser futura | |
| `profundidadeCm` | cm | Obrigatório: `0-20` ou `20-40` | |
| `ph` | | Obrigatório, de 3,0 a 9,0, até 2 decimais | |
| `metodoPh` | | Obrigatório: `CACL2` ou `AGUA`. Define a coluna da Tabela 3.2 usada na classe; não muda nenhuma dose | |
| `moGDm3` | g/dm³ | Obrigatório, até 3 inteiros e 2 decimais | Maior que zero |
| `pMehlich` | mg/dm³ | Obrigatório, até 3 inteiros e 2 decimais | Maior que zero |
| `kCmolc` | cmolc/dm³ | Obrigatório, até 2 inteiros e 3 decimais | Maior que zero |
| `caCmolc` | cmolc/dm³ | Obrigatório, até 2 inteiros e 3 decimais | Maior que zero |
| `mgCmolc` | cmolc/dm³ | Obrigatório, até 2 inteiros e 3 decimais | Maior que zero |
| `alCmolc` | cmolc/dm³ | Obrigatório, até 2 inteiros e 3 decimais | Não negativo |
| `hAlCmolc` | cmolc/dm³ | Obrigatório, até 2 inteiros e 3 decimais | Maior que zero |
| `argilaPorcentagem` | % | Obrigatório, de 0 a 100, até 2 decimais | |
| `znMgDm3` | mg/dm³ | Opcional, até 4 inteiros e 2 decimais | Não negativo |
| `bMgDm3` | mg/dm³ | Opcional, até 4 inteiros e 2 decimais | Não negativo |
| `cuMgDm3` | mg/dm³ | Opcional, até 4 inteiros e 2 decimais | Não negativo |
| `mnMgDm3` | mg/dm³ | Opcional, até 4 inteiros e 2 decimais | Não negativo |

```json
{
  "talhaoId": 1,
  "dataAnalise": "2026-09-15",
  "profundidadeCm": "0-20",
  "ph": 5.2,
  "metodoPh": "CACL2",
  "moGDm3": 28.5,
  "pMehlich": 12.0,
  "kCmolc": 0.35,
  "caCmolc": 3.2,
  "mgCmolc": 1.1,
  "alCmolc": 0.1,
  "hAlCmolc": 4.8,
  "argilaPorcentagem": 45.0,
  "znMgDm3": 1.4,
  "bMgDm3": 0.35
}
```

**201: `LaudoResponse`**. Erros: 400, 403, 404 e 422.

Exemplo real de 422:

```json
{
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "Fósforo deve ser maior que zero; Cálcio deve ser maior que zero",
  "erros": ["Fósforo deve ser maior que zero", "Cálcio deve ser maior que zero"]
}
```

### `GET /laudos`

Filtros opcionais: `talhaoId`, `profundidadeCm` (`0-20` ou `20-40`; outro valor gera 422), `inicio` e `fim`. Ordem: do mais recente ao mais antigo. Na lista, `interpretacao` vem vazia.

### `GET /laudos/{id}`

**200: `LaudoResponse`**, com interpretação:

```json
{
  "id": 4,
  "talhaoId": 1,
  "talhaoIdentificacao": "Gleba 01",
  "propriedadeId": 1,
  "propriedadeNome": "Sítio Boa Esperança",
  "produtorId": 3,
  "produtorNome": "José Antunes",
  "dataAnalise": "2026-09-15",
  "profundidadeCm": "0-20",
  "ph": 5.20,
  "metodoPh": "CACL2",
  "moGDm3": 28.50,
  "pMehlich": 12.00,
  "kCmolc": 0.350,
  "caCmolc": 3.200,
  "mgCmolc": 1.100,
  "alCmolc": 0.100,
  "hAlCmolc": 4.800,
  "argilaPorcentagem": 45.00,
  "znMgDm3": 1.40,
  "bMgDm3": 0.35,
  "indicadores": {
    "somaBases": 4.65,
    "ctcEfetiva": 4.75,
    "ctcTotal": 9.45,
    "saturacaoBases": 49.2,
    "saturacaoAluminio": 2.1
  },
  "interpretacao": [
    { "atributo": "FOSFORO", "rotulo": "Fósforo (P)", "valor": 12.00, "unidade": "mg/dm³", "classe": "Alto" },
    { "atributo": "POTASSIO", "rotulo": "Potássio (K⁺)", "valor": 0.350, "unidade": "cmolc/dm³", "classe": "Muito alto" }
  ]
}
```

A lista `interpretacao` foi reduzida neste exemplo. A resposta traz todos os atributos com valor. `classe` é nula quando a tabela não cobre o valor. No potássio, `valor` é o teor do laudo, e `classe` vem da porcentagem de K na CTC: 0,35 / 9,45 = 3,7%.

### `DELETE /laudos/{id}`

**204**. Erros: 403, 404 e 409 ("Este laudo já foi usado em recomendações e não pode ser excluído").

### `POST /laudos/indicadores`

Calcula a prévia sem gravar. `IndicadoresRequest`: `caCmolc`, `mgCmolc`, `kCmolc`, `alCmolc` e `hAlCmolc`, todos obrigatórios e não negativos.

```json
{ "caCmolc": 3.2, "mgCmolc": 1.1, "kCmolc": 0.35, "alCmolc": 0.1, "hAlCmolc": 4.8 }
```

**200: `IndicadoresSolo`**

```json
{ "somaBases": 4.65, "ctcEfetiva": 4.75, "ctcTotal": 9.45, "saturacaoBases": 49.2, "saturacaoAluminio": 2.1 }
```

## 9. Recomendações (`RecomendacaoController`)

### Corpo: `RecomendacaoRequestDTO`

Usado em `POST /recomendacoes/simular` e `POST /recomendacoes`.

| Campo | Regras | Observação |
|---|---|---|
| `analiseSoloId` | Obrigatório | Laudo de 0-20 cm. O de 20-40 cm é encontrado automaticamente |
| `culturaAlvoId` | Obrigatório | |
| `culturaAnteriorId` | Opcional | `null`, sem `grupoAnterior`, significa pousio/palhada. Na soja, o cálculo segue com N igual a 0. No milho e no trigo, gera 422, porque o manual não traz dose |
| `grupoAnterior` | Opcional | `GRAMINEA` ou `LEGUMINOSA`, para a cultura anterior que não está cadastrada. Usa a linha desse grupo na tabela de N. Junto com `culturaAnteriorId`, gera 422 |
| `sistemaManejo` | Obrigatório | `PLANTIO_DIRETO` ou `CONVENCIONAL` |
| `expectativaRendimentoTonHa` | Obrigatório, no mínimo 0,01 | Até 3 inteiros e 2 decimais |
| `prntCalcario` | Opcional, de 0,01 a 200 | `null` assume 100 |

```json
{
  "analiseSoloId": 4,
  "culturaAlvoId": 1,
  "culturaAnteriorId": 3,
  "sistemaManejo": "PLANTIO_DIRETO",
  "expectativaRendimentoTonHa": 4.5,
  "prntCalcario": 100.0
}
```

### `POST /recomendacoes/simular`

Calcula em memória, **sem gravar**. **200**, com `id` nulo:

```json
{
  "id": null,
  "status": "SIMULACAO",
  "analiseSoloId": 4,
  "resumoQuimico": {
    "somaBases": 4.65,
    "ctcEfetiva": 4.75,
    "ctcTotal": 9.45,
    "v1Porcentagem": 49.2,
    "mPorcentagem": 2.1
  },
  "calagem": {
    "necessario": true,
    "v2AlvoPorcentagem": 60.0,
    "necessidadeCalcarioTonHa": 1.02,
    "ncCalculadaTonHa": 1.02,
    "observacao": "Em área consolidada, aplicar em superfície, de uma vez ou parcelado em até três anos. Na implantação do sistema, incorporar até pelo menos 20 cm.",
    "justificativa": "V1% atual (49,2%) abaixo do limite de 50,0% da cultura: elevar para V2% de 60,0%."
  },
  "gessagem": {
    "necessario": false,
    "doseKgHa": 0.00,
    "avaliada": true,
    "justificativa": "Gessagem não necessária: nenhum critério acionado na camada de 20-40 cm."
  },
  "adubacao": {
    "doseNkgHa": { "min": 0.00, "max": 0.00 },
    "doseP2O5kgHa": { "min": 61.00, "max": 80.00 },
    "doseK2OkgHa": { "min": 50.00, "max": 70.00 },
    "justificativaN": "FBN ativada. Antecessora Gramínea compensada.",
    "classeFosforo": "Alto",
    "justificativaP": "Teor de P Alto com argila de 45,0%.",
    "classePotassio": "Muito alto",
    "justificativaK": "K de 0,35 cmolc/dm³ equivale a 3,7% da CTC: classe Muito alto.",
    "classeMateriaOrganica": "Alto"
  },
  "micronutrientes": [
    {
      "nutriente": "Zinco",
      "simbolo": "Zn",
      "avaliado": true,
      "teor": 1.40,
      "classe": "Alto",
      "doseKgHa": 0.00,
      "justificativa": "Teor de 1,40 mg/dm³, classe Alto: 0,00 kg/ha."
    }
  ],
  "culturaAlvo": { "id": 1, "nome": "Soja" },
  "culturaAnterior": { "id": 3, "nome": "Trigo" },
  "grupoAnterior": "GRAMINEA",
  "antecessora": "Trigo",
  "sistemaManejo": "PLANTIO_DIRETO",
  "expectativaRendimentoTonHa": 4.5,
  "prntCalcario": 100.0,
  "talhao": { "id": 1, "identificacao": "Gleba 01" },
  "laudo": { "id": 4 },
  "laudoSubsuperficieId": 5,
  "versaoParametros": { "id": 2, "codigo": "NEPAR-2019.1", "conferidaManual": true },
  "dataGeracao": "2026-11-01T18:00:00",
  "dataAdocao": null,
  "responsavel": null,
  "avisos": [],
  "memoriaCalculo": [
    "SB = Ca + Mg + K = 3,20 + 1,10 + 0,35 = 4,65 cmolc/dm³",
    "t = SB + Al = 4,65 + 0,10 = 4,75 cmolc/dm³",
    "T = SB + (H+Al) = 4,65 + 4,80 = 9,45 cmolc/dm³",
    "V1% = SB / T × 100 = 49,2%",
    "m% = Al / t × 100 = 2,1%",
    "K na CTC = K / T × 100 = 3,7%",
    "NC = (V2 − V1) × T / PRNT = (60,0% − 49,2%) × 9,45 / 100,0% = 1,02 t/ha",
    "Gessagem: não indicada pelo laudo 20-40 cm de 15/09/2026 (m% 4,7%, Ca²⁺ 1,40)",
    "P₂O₅: Soja, P Alto, rendimento 4,50 t/ha = 61,00 a 80,00 kg/ha",
    "K₂O: Soja, K Muito alto, rendimento 4,50 t/ha = 50,00 a 70,00 kg/ha",
    "N: Soja é leguminosa, Fixação Biológica de Nitrogênio dispensa N mineral: 0,00 kg/ha",
    "Zn: Teor de 1,40 mg/dm³, classe Alto: 0,00 kg/ha.",
    "B: Teor de 0,35 mg/dm³, classe Alto: 0,00 kg/ha."
  ]
}
```

Para caber aqui, `culturaAlvo`, `culturaAnterior`, `talhao`, `laudo` e `versaoParametros` foram reduzidos, e a lista de micronutrientes mostra só o zinco. A data é ilustrativa.

| Bloco | Conteúdo |
|---|---|
| `resumoQuimico` | Indicadores do laudo |
| `calagem` | `necessidadeCalcarioTonHa` é a dose final; `ncCalculadaTonHa`, o resultado da fórmula. A dose é zero se V1 não estiver abaixo do limite da cultura |
| `gessagem` | `avaliada: false` significa que não há laudo de 20-40 cm |
| `adubacao` | Faixas de dose, com `min` e `max`, classes e justificativas. Cabe ao agrônomo definir o valor dentro da faixa |
| `micronutrientes` | Classe e dose nulas quando `avaliado` é falso. Só a soja tem dose no manual |
| `talhao`, `laudo`, `laudoSubsuperficieId` | Contexto |
| `versaoParametros` | Versão usada. `conferidaManual: false` exige aviso ao usuário |
| `responsavel` | `id`, `nome` e `registroProfissional`. Nulo na simulação em memória |
| `avisos` | Pontos de atenção, como teor de micronutriente não informado ou gessagem não avaliada |
| `memoriaCalculo` | Cada conta feita, para auditoria |

**Erros:**

| Status | Situação |
|---|---|
| 400 | Campo ausente ou enum inválido |
| 403 | Perfil de produtor ou produtor fora do escopo |
| 404 | Laudo ou cultura inexistente |
| 422 | Laudo de 20-40 cm usado como base |
| 422 | Produtividade marcada como "Inviável" no manual |
| 422 | Milho ou trigo sem cultura anterior |
| 422 | Parâmetros ausentes, com a lista do que falta |

Resposta real quando o manual marca a célula como "Inviável":

```json
{
  "type": "about:blank",
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "Produtividade de 17,00 t/ha é inviável para Milho com P Baixo no solo, segundo o manual.",
  "instance": "/api/v1/recomendacoes/simular",
  "erros": ["Produtividade de 17,00 t/ha é inviável para Milho com P Baixo no solo, segundo o manual."]
}
```

Milho sem cultura anterior tem o mesmo formato, com a mensagem: "O manual não traz dose de N para Milho sem cultura anterior: selecione uma gramínea ou uma leguminosa."

Formato do erro de parâmetros ausentes, gerado por `CalculadorAdubacaoNPK`. Com as tabelas completas, ele só aparece se uma nova versão publicada tiver alguma lacuna:

```json
{
  "title": "Parâmetros NEPAR não cadastrados",
  "status": 422,
  "erros": ["Dose de P₂O₅ para Soja, P Médio e rendimento de 4,50 t/ha"]
}
```

### `POST /recomendacoes`

Recalcula no servidor e grava com status `SIMULACAO`. **201**, com a mesma estrutura e `id` preenchido. Os erros são os mesmos da simulação, e somente o agrônomo pode usar.

### `POST /recomendacoes/{id}/adotar`

Sem corpo. Muda o status para `ADOTADA`. A partir daí, o registro não muda mais.

**200: `RecomendacaoResponseDTO`**, com `dataAdocao` e `responsavel`.

| Status | Situação |
|---|---|
| 403 | Não é agrônomo ou o produtor está fora do escopo |
| 404 | Inexistente |
| 422 | "Esta recomendação já foi adotada" |

### `GET /recomendacoes`

Filtros opcionais: `talhaoId`, `laudoId`, `status`, `inicio` e `fim`. O período considera a data de adoção ou, na falta dela, a de geração. Ordem: da mais recente à mais antiga.

Para o produtor, o filtro é sempre `ADOTADA`.

### `GET /recomendacoes/{id}`

**200: `RecomendacaoResponseDTO`**. Erros: 403 e 404.

### `DELETE /recomendacoes/{id}`

Descarta uma simulação salva. **204**. Erros: 403, 404 e 409 ("Recomendação adotada não pode ser excluída").

### `GET /recomendacoes/{id}/safra`

**200: `SafraResponse`**, com a produtividade esperada na recomendação e o resultado de campo. Os campos do resultado ficam nulos até serem registrados. Erros: 403, 404 e 409 (recomendação não adotada).

```json
{
  "recomendacaoId": 1,
  "talhaoId": 1,
  "talhao": "Gleba 01",
  "propriedade": "Sítio Boa Esperança",
  "cultura": "Soja",
  "dataAdocao": "2023-10-05",
  "produtividadeEsperadaTonHa": 4.00,
  "produtividadeColhidaTonHa": 3.70,
  "dataColheita": "2024-02-20",
  "diferencaPercentual": -7.5,
  "custoAdubacaoReais": 31000.00,
  "custoPorHectareReais": 953.85
}
```

### `PUT /recomendacoes/{id}/safra`

Cria ou substitui o resultado da safra. Não altera a recomendação adotada nem nenhuma dose. Com todos os campos nulos, o registro é removido.

| Campo | Unidade | Formato (400) | Regra de negócio (422) |
|---|---|---|---|
| `produtividadeColhidaTonHa` | t/ha | Opcional, até 3 inteiros e 2 decimais | Maior que zero; exige `dataColheita` |
| `dataColheita` | | Opcional, não pode ser futura | Não pode ser anterior à adoção; exige a produtividade |
| `custoAdubacaoReais` | R$ | Opcional, até 10 inteiros e 2 decimais. Gasto total no talhão | Não negativo |

**200: `SafraResponse`**. O gasto por hectare usa a área atual do talhão. Erros: 400, 403, 404, 409 e 422.

## 10. Receituário (`RelatorioController`)

### `GET /relatorios/receituario/{id}`

**200**, com `application/pdf` e `Content-Disposition: attachment; filename="receituario-6.pdf"`.

| Status | Situação |
|---|---|
| 403 | Produtor fora do escopo |
| 404 | Inexistente. Também para produtor que pede uma simulação |
| 422 | "O receituário é emitido apenas para recomendações adotadas" |

```bash
curl http://localhost:8080/api/v1/relatorios/receituario/6 -H "Authorization: Bearer SEU_TOKEN" -o receituario-6.pdf
```

## 11. Painel (`DashboardController`)

### `GET /dashboard/resumo`

**200: `ResumoPainelResponse`**

```json
{
  "propriedades": 2,
  "talhoes": 4,
  "areaTotalHectares": 146.70,
  "laudos": 8,
  "recomendacoesAdotadas": 7,
  "simulacoesSalvas": 1,
  "colheitasRegistradas": 7,
  "colheitasRecentes": [],
  "laudosRecentes": [
    { "id": 5, "talhaoId": 1, "data": "2026-09-15", "profundidadeCm": "20-40", "talhao": "Gleba 01", "propriedade": "Sítio Boa Esperança", "v": 31.0, "ph": 4.90, "metodoPh": "CACL2" }
  ],
  "recomendacoesRecentes": [
    { "id": 8, "data": "2026-10-20", "status": "SIMULACAO", "cultura": "Milho", "talhao": "Gleba 02", "propriedade": "Sítio Boa Esperança", "calcarioTonHa": 3.16 }
  ],
  "talhoesSemLaudo": [
    { "id": 4, "identificacao": "Talhão Sul" }
  ]
}
```

As listas foram reduzidas neste exemplo. As de recentes têm até 5 itens, `colheitasRecentes` é uma lista de `SafraResponse`, e `talhoesSemLaudo`, de `TalhaoResponse`.

### `GET /dashboard/evolucao?talhaoId=`

`talhaoId` é obrigatório. **200: `EvolucaoResponse`**, em ordem cronológica. Os pontos vêm só dos laudos de 0-20 cm.

```json
{
  "talhao": { "id": 1, "identificacao": "Gleba 01" },
  "pontos": [
    { "data": "2026-09-15", "laudoId": 4, "ph": 5.20, "metodoPh": "CACL2", "mo": 28.50, "p": 12.00, "k": 0.350, "ca": 3.200, "mg": 1.100, "v": 49.2, "m": 2.1, "ctc": 9.45 }
  ],
  "adotadas": [
    {
      "data": "2023-10-05",
      "recomendacaoId": 1,
      "cultura": "Soja",
      "calcarioTonHa": 3.07,
      "gessoKgHa": 0.00,
      "nKgHa": { "min": 0.00, "max": 0.00 },
      "p2o5KgHa": { "min": 81.00, "max": 100.00 },
      "k2oKgHa": { "min": 71.00, "max": 90.00 }
    }
  ],
  "safras": []
}
```

`safras` é uma lista de `SafraResponse`, só com as recomendações que têm resultado registrado. Os objetos e as listas foram reduzidos neste exemplo. Erros: 400 (sem `talhaoId`), 403 e 404.

## 12. Catálogo de respostas

| DTO | Campos |
|---|---|
| `UsuarioResponse` | `id`, `nome`, `email`, `perfil`, `registroProfissional`, `dataCriacao` |
| `TokenResponse` | `token`, `tipo`, `expiraEm`, `usuario` |
| `VinculoResponse` | `agronomo`, `produtor` |
| `CulturaResponse` | `id`, `nome`, `vAlvo`, `vMinimo`, `phMinIdeal`, `phMaxIdeal`, `grupo` |
| `PropriedadeResponse` | `id`, `produtorId`, `produtorNome`, `nome`, `municipio`, `uf`, `quantidadeTalhoes`, `areaTotalHectares` |
| `TalhaoResponse` | `id`, `identificacao`, `areaHectares`, `texturaPredominante`, `propriedadeId`, `propriedadeNome`, `municipio`, `uf`, `produtorId`, `produtorNome` |
| `LaudoResponse` | Dados do laudo e do talhão, `indicadores`, `interpretacao` |
| `RecomendacaoResponseDTO` | [Seção 9](#9-recomendações-recomendacaocontroller) |
| `SugestaoAntecessoraResponse` | `cultura`, `recomendacaoId`, `dataAdocao` |
| `HistoricoTalhaoResponse` | `talhao`, `eventos` |
| `EvolucaoResponse` | `talhao`, `pontos`, `adotadas` |
| `ResumoPainelResponse` | Contagens, `areaTotalHectares`, `laudosRecentes`, `recomendacoesRecentes`, `talhoesSemLaudo` |
| `ParametrosVigentesResponse` | [Seção 5](#5-sistema-e-parâmetros-parametrocontroller) |
| `SistemaInfoResponse` | `versaoParametros`, `contasDemonstracao` |

## 13. Rotas fora da API

Estas rotas são públicas:

| Rota | Conteúdo |
|---|---|
| `/swagger-ui.html` | Interface do Swagger |
| `/v3/api-docs` | Especificação OpenAPI em JSON |

O CORS vale para `/api/**`, com as origens definidas em `CORS_ORIGINS`, os métodos `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS`, os cabeçalhos `Authorization` e `Content-Type` e a exposição de `Location` e `Content-Disposition`.
