# DIO Spring Boot - Final Project 05: Spring AI (budgeting)

## Introduction

This final module applies Spring AI in a budgeting API while preserving the same layered architecture used across the track.

The goal is to integrate AI capabilities without bypassing domain and use case boundaries.

## Code Context

The project processes voice commands to create and query financial transactions.

Primary flow:

1. Client uploads an audio file.
2. Audio is transcribed into text.
3. The model selects an application tool/use case.
4. The use case persists or queries transaction data.
5. The final response is converted to audio.

## Project Structure

- `src/main/java/dio/budgeting/domain`
  - Domain model and repository contract.
- `src/main/java/dio/budgeting/application`
  - Use cases used by both REST and AI tool calling.
- `src/main/java/dio/budgeting/infrastructure`
  - HTTP adapters, JPA adapters, and integration glue.

## Module-Specific Topics

### Speech-to-text

- Uses `TranscriptionModel` for audio transcription.
- Model settings are configured in `application.properties`.

### Tool calling

- `ChatClient` registers use-case tools.
- `@Tool` methods expose business capabilities to the model.

### Text-to-speech

- `TextToSpeechModel` produces MP3 output from final text.
- AI endpoint returns generated audio.

## Spring AI Documentation

- Spring AI Reference: https://docs.spring.io/spring-ai/reference/index.html
- ChatModel API: https://docs.spring.io/spring-ai/reference/api/chatmodel.html
- ChatClient API: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Tools API: https://docs.spring.io/spring-ai/reference/api/tools.html
- Audio Transcriptions API: https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html
- Audio Speech API: https://docs.spring.io/spring-ai/reference/api/audio/speech.html

## Shared Architecture References

Common architecture concepts are documented in the root README:

- [DDD layers](../README.md#ddd-layered-architecture)
- [Class vs record](../README.md#java-class-vs-java-record-in-domain-modeling)
- [Strong typed identifiers](../README.md#strong-typed-identifiers)
- [Repository pattern](../README.md#repository-pattern)
- [Use cases and Clean Architecture](../README.md#use-cases-and-clean-architecture)
- [Docker Compose support](../README.md#docker-compose-support-in-development)

## How to Run

Set your OpenAI API key:

```bash
export OPENAI_API_KEY="your_api_key_here"
```

Run the application and tests:

```bash
./gradlew bootRun
./gradlew test
```

## Notes

- Educational final project focused on AI plus architectural discipline.
- External provider integration tests may require active credentials.

---

## Minha Evolução (Desafio de Projeto DIO)

Este fork foi feito como entrega do desafio de projeto da trilha **Itaú Java
com Inteligência Artificial**, módulo *Desenvolvendo sua API Inteligente com
Reconhecimento de Fala e Spring Boot*.

### O que o projeto faz

A API recebe um comando de voz (áudio) sobre gastos financeiros, transcreve o
áudio em texto, usa um modelo de linguagem (via Spring AI) para entender a
intenção do comando, executa a ação correspondente (registrar ou consultar
transações) e devolve a resposta final também em áudio. Também é possível
interagir diretamente pelos endpoints REST, sem passar pelo fluxo de voz.

### Qual melhoria eu implementei

Adicionei um **novo tipo de consulta financeira**: um resumo de gastos
(`SummarizeTransactionsUseCase`), que retorna o total geral gasto e o total
gasto por categoria. A melhoria segue a mesma arquitetura em camadas do
projeto original:

- `domain/TransactionRepository`: novo método `findAll()`.
- `infrastructure/persistence/repository/JpaTransactionRepository`:
  implementação do `findAll()` usando o `TransactionEntityRepository`.
- `application/SummarizeTransactionsUseCase`: novo caso de uso, exposto tanto
  como `@Tool` (para o `ChatClient` usar via comando de voz, ex.: "quanto eu
  gastei no total?") quanto como serviço comum.
- `application/output/CategorySummaryOutput` e `SpendingSummaryOutput`: novos
  DTOs de saída do caso de uso.
- `infrastructure/http/response/CategorySummaryResponse` e
  `SpendingSummaryResponse`: DTOs de resposta HTTP.
- `infrastructure/http/TransactionController`: novo endpoint
  `GET /transactions/summary` e registro da nova tool no `ChatClient`.

Os valores retornados seguem a mesma representação já usada em
`TransactionOutput`, para manter consistência com o restante da API.

### Tecnologias usadas

- Java 25 + Spring Boot
- Spring AI (ChatClient, Tool Calling, Transcription API, Speech API)
- Spring Data JPA + MySQL (via Docker Compose)
- JUnit 5

### Como executar

```bash
export OPENAI_API_KEY="sua_chave_aqui"
./gradlew bootRun
```

Novo endpoint de consulta:

```
GET /transactions/summary
```

Resposta de exemplo:

```json
{
  "total": 10000.0,
  "count": 3,
  "byCategory": [
    { "category": "GROCERIES", "total": 7000.0, "count": 2 },
    { "category": "PHARMA", "total": 3000.0, "count": 1 }
  ]
}
```

### Como testar o fluxo principal

O fluxo de voz (transcrição + IA + síntese de fala) depende de uma
`OPENAI_API_KEY` válida, então não pude executá-lo neste ambiente. Por isso,
priorizei testar a lógica pura do novo caso de uso, que não depende de
nenhuma credencial externa:

```bash
./gradlew test --tests "dio.budgeting.application.SummarizeTransactionsUseCaseTest"
```

O teste usa um repositório em memória (fake) e cobre dois cenários: resumo
vazio (sem transações) e soma correta do total geral e por categoria. Também
validei a lógica de agrupamento e soma isoladamente, com um pequeno programa
Java independente, antes de integrá-la ao caso de uso real.

Com a `OPENAI_API_KEY` configurada, o fluxo completo pode ser testado
enviando um áudio para `POST /transactions/ai` perguntando, por exemplo,
"quanto eu já gastei no total?" — o modelo deve escolher a tool
`summarize-transactions` para responder.

### O que eu aprendi

Entendi melhor como o Tool Calling do Spring AI conecta um modelo de
linguagem a casos de uso reais da aplicação, sem misturar a lógica de IA com
as regras de negócio: o `@Tool` fica no caso de uso, não no controller. Também
reforcei a importância de manter a arquitetura em camadas (domain →
application → infrastructure) mesmo ao adicionar uma funcionalidade nova,
para que ela fique disponível tanto via REST quanto via comando de voz sem
duplicar código.
