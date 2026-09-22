# Privacidade, retenção e acesso a placas

## Finalidade

O sistema trata a placa exclusivamente para identificar uma estadia, controlar a ocupação e calcular sua cobrança. Não use os dados para perfilamento, marketing ou outra finalidade incompatível com a operação do estacionamento.

## Minimização e retenção

- A aplicação armazena somente placa, horários, estado e valor da estadia.
- Imagens, dados biométricos e dados de reconhecimento não são coletados nem armazenados por padrão.
- Em produção, elimine ou anonimize estadias finalizadas após o prazo operacional e fiscal definido pelo responsável pelo estacionamento. O prazo deve ser documentado antes da publicação e revisto quando a legislação aplicável mudar.
- Backups seguem o mesmo prazo de retenção e devem ser protegidos com acesso restrito.

## Acesso

As operações de balcão (entrada, saída, ocupação e estadias ativas) são separadas da consulta ao histórico completo. O endpoint `GET /api/v1/stays` exige o cabeçalho `X-Admin-Token` quando `PARKING_ADMIN_TOKEN` estiver configurado. Mantenha esse segredo fora do repositório, entregue-o apenas a administradores e altere-o quando houver troca de equipe.

Sem `PARKING_ADMIN_TOKEN`, a consulta histórica é mantida aberta apenas para desenvolvimento local. A aplicação registra no log de auditoria a consulta histórica e os registros de entrada e saída. Em produção, encaminhe esses logs para um destino protegido e defina quem os consulta.

## Responsabilidades antes de publicar

- Definir o controlador dos dados, canal de atendimento ao titular e prazo de retenção aplicável.
- Configurar `PARKING_ADMIN_TOKEN` como segredo do ambiente de deploy.
- Restringir o acesso de rede ao banco e aos logs.
- Revisar permissões de quem administra o painel e remover acessos que não forem mais necessários.
- Caso seja integrada uma câmera, manter imagens desabilitadas por padrão e implementar uma revisão específica de necessidade, base legal e retenção antes de ativá-la.
