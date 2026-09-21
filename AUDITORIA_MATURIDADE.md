# Auditoria de maturidade — Estacionamento Java

## 1. Resumo executivo

### Propósito

O projeto aparenta cadastrar, listar e retirar veículos de um estacionamento, calculando uma cobrança por tempo de permanência.

### Maturidade atual

O repositório está no estágio de **protótipo/exercício de console funcional**, ainda distante de um produto de gestão de estacionamento ou de um repositório de portfólio consolidado.

Há uma boa semente de domínio e um fluxo simples de CLI, mas faltam confiabilidade no fluxo principal, testes, documentação, build reprodutível e persistência. Hoje, ele comunica mais “exercício inicial em Java” do que “aplicação pronta para evolução”.

## 2. Entendimento do projeto

- Usuário provável: atendente de um estacionamento pequeno.
- Casos de uso atuais: cadastrar, listar, remover veículo e calcular preço.
- Entidades atuais: estacionamento, veículo representado apenas por `String` (placa) e preço.
- Fluxo: menu → leitura do terminal → `Estacionamento` → lista em memória.
- Stack: Java puro, `Scanner`, `BigDecimal`, `ArrayList` e arquivos de configuração do IntelliJ.
- Não há Maven/Gradle, banco de dados, API, testes, README ou automação.

## 3. Pontos positivos

- O escopo inicial é claro e pequeno: o menu em `src/Main.java` torna os casos de uso identificáveis.
- Usar `BigDecimal` para preço em `src/Estacionamento.java` é uma boa intenção para evitar erros comuns de ponto flutuante.
- O repositório possui Git configurado e a árvore estava limpa durante a auditoria.
- O `.gitignore` já evita artefatos de compilação comuns.

## 4. Arquitetura e código

A arquitetura atual possui duas classes no pacote padrão:

```text
Main
 ├─ mostra menu
 ├─ lê terminal
 └─ chama Estacionamento

Estacionamento
 ├─ lê terminal
 ├─ mostra mensagens
 ├─ mantém dados
 └─ calcula preço
```

Para o tamanho atual, duas classes não são um problema. O problema é que `Estacionamento` mistura regras de negócio, entrada do usuário, saída no terminal e armazenamento.

### Fluxo de entrada quebradiço

**Evidência:** `src/Main.java` e `src/Estacionamento.java` instanciam, cada um, um `Scanner` para `System.in`.

**Problema:** existem dois `Scanner` lendo o mesmo fluxo; além disso, `nextInt()` deixa a quebra de linha pendente. Assim, o primeiro cadastro pode receber uma placa vazia, e os comportamentos de pausa/leitura ficam inconsistentes.

**Impacto:** o principal fluxo do produto falha ou parece falhar para o usuário.

**Recomendação:** manter um único leitor de entrada no programa; a classe de domínio deve receber valores já validados, sem depender de `Scanner`.

**Prioridade:** P1.

### Cobrança não é mostrada e remoção não é validada

**Evidência:** `src/Estacionamento.java`, nas operações de remoção e de impressão do preço.

**Problema:** a saída usa `${precoFinal}`, que em Java é texto literal. Além disso, `remove()` pode retornar `false`, mas o programa informa sucesso e cobra mesmo assim.

**Impacto:** a operação financeira central pode gerar informação incorreta.

**Recomendação:** validar se o veículo existe antes de finalizar a saída e formatar o valor com `String.format` ou `NumberFormat` para BRL.

**Prioridade:** P1.

### Modelo de domínio insuficiente

**Evidência:** `src/Estacionamento.java` armazena veículos como `List<String>`.

**Problema:** não há data/hora de entrada, identificação da vaga, status, tipo de veículo ou regra de tarifa explícita.

**Impacto:** o sistema depende de o atendente informar manualmente o tempo, aceita placas duplicadas e não permite auditoria básica.

