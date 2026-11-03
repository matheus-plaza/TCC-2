# Como executar o AgroMotor

Este arquivo resume os passos para rodar o sistema. O `README.md` traz a visão geral do projeto, e a pasta
`TCC-BACK/docs` documenta a arquitetura, o modelo, os serviços e a API.

## Opção 1: Java e Node (sem Docker)

Pré-requisitos: **Java 21** e **Node 22**. Não é preciso instalar o PostgreSQL: o backend usa um PostgreSQL 16
embutido, baixado na primeira execução.

**1. Backend**, em um terminal:

```bash
cd TCC-BACK
./mvnw spring-boot:test-run
```

No Windows (cmd ou PowerShell), use `mvnw.cmd spring-boot:test-run`.

A primeira execução baixa as dependências do Maven e o PostgreSQL embutido, então precisa de internet e pode levar alguns
minutos. O backend está pronto quando aparece `Started AgroMotorApplication` no terminal. A API fica em
http://localhost:8080/api/v1 e o Swagger em http://localhost:8080/swagger-ui.html.

**2. Frontend**, em outro terminal:

```bash
cd TCC-FRONT
npm ci
npm start
```

Depois, abra http://localhost:4200.

## Opção 2: Docker

Com o Docker Desktop aberto, na pasta raiz:

```bash
docker compose up -d --build
```

Depois, abra http://localhost:8081. Para encerrar, use `docker compose down`.

## Entrando no sistema

O sistema sobe no modo demonstração, com dados fictícios: agrônomo, produtores, propriedades, laudos e recomendações já
calculadas. Na tela de login, os botões **Agrônomo**, **Produtor** e **Administrador** entram direto com as contas de
exemplo, sem digitar senha.

Um roteiro curto para ver o fluxo principal:

1. Entrar como **Agrônomo**.
2. Em **Nova recomendação**, escolher o talhão Gleba 01, a cultura soja e um rendimento, por exemplo 4,5 t/ha, e simular.
3. Conferir a interpretação do laudo, as doses e a memória de cálculo; depois, adotar a recomendação.
4. Em **Recomendações**, abrir a recomendação adotada e exportar o receituário em PDF.
5. Entrar como **Produtor** para ver o painel de evolução do solo e os receituários adotados.

## Testes

```bash
cd TCC-BACK
./mvnw test
```

São 148 testes: motor de cálculo, casos de controle, regras de arquitetura (ArchUnit) e integração com PostgreSQL real,
sem Docker.

## Problemas comuns

- **Porta ocupada:** sem Docker, o backend usa a 8080 e o frontend a 4200. Com Docker, são usadas a 5432 (banco), a 8080
  (API) e a 8081 (tela). As duas opções não rodam ao mesmo tempo.
- **`npm ci` falha:** confira se o Node é a versão 22 (`node -v`).
- **O frontend abre em branco:** o backend ainda está subindo; espere a mensagem `Started` e recarregue a página.
