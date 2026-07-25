# java-task-api

[![CI](https://github.com/bruno1186/java-task-api/actions/workflows/ci.yml/badge.svg)](https://github.com/bruno1186/java-task-api/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-orange)
![Maven](https://img.shields.io/badge/build-Maven-blue)

API de gerenciamento de tarefas escrita em **Java puro**, sem frameworks web. O servidor HTTP usa o `com.sun.net.httpserver` do proprio JDK, a serializacao JSON e feita a mao e o dominio e coberto por testes JUnit 5. O objetivo e demonstrar arquitetura limpa e codigo testavel sem dependencias de runtime.

## Arquitetura

```
src/main/java/com/bruno/taskapi
├── Main.java                     # ponto de entrada (le a porta de PORT)
├── domain/
│   ├── Task.java                 # entidade imutavel do dominio
│   ├── TaskService.java          # regras de negocio + store thread-safe
│   └── TaskNotFoundException.java
└── http/
    ├── TaskHttpServer.java       # servidor HTTP (JDK httpserver)
    └── Json.java                 # serializacao JSON sem dependencias
```

- **Sem framework:** apenas a biblioteca padrao do Java.
- **Thread-safe:** `ConcurrentHashMap` + `AtomicLong` para geracao de id.
- **Imutabilidade:** `Task` retorna nova instancia ao mudar de status.

## Pre-requisitos

- Java 17+
- Maven 3.9+

## Como rodar

```bash
# build + testes
mvn verify

# executar (porta 8080 por padrao; use PORT para trocar)
mvn -q -DskipTests package
java -jar target/java-task-api.jar
# ou: PORT=9090 java -jar target/java-task-api.jar
```

## Endpoints

| Metodo | Rota          | Descricao                         |
| ------ | ------------- | --------------------------------- |
| GET    | `/health`     | Health check                      |
| GET    | `/tasks`      | Lista todas as tarefas            |
| POST   | `/tasks`      | Cria uma tarefa (`{"title":"…"}`) |
| GET    | `/tasks/{id}` | Busca uma tarefa por id           |
| POST   | `/tasks/{id}` | Marca a tarefa como concluida     |
| DELETE | `/tasks/{id}` | Remove a tarefa                   |

## Exemplos

```bash
# criar
curl -X POST localhost:8080/tasks -d '{"title":"escrever documentacao"}'
# {"id":1,"title":"escrever documentacao","status":"PENDING","createdAt":"…"}

# listar
curl localhost:8080/tasks

# concluir
curl -X POST localhost:8080/tasks/1

# remover
curl -X DELETE localhost:8080/tasks/1
```

## Testes

Cobertura de dominio (`TaskServiceTest`) e integracao HTTP end-to-end (`TaskHttpServerTest`, subindo o servidor em porta efemera). O pipeline de CI executa `mvn verify` a cada push e pull request.

## Licenca

MIT