**Recomendação:** introduzir, sem exagero, `Veiculo`, `Estadia` (ou `Ticket`) e `CalculadoraDeTarifa`. A data/hora de entrada deve ser registrada pelo sistema.

**Prioridade:** P1.

### Validação e tratamento de erros ausentes

**Evidência:** `src/Main.java` lê a opção com `nextInt()` e `src/Estacionamento.java` converte o tempo com `Integer.parseInt()`.

Entradas não numéricas causam exceções; não há tratamento para opções inexistentes, horas negativas/zero, placa vazia ou formato inválido.

**Prioridade:** P1.

## 5. Dados e segurança

Não há banco, usuários, rede, credenciais ou dependências externas. Portanto, não há achado de segurança crítico ou alto comprovado.

Há, porém, riscos locais de integridade:

- Placa vazia ou duplicada pode ser cadastrada.
- Veículo inexistente pode ser “removido”.
- Permanência negativa pode resultar em cobrança negativa.
- Todos os dados são perdidos ao encerrar o programa.

Persistência não é obrigatória para um exercício, mas passa a ser essencial se a proposta for um sistema de estacionamento real.

## 6. Testes, experiência de desenvolvimento e operações

| Área | Estado atual | Evidência |
| --- | --- | --- |
| Testes | Ausentes | Não há diretório ou framework de testes |
| Build | Manual e não documentado | Não há `pom.xml` ou `build.gradle` |
| Versão Java | Não é reproduzível | A IDE aponta JDK 24 em `.idea/misc.xml` |
| Documentação | Ausente | Não existe `README.md` |
| CI | Ausente | Não existe workflow GitHub Actions |
| Persistência/migrations | Ausentes | Dados ficam em `ArrayList` |
| Observabilidade | Ausente | Saídas apenas com `System.out.println` |
| Higiene Git | Parcial | `.idea` e `Estacionamento.iml` estão versionados |

Outra pessoa não consegue clonar e executar o projeto com segurança sem inferir a versão Java, o comando de compilação e o comportamento esperado.

## 7. Completude do produto

### Essenciais

- Corrigir cadastro, saída e cálculo de tarifa.
- Validar placa, opção do menu e duração.
- Registrar entrada automaticamente e calcular saída a partir de horário real.
- Persistir estadias ativas e histórico.

### Importantes

- Capacidade total, vagas disponíveis e ocupação atual.
- Tabela de tarifas configurável: valor inicial, hora adicional, tolerância e diária.
- Recibo de saída com placa, horários, duração e valor.
- Pesquisa de veículo por placa e histórico de estadias.
- Perfis simples: atendente e administrador, caso haja uso por mais de uma pessoa.

### Diferenciadores

- API REST com Spring Boot, mantendo o núcleo de domínio independente.
- Interface web simples para recepção e painel de ocupação.
- Relatórios diário/mensal de receita e ocupação.
- Docker Compose com PostgreSQL.
- Deploy demonstrável, com dados de exemplo e screenshots.

## 8. Análise de portfólio

Hoje o projeto parece um exercício acadêmico principalmente porque:

- Só há um commit.
- Não há descrição, README, instrução de execução ou demonstração.
- Comentários padrão do IntelliJ continuam no código em `src/Main.java`.
- Arquivos específicos da IDE foram enviados ao Git.
- Os fluxos de negócio não estão protegidos por testes.

Para um engenheiro sênior, o potencial de discussão é bom se o projeto evoluir em torno de decisões concretas: modelagem de tarifa, arredondamento monetário, persistência de estadias, concorrência de vagas e estratégia de testes. Microserviços, Kafka, Kubernetes e múltiplos bancos não agregariam valor aqui.

## 9. Evolução recomendada

