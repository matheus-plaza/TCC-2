# Documentação do backend do AgroMotor

O AgroMotor é o backend do Sistema de Apoio à Decisão para recomendação de calagem e adubação de soja, milho e trigo no
Paraná. Ele recebe um laudo de solo, calcula as doses com base em tabelas guardadas no banco, registra a decisão do
agrônomo e emite o receituário em PDF.

## Por onde começar

| Ordem | Documento | Responde a |
|---|---|---|
| 1 | [Arquitetura](01-arquitetura.md) | Como o projeto está organizado? Qual é o objetivo de cada classe? Como uma requisição percorre o sistema? |
| 2 | [Model](02-model.md) | O que significa cada dado? Quais regras cada objeto garante? Como isso vira tabela no banco? |
| 3 | [Services](03-services.md) | Como cada dose é calculada? O que cada caso de uso faz e quem pode executá-lo? |
| 4 | [API](04-api.md) | Quais rotas existem? O que enviar, o que volta e quais erros podem ocorrer? |

Se você quer entender o sistema pela primeira vez, leia nessa ordem. Se quer localizar uma classe, use o
[catálogo de classes](01-arquitetura.md#10-catálogo-de-todas-as-classes).

## Sobre os exemplos

As respostas e os resultados de cálculo mostrados aqui foram capturados do sistema em execução, com os dados de
demonstração, e os e-mails foram trocados por endereços genéricos. Os laudos, as contas e as propriedades são fictícios; as
tabelas de recomendação são as do manual, versão `NEPAR-2019.1`.

## Outros documentos do projeto

- [`docs/dados-do-manual.md`](../../docs/dados-do-manual.md): cada dado tirado do livro, com a página e as decisões de leitura.
- [`docs/parametros-nepar.md`](../../docs/parametros-nepar.md): onde cada dado fica e como publicar uma nova edição do manual.
- [`docs/rastreabilidade.md`](../../docs/rastreabilidade.md): onde cada requisito do SRS foi atendido e como as ambiguidades foram resolvidas.
- Swagger, com o sistema no ar: <http://localhost:8080/swagger-ui.html>.

Os diagramas usam [Mermaid](https://mermaid.js.org/). O GitHub os desenha automaticamente; no VS Code ou no IntelliJ, pode ser
preciso instalar o suporte a Mermaid na visualização de Markdown.
