# Estacionamento API

API REST em Java para controlar entradas e saídas de veículos, ocupação e cobrança por permanência. O horário é registrado pelo servidor, preparando o sistema para uma futura integração com câmeras de reconhecimento de placas.

## Requisitos

- Java 21 ou superior
- PostgreSQL 15 ou superior

O Maven Wrapper está incluído; não é necessário instalar Maven globalmente.

Crie o banco e o usuário localmente:

```sql
CREATE USER estacionamento WITH PASSWORD 'estacionamento';
CREATE DATABASE estacionamento OWNER estacionamento;
```

As configurações disponíveis estão em `.env.example`. Os valores padrão de desenvolvimento usam `localhost:5432`, usuário `estacionamento` e senha `estacionamento`.

## Executar

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A especificação OpenAPI estará disponível em `http://localhost:8080/openapi.yaml`.

Exemplo de entrada:

```bash
curl -X POST http://localhost:8080/api/v1/stays/entries \
  -H "Content-Type: application/json" \
  -d '{"plate":"ABC1D23"}'
```

Exemplo de saída:

```bash
curl -X POST http://localhost:8080/api/v1/stays/exits \
  -H "Content-Type: application/json" \
  -d '{"plate":"ABC1D23"}'
```

## Executar a interface de terminal

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=cli
```

A CLI utiliza o mesmo banco e os mesmos casos de uso da API.

## Testes

```bash
./mvnw test
```

Os testes unitários usam relógio controlado e os testes de persistência usam H2 em modo compatível com PostgreSQL.

## Regra de cobrança

- A tarifa inicial cobre a primeira hora.
- Cada hora adicional iniciada cobra o valor por hora configurado.
- Um período opcional de tolerância pode ser configurado em minutos.
- Valores monetários usam `BigDecimal`.

## Estrutura

```text
domain          regras e entidades do estacionamento
application     casos de uso e portas de persistência
infrastructure  configuração, JPA e banco de dados
interfaces      API REST e interface de terminal
```