| Fase | Resultado |
| --- | --- |
| Fundação | Maven, Java LTS definido, pacotes, README, `.gitignore` corrigido e JUnit |
| Domínio | `Veiculo`, `Estadia`, tarifa e validações independentes do terminal |
| Confiabilidade | Fluxo de entrada/saída correto, preço formatado, testes de regras |
| Persistência | H2 para desenvolvimento e PostgreSQL para produção, com Flyway |
| Interface | CLI melhorada ou API REST; interface web somente se for objetivo do portfólio |
| Automação | GitHub Actions para build e testes |
| Portfólio | Diagrama simples, screenshots, dados de demonstração e deploy |

## 10. Cinco melhorias de maior impacto

1. Corrigir o fluxo de leitura, remoção e apresentação do preço.
2. Criar um modelo de `Estadia` com entrada, saída e placa validada.
3. Adotar Maven e testes automatizados das regras de tarifa.
4. Persistir as operações e o histórico em banco de dados.
5. Criar um README profissional e CI no GitHub Actions.

## 11. Definição de pronto para portfólio

O projeto pode ser considerado um repositório profissional consolidado quando:

- O fluxo de entrada, listagem e saída estiver correto, validado e coberto por testes.
- A versão Java e os comandos de build/teste estiverem definidos em Maven e no README.
- Houver separação simples entre interface de console/API, casos de uso e regras de domínio.
- As estadias e o histórico persistirem entre execuções, com migrations versionadas.
- O repositório tiver CI que execute build e testes.
- O README apresentar o problema, arquitetura, decisões, execução e demonstração do sistema.

## 12. Backlog acionável

### P1 — Alta prioridade

#### P1-01 — Corrigir o fluxo interativo e as validações

**Por quê:** o cadastro e a retirada podem funcionar incorretamente devido aos dois `Scanner`, ao uso de `nextInt()` e à falta de validação.

**Escopo:** `Main`, interface de console e regras de entrada.

**Concluído quando:** há um único leitor de entrada; opções inválidas não encerram o programa; placa vazia/inválida e duração não positiva são recusadas; veículo inexistente não gera cobrança.

#### P1-02 — Corrigir cálculo e apresentação da cobrança

**Por quê:** o valor calculado não aparece na mensagem e a retirada informa sucesso sem confirmar a remoção.

**Escopo:** `Estacionamento` e futuros testes de tarifa.

**Concluído quando:** uma saída válida exibe valor em BRL, uma placa inexistente gera mensagem adequada e nenhum valor negativo é calculado.

#### P1-03 — Modelar veículo, estadia e tarifa

**Por quê:** `List<String>` não representa as regras necessárias ao produto.

**Escopo:** novas classes de domínio e adaptação do fluxo de cadastro/retirada.

**Concluído quando:** uma estadia registra placa normalizada, horário de entrada, horário de saída, estado ativo/finalizado e valor calculado por uma regra dedicada.

#### P1-04 — Tornar build e testes reproduzíveis

**Por quê:** não há versão Java declarada, gerenciador de build ou testes.

**Escopo:** Maven, JUnit 5, estrutura `src/main/java` e `src/test/java`.

**Concluído quando:** `mvn test` executa testes de cadastro, duplicidade, retirada inexistente e cálculo de tarifa em qualquer máquina com a versão Java documentada.

#### P1-05 — Persistir estadias e histórico

**Por quê:** todos os dados somem ao encerrar a aplicação.

**Escopo:** repositório de dados, H2 para desenvolvimento, PostgreSQL opcional para ambiente publicado e migrations Flyway.

**Concluído quando:** estadias ativas e finalizadas permanecem após reiniciar a aplicação, com migrações versionadas.

#### P1-06 — Disponibilizar uma API REST documentada

**Por quê:** a API REST deve ser a interface principal da versão profissional do sistema; ela permite integrar um painel administrativo e, posteriormente, uma câmera sem acoplar as regras de negócio a essas interfaces.

**Escopo:** Spring Boot, controllers, DTOs, tratamento de erros, OpenAPI e os casos de uso de entrada, saída, listagem de estadias ativas, histórico e ocupação.

