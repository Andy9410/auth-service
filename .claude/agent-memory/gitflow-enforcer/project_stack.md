---
name: Stack de producción y repositorios del sistema
description: Repositorios de microservicios del sistema, stack de despliegue y relaciones entre servicios
type: project
---

El sistema está compuesto por al menos dos microservicios relacionados:

- **auth-service** (`/home/andy/desarrollo/auth-service`, repo: `Andy9410/auth-service`)
  - Spring Boot con JWT
  - Desplegado en Fly.io con Neon (PostgreSQL serverless)
  - Estrategia gitflow-lite: `main`, `development`, ramas `feature/`, `fix/`, `refactor/`

- **chat-service** (`/home/andy/desarrollo/chat-service`, repo: `Andy9410/iachat`)
  - Spring Boot con LLM integration (Groq via SDK de OpenAI para Java)
  - Comparte el `JWT_SECRET` con auth-service para validar tokens sin llamadas HTTP
  - Desplegado en Fly.io (rama `deploy-fly` existe en el repo)
  - Misma estrategia gitflow-lite; rama `development` existe en remoto

**Por qué:** Los servicios están acoplados vía JWT: chat-service valida tokens emitidos por auth-service usando el mismo secret compartido.

**Cómo aplicar:** Al sugerir cambios en auth que afecten el JWT (algoritmo, claims, expiración), considerar el impacto en chat-service. Al crear PRs de chat-service, el repo remoto es `Andy9410/iachat`, no `Andy9410/chat-service`.
