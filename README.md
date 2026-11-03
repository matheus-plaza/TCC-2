# AgroMotor: SAD de Calagem e Adubação (Base NEPAR-SBCS, 2ª edição)

Sistema de Apoio à Decisão para recomendação de calagem e adubação de **soja, milho e trigo** no Paraná.
TCC de Ciência da Computação, UTFPR Santa Helena.

| Pasta | Conteúdo |
|---|---|
| `TCC-BACK/` | API REST: Java 21, Spring Boot 3.5, PostgreSQL 16, Flyway, JWT, Clean Architecture |
| `TCC-FRONT/` | SPA: Angular 22, Angular Material, RxJS, Reactive Forms, Chart.js |
| `docs/` | Rastreabilidade dos requisitos, parâmetros NEPAR, validação comparativa |

As tabelas de recomendação vêm do **Manual de Adubação e Calagem para o Estado do Paraná, 2ª ed.** (Pauletti e Motta, 2019,
SBCS/NEPAR). Cada valor usado e a página de origem estão em [`docs/dados-do-manual.md`](docs/dados-do-manual.md).

## Como executar

### Sem instalar nada além do Java 21 e do Node 22

O banco é um PostgreSQL 16 embutido, baixado pelo Maven, com os dados em `~/.agromotor/pgdata`.

```bash
cd TCC-BACK
./mvnw spring-boot:test-run
```

```bash
cd TCC-FRONT
npm ci
npm start
```

Acesse http://localhost:4200. A API fica em http://localhost:8080/api/v1 e o Swagger em
http://localhost:8080/swagger-ui.html.

### Com Docker

```bash
docker compose up -d --build
```

Acesse http://localhost:8081.

### Modo demonstração

O perfil Spring `demo` (padrão nas duas formas acima) cria no banco vazio agrônomo, produtores, propriedades, laudos
e recomendações geradas pelo motor real. A tela de login oferece botões para entrar com as contas de exemplo, que
estão em `TCC-BACK/src/main/resources/application-demo.properties`.

Fora da demonstração, nenhuma senha padrão acompanha o sistema: defina `SPRING_PROFILES_ACTIVE=default`,
`ADMIN_EMAIL`, `ADMIN_PASSWORD` e `JWT_SECRET` (pelo menos 32 bytes).

## Testes

```bash
cd TCC-BACK
./mvnw test
```

- `CalculadorCalagemTest`, `CalculadorGessagemTest`, `CalculadorAdubacaoNPKTest`, `CalculadorMicronutrientesTest`,
  `EngineCalculoAgronomicoTest`, `LaudoSoloTest`: motor isolado, sem Spring nem banco.
- `CasosControleTest`: validação comparativa com resultados de referência
  (`src/test/resources/validacao/casos-controle.csv`).
- `ArquiteturaTest` (ArchUnit): falha o build se a regra de dependência for violada ou se o domínio usar ponto flutuante.
- `FluxoRecomendacaoIntegracaoTest`, `SegurancaIntegracaoTest`, `ModoDemonstracaoIntegracaoTest`: API, JWT, RBAC,
  JPA e Flyway contra PostgreSQL 16 real, sem Docker, incluindo o limite de 300 ms da simulação.
- `ManualNeparIntegracaoTest`: tabelas publicadas no banco e cenários com valores do manual.

## Arquitetura

```
br.edu.tcc.agromotor
├── domain          Java puro: entidades, motor de cálculo e contratos dos repositórios
│   ├── model       LaudoSolo, Cultura, Recomendacao, regras paramétricas e resultados
│   ├── service     EngineCalculoAgronomico → CalculadorCalagem, CalculadorGessagem,
│   │               CalculadorAdubacaoNPK, CalculadorMicronutrientes (Strategy)
│   └── repository  ParametroRepository, LaudoRepository...
├── application     Casos de uso (@UseCase próprio, sem Spring), DTOs e portas
├── infrastructure  JPA/PostgreSQL, tabelas paramétricas, segurança/JWT, PDF, demonstração
└── presentation    Controllers REST e tratamento de erros (RFC 9457)
```

Principais rotas, todas em `/api/v1`:

| Requisito | Rota |
|---|---|
| RF01 | `POST /auth/login`, `POST /auth/registro`, `GET /auth/me` |
| RF02 | `/propriedades`, `/propriedades/{id}/talhoes`, `/talhoes/{id}`; vínculos em `/vinculos` |
| RF03, RF04 | `POST /laudos`, `GET /laudos?talhaoId=&profundidadeCm=&inicio=&fim=`, `GET /laudos/{id}`, `POST /laudos/indicadores` |
| RF05 | `POST /recomendacoes/simular` (em memória, resposta com `id` nulo) |
| RF06 | `POST /recomendacoes` (registra como SIMULACAO) e `POST /recomendacoes/{id}/adotar` |
| RF07 | `GET /talhoes/{id}/historico?inicio=&fim=` e `GET /dashboard/evolucao?talhaoId=` |
| Colheita e gasto | `GET` e `PUT /recomendacoes/{id}/safra` |
| RF08 | `GET /relatorios/receituario/{id}` |

As decisões de projeto e o que mudou em relação ao DER do TCC 1 estão em [`docs/rastreabilidade.md`](docs/rastreabilidade.md).