**Concluído quando:** a API possui endpoints versionados e documentados para registrar entrada e saída, consultar estadias e ocupação; retorna códigos HTTP coerentes; valida seus dados de entrada; e mantém as regras de tarifa fora dos controllers.

### P2 — Melhorias de engenharia

#### P2-01 — Separar console, casos de uso e domínio

**Concluído quando:** classes de domínio não importam `Scanner` nem usam `System.out`, permitindo testes unitários puros.

#### P2-02 — Criar README e remover artefatos de IDE do versionamento

**Concluído quando:** o README explica propósito, requisitos, execução, testes e decisões; `.idea` e `.iml` não fazem parte do repositório.

#### P2-03 — Adicionar CI mínimo

**Concluído quando:** todo push ou pull request executa build e testes no GitHub Actions.

#### P2-04 — Criar painel operacional sobre a API

**Por quê:** a operação diária fica mais clara quando o atendente enxerga vagas disponíveis, veículos ativos e valores recebidos, sem acesso direto ao banco.

**Escopo:** interface web simples ou cliente administrativo; consumo exclusivo da API REST.

**Concluído quando:** o atendente consegue registrar entrada e saída manualmente, consultar estadias ativas e visualizar ocupação e receita diária.

#### P2-05 — Definir privacidade, retenção e acesso aos dados de placas

**Por quê:** placas podem identificar pessoas no contexto do estacionamento; a futura integração por câmera amplia a coleta de dados.

**Escopo:** política de retenção, perfis de acesso, trilha de auditoria, documentação de finalidade e configuração para não armazenar imagens por padrão.

**Concluído quando:** o sistema documenta finalidade e prazo de retenção; restringe consultas administrativas; registra ações sensíveis; e elimina imagens ou dados de reconhecimento não necessários.

### P3 — Diferenciadores de portfólio

#### P3-01 — Empacotar a aplicação para demonstração

**Escopo:** Dockerfile, Docker Compose, banco PostgreSQL, variáveis de ambiente de exemplo e dados de demonstração.

**Concluído quando:** outra pessoa inicia API e banco com um comando documentado, executa migrations e testa os fluxos com dados de exemplo.

#### P3-02 — Criar relatórios operacionais

**Escopo:** consultas e endpoints de ocupação, receita por período, quantidade de entradas/saídas e tempo médio de permanência.

**Concluído quando:** o painel mostra métricas diárias e mensais derivadas de estadias finalizadas, com filtros de período e testes para os cálculos agregados.

#### P3-03 — Preparar a detecção de placas como integração substituível

**Por quê:** o reconhecimento óptico deve ser uma integração, não uma regra acoplada à API ou à câmera específica.

**Escopo:** porta `LeitorDePlaca` (ou equivalente), modelo de evento de detecção, normalização de placa, nível de confiança e fluxo de confirmação manual.

**Concluído quando:** a API recebe uma detecção de placa por um adaptador; placas são normalizadas antes da busca; baixa confiança, placa duplicada e ausência de estadia ativa geram uma pendência para confirmação do atendente; e todos esses cenários possuem testes.

#### P3-04 — Integrar câmera e reconhecimento automático de placas

**Por quê:** esta é a implementação final que transforma a entrada e a saída em operações automáticas, usando os horários confiáveis registrados pelo servidor.

**Escopo:** câmera compatível, adaptador para serviço de OCR/ALPR, endpoint ou worker de ingestão de detecções, monitoramento de falhas e atualização do painel operacional.

**Concluído quando:** uma placa detectada na entrada cria uma estadia com `entradaEm` registrado pelo servidor; a mesma placa na saída encerra a estadia com `saidaEm`, calcula a tarifa exata e atualiza a ocupação; leituras de baixa confiança requerem confirmação humana; eventos repetidos não criam cobranças duplicadas; e imagens não são armazenadas além do prazo e da finalidade definidos em P2-05.
