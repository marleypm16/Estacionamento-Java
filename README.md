# Estacionamento API

Sistema para a recepção de um estacionamento registrar entradas e saídas, acompanhar vagas e calcular a cobrança pelo tempo de permanência. A API registra os horários no servidor, preservando um núcleo de negócio que também pode ser usado pela CLI e pelo painel web.

## Recursos

- Registro de entrada e saída por placa, com normalização e validação.
- Controle de lotação e consulta de veículos ativos.
- Cobrança configurável com `BigDecimal`, tolerância e horas adicionais.
- Histórico persistido com migrations Flyway.
- Painel React para a operação diária.

## Tecnologias e arquitetura

- Java 21, Spring Boot, Spring Web, Validation e Data JPA.
- PostgreSQL em execução normal; H2 nos testes.
- Flyway para versionar o esquema.
- React + Vite para o painel, que consome somente a API REST.

```text
interfaces       REST, CLI e painel web
application      casos de uso e portas
domain           regras de estadia, placa e tarifa
infrastructure   configuração, JPA e banco
```

As regras de domínio não dependem de HTTP, terminal ou persistência. Isso permite testá-las isoladamente e trocar as interfaces sem duplicar regras de cobrança.

## Requisitos

- Java 21 ou superior.
- PostgreSQL 15 ou superior para executar a API localmente.
- Node.js 20.19+ ou 22.12+ para o painel web.

O Maven Wrapper está incluído; não é necessário instalar Maven globalmente.

Crie o banco e o usuário localmente:

```sql
CREATE USER estacionamento WITH PASSWORD 'estacionamento';
CREATE DATABASE estacionamento OWNER estacionamento;
```

Copie os valores de `.env.example` para as variáveis de ambiente do seu terminal. Em desenvolvimento, a API usa por padrão `localhost:5432`, usuário `estacionamento` e senha `estacionamento`.

## Executar a API

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A especificação OpenAPI fica em `http://localhost:8080/openapi.yaml`.

Exemplo de entrada:

```bash
curl -X POST http://localhost:8080/api/v1/stays/entries \
  -H "Content-Type: application/json" \
  -d '{"plate":"ABC1D23"}'
```

## Demonstração com Docker

Para iniciar a API, PostgreSQL, migrations e duas estadias de demonstração:

```bash
docker compose up --build
```

O painel continua sendo iniciado em `frontend` com `npm run dev`. Para consultar histórico,
relatórios e pendências de leitura na demonstração, use a chave `demo-admin-token` (ou defina
`PARKING_ADMIN_TOKEN` em um arquivo `.env`). Para remover somente os dados de demonstração:

```bash
docker compose down -v
```

## Executar o painel React

Com a API em execução em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. Para apontar para outra API, crie `frontend/.env.local` com `VITE_API_BASE_URL=http://servidor:porta`.

Para gerar a versão de produção:

```bash
cd frontend
npm run build
```

## Interface de terminal

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=cli
```

A CLI usa os mesmos casos de uso e banco da API.

## Testes e qualidade

```bash
./mvnw test
cd frontend && npm run build
```

Os testes unitários usam relógio controlado; os testes de persistência usam H2 em modo compatível com PostgreSQL. O workflow do GitHub Actions executa ambos os builds a cada push e pull request.

## Regra de cobrança

- A tarifa inicial cobre a primeira hora.
- Cada hora adicional iniciada cobra o valor por hora configurado.
- Um período opcional de tolerância pode ser configurado em minutos.
- Valores monetários usam `BigDecimal`.

## Relatórios e leituras de placa

- `GET /api/v1/reports/operations?from=2026-09-01&to=2026-09-30` retorna entradas, saídas,
  receita e tempo médio de permanência. Requer `X-Admin-Token` quando configurado.
- `POST /api/v1/plate-detections` recebe eventos de qualquer adaptador de câmera/OCR. Leituras
  abaixo de 90%, entradas duplicadas e saídas sem estadia ativa permanecem como pendências.
- `GET /api/v1/plate-detections/pending` e os endpoints de confirmação/dispensa permitem que o
  atendente trate cada exceção. A integração de OCR não conhece a regra de tarifa nem o banco.

## Privacidade e acesso a dados de placas

Placas são dados operacionais tratados exclusivamente para controlar a permanência e a cobrança. O projeto não coleta nem armazena imagens por padrão. Consulte a [política de privacidade e retenção](docs/PRIVACIDADE_E_RETENCAO.md) antes de publicar a aplicação: ela define retenção, controles de acesso, auditoria e a configuração do token administrativo.
