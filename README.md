# DryStorm API

Backend REST da **DryStorm** — loja de roupas esportivas **dryfit**. Pensado para integrar com a landing page estática [**DryStormLandpage**](https://github.com/brunomartinsoliveira/DryStormLandpage).

<img width="1376" height="670" alt="Generated_image" src="https://github.com/user-attachments/assets/cca80035-afe7-466f-8304-c4cb5d380557" />


![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat&logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## O que é isso?

Drystorm é um projeto full stack com backend em Java, contemplando API REST, autenticação de usuários (login) e fluxo de pagamentos. No frontend, foi desenvolvida uma landing page responsiva com HTML5 e CSS3, priorizando usabilidade e performance. O projeto reforça minha experiência em integração entre camadas, boas práticas de desenvolvimento e construção de aplicações web com foco em qualidade e escalabilidade. Em uma frase: **expõe o catálogo de produtos dryfit, permite que clientes agendem visita/prova na loja e envia e-mails transacionais**, enquanto o time usa rotas protegidas por **JWT** para administrar agenda e catálogo.

| Área | O que oferece |
|------|----------------|
| **Catálogo** | Lista itens ativos, detalhe por ID e visão **agrupada por categoria** (Camisetas, sexo e cores). |
| **Agendamento público** | Criação de reserva com validação de dia útil, horário de funcionamento e **conflito de horários**; consulta de **slots livres** por data e produto. |
| **Confirmação** | Link com token no e-mail → cliente confirma → status passa de `PENDING` para `CONFIRMED`. |
| **E-mail** | Templates HTML (confirmação, lembrete D-1, cancelamento), envio **assíncrono**. |
| **Admin** | Login JWT; CRUD lógico de itens do catálogo; listagem, status, reagendamento e histórico de agendamentos. |

**Stack:** Java 21, Spring Boot 3.2 (Web, JPA, Security, Mail, Validation), PostgreSQL, Flyway, Docker Compose, OpenAPI (Swagger).

---

## Repositórios relacionados:

| Projeto | Descrição |
|---------|-----------|
| [**DryStormLandpage**](https://github.com/brunomartinsoliveira/DryStormLandpage) | Landing HTML/CSS (GitHub Pages). Configure `FRONTEND_URL` na API para essa URL nos links dos e-mails. |
| **drystorm-api** | Backend e regras de negócio. |

---

## Arquitetura do projeto: 

```
src/main/java/com/drystorm/api/
├── config/       # Security (JWT), OpenAPI, CORS
├── controller/   # REST: Auth, Services, Appointments
├── dto/          # Request / Response com validação
├── entity/       # Service, Appointment, User
├── exception/    # Handlers globais
├── repository/   # Spring Data JPA
├── service/      # Regras de negócio, e-mail
└── util/         # JWT, geração de token de confirmação
```

---

## Como rodar?

### Pré-requisitos:

- Java 21+
- Docker e Docker Compose (PostgreSQL)
- Maven (`mvn`)

### Configuração:

```bash
git clone https://github.com/brunomartinsoliveira/drystorm-api.git
cd drystorm-api
cp .env.example .env
# Ajuste DB_*, MAIL_*, JWT_SECRET, FRONTEND_URL no .env
```

### Banco e aplicação:

```bash
docker compose up postgres -d
mvn spring-boot:run
```

Ou suba API + banco com `docker compose up -d` (conforme seu `docker-compose.yml`).

### URLs úteis:

| Serviço | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PgAdmin (se usar profile `tools`) | http://localhost:5050 |

---

## Autenticação:

Rotas administrativas exigem **Bearer JWT** obtido em:

`POST /api/v1/auth/login` com `email` e `password`.

Usuário seed (troque a senha em produção): `admin@drystorm.com.br` / `Admin@123`.

---

## Endpoints:

### Públicos:

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/v1/services` | Lista itens ativos |
| GET | `/api/v1/services/grouped` | Catálogo agrupado por categoria |
| GET | `/api/v1/services/{id}` | Detalhe |
| GET | `/api/v1/appointments/available-slots` | `?date=&serviceId=` |
| POST | `/api/v1/appointments` | Novo agendamento |
| GET | `/api/v1/appointments/confirm/{token}` | Confirmação via link do e-mail |

### Protegidos:

Agendamentos: busca por id, por data, por período, por e-mail do cliente; atualização de status; reagendamento.

Catálogo: criar, atualizar, desativar e reativar itens (papéis conforme regras do projeto).

Documentação interativa completa no **Swagger**.

---

## Fluxo de agendamento:

```
Landing → GET /services/grouped → GET /available-slots
    → POST /appointments (PENDING)
    → e-mail com link → GET /confirm/{token} (CONFIRMED)
    → admin: IN_PROGRESS → COMPLETED (ou CANCELLED / NO_SHOW)
```

---

## Status dos agendamentos:

`PENDING` → `CONFIRMED` → `IN_PROGRESS` → `COMPLETED`  
Cancelamento: `CANCELLED`. Falta: `NO_SHOW`. Transições inválidas retornam erro de negócio.

---

## Integração com a landing page:

```javascript
const API = 'https://sua-api.com';

const { data } = await (await fetch(`${API}/api/v1/services/grouped`)).json();

await fetch(`${API}/api/v1/appointments`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    clientName: 'João Silva',
    clientEmail: 'joao@email.com',
    clientPhone: '(83) 99999-9999',
    clientProductNote: 'Camiseta Dryfit Training, tamanho M, cor preta',
    serviceId: 1,
    appointmentDate: '2026-04-15',
    appointmentTime: '09:00'
  })
});
```

`clientProductNote` = produto, tamanho ou cor de interesse (campo obrigatório).

---

## E-mails:

1. Ative 2FA na conta Google.  
2. Crie uma **senha de app** e use em `MAIL_PASSWORD` no `.env`.  
3. `MAIL_USERNAME` = e-mail remetente.

---

## Rota Migratória:

| Versão | Conteúdo |
|--------|----------|
| V1 | Schema inicial (`services`, `users`, `appointments`) |
| V2 | Usuário admin + seed legado |
| V3 | Domínio dryfit: coluna `client_product_note`, catálogo de roupas, limpeza de dados antigos |

> A **V3** apaga agendamentos e serviços anteriores ao aplicar. Faça backup se já houver dados importantes.

---

## Variáveis de ambiente:

| Variável | Descrição |
|----------|-----------|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | PostgreSQL |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Envio de e-mail |
| `JWT_SECRET` | Segredo JWT (mín. 32 caracteres) |
| `FRONTEND_URL` | URL pública da landing (links nos e-mails) |

---

## Testes:

```bash
mvn test
mvn test jacoco:report   # cobertura em target/site/jacoco/index.html
```

---

## Licença:

MIT.

---

https://www.linkedin.com/in/brunooliveiradev/

**Autor:** Bruno Martins de Oliveira — projeto **DryStorm** (roupas dryfit).
