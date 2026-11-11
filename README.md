# Análise de excesso de velocidade

Aplicação web que recebe um arquivo CSV com leituras de rastreadores, identifica episódios de excesso de velocidade e gera um ranking dos veículos. O processamento acontece integralmente em memória, sem banco de dados.

## Tecnologias

- Java 21, Spring Boot 4 e Maven no backend.
- Angular 21, TypeScript e Vitest no frontend.
- Docker Compose para executar a aplicação completa.
- Nginx para servir o frontend e encaminhar chamadas de `/api` ao backend.

## Execução com Docker

### Pré-requisito

- Docker Desktop, com Docker Compose disponível.

Na raiz do repositório, execute:

```bash
docker compose up --build
```

Acesse a aplicação em [http://localhost:4200](http://localhost:4200). A API também fica acessível diretamente em `http://localhost:8080`.

Para encerrar:

```bash
docker compose down
```

## Execução local

### Pré-requisitos

- Java 21.
- Node.js 24 e npm 12.

Inicie o backend no Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Em Linux ou macOS:

```bash
cd backend
./mvnw spring-boot:run
```

Em outro terminal, inicie o frontend:

```bash
cd frontend
npm ci
npm start
```

O servidor de desenvolvimento do Angular encaminha `/api` para o backend em `http://localhost:8080`.

## Como usar

1. Abra `http://localhost:4200`.
2. Selecione um CSV com o cabeçalho `placa;dataHora;velocidadeKmh;limiteKmh`.
3. Clique em **Analisar arquivo**.
4. Consulte o resumo, ranking, episódios e linhas inválidas.
5. Use o seletor de gravidade para filtrar os episódios localmente.

O arquivo [`leituras.csv`](leituras.csv) pode ser usado para testar o fluxo completo.

## API

### `POST /api/analises`

Recebe `multipart/form-data` com o arquivo no campo `arquivo` e retorna:

- resumo de linhas lidas, linhas válidas e episódios;
- episódios de excesso de velocidade;
- ranking por pontuação;
- linhas inválidas com número e motivo.

Exemplo com cURL:

```bash
curl -X POST -F "arquivo=@leituras.csv" http://localhost:8080/api/analises
```

Arquivo vazio, ausente ou com cabeçalho diferente do esperado retorna HTTP 400 com uma mensagem objetiva.

## Testes e build

Backend, no Windows:

```powershell
cd backend
.\mvnw.cmd test
```

Backend, em Linux ou macOS:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

## Estrutura

```text
backend/                 API, regras de negócio e testes Java
frontend/                interface Angular e testes de componentes/serviço
compose.yaml             orquestração local dos dois serviços
leituras.csv             massa de dados de referência
DECISOES.md              decisões tomadas para casos não especificados
```

As principais decisões de implementação e os pontos que poderiam evoluir estão registrados em [`DECISOES.md`](DECISOES.md).
