# DryStorm API

API REST para agendamento de visitas em loja de roupas dryfit

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat&logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-blue)

## Funcionalidades

- Catálogo de produtos por ID
- Agendamento com validação de horário e conflito
- Consulta de slots disponíveis por data
- E-mail de confirmação e cancelamento
- Área admin protegida com JWT

## Stack

Java 21 · Spring Boot 3.2 · Spring Security · JWT · Spring Data JPA · PostgreSQL · Docker Compose · Swagger

## Como rodar

```bash
git clone https://github.com/brunomartinsoliveira/drystorm-api.git
cd drystorm-api
docker-compose up --build
```

Swagger: http://localhost:8080/swagger-ui.html  
Admin: `admin@drystorm.com.br` / `Admin@123`

---

**Autor:** Bruno Martins —

## Licença

MIT.

---

**Autor:** Bruno Martins de Oliveira — projeto **DryStorm** (roupas dryfit).
