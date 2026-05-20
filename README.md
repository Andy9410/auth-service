# Auth Service

Microservicio de autenticación moderno para una plataforma de chat con IA estilo ChatGPT/Claude.

Este servicio es responsable exclusivamente de:

* registro de usuarios
* login
* JWT authentication
* refresh tokens
* logout
* validación de sesión
* seguridad

---

# 🚀 Tecnologías

* Spring Boot
* Spring Security
* JWT Authentication
* PostgreSQL
* Flyway
* Docker

---

# ✨ Funcionalidades

## Autenticación

* Registro de usuarios
* Login con JWT
* Refresh Tokens
* Logout
* Persistencia de sesión
* Renovación automática de tokens

## Seguridad

* JWT Stateless Authentication
* BCrypt Password Encoding
* Spring Security
* CORS configurable
* Validación global de requests
* Manejo centralizado de errores
* Refresh Token Rotation

---

# 📁 Estructura del Proyecto

```text id="8wz4ke"
auth-service/
│
├── src/main/java/com/example/auth/
│
├── config/
├── controller/
├── dto/
├── entity/
├── exception/
├── mapper/
├── repository/
├── security/
├── service/
├── util/
│
├── src/main/resources/
│   ├── db/migration/
│   ├── application.yml
│   └── application-dev.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

# 🔐 Authentication Flow

```text id="g7n1vx"
Usuario
   ↓
POST /auth/login
   ↓
Spring Security valida credenciales
   ↓
Generación JWT + Refresh Token
   ↓
Respuesta autenticada
   ↓
Frontend almacena tokens
   ↓
Requests autenticadas usando Bearer Token
```

---

# 📡 Endpoints

## Register

```http id="r0v5xq"
POST /auth/register
```

## Login

```http id="m8y2jd"
POST /auth/login
```

## Refresh Token

```http id="u9k3fa"
POST /auth/refresh
```

## Logout

```http id="n4b7tr"
POST /auth/logout
```

## Current User

```http id="z1x6ce"
GET /auth/me
```

---

# 🗄️ Base de Datos

## users

| Campo      | Tipo      |
| ---------- | --------- |
| id         | UUID      |
| name       | VARCHAR   |
| email      | VARCHAR   |
| password   | VARCHAR   |
| created_at | TIMESTAMP |

---

## refresh_tokens

| Campo      | Tipo      |
| ---------- | --------- |
| id         | UUID      |
| user_id    | UUID      |
| token      | TEXT      |
| expires_at | TIMESTAMP |
| revoked    | BOOLEAN   |

---

## roles

| Campo | Tipo    |
| ----- | ------- |
| id    | UUID    |
| name  | VARCHAR |

---

# 🔒 Características de Seguridad

* JWT Authentication Filter
* Stateless Sessions
* BCrypt Password Encryption
* Protected Endpoints
* Request Validation
* Global Exception Handling
* Secure Token Generation
* Refresh Token Rotation

---

# ⚙️ Variables de Entorno

```env id="t6f8ap"
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

JWT_SECRET=
JWT_EXPIRATION=
JWT_REFRESH_EXPIRATION=
```

---

# 🐳 Docker

## Ejecutar proyecto

```bash id="q3p9vn"
docker-compose up --build
```

---

# 💻 Desarrollo local

Levantá solo la base de datos con Docker y corré el Spring Boot desde el IDE o CLI con el perfil `local`:

```bash
# 1. Iniciar Postgres en el puerto 5434
docker compose up postgres -d

# 2. Correr el servicio con perfil local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

El perfil `local` usa `localhost:5434/auth_db` con usuario/contraseña `postgres`.

---

# 🧱 Arquitectura

El proyecto sigue:

* Clean Architecture
* SOLID Principles
* separación controller/service/repository
* DTO pattern
* configuración desacoplada

---

# 📦 Componentes Principales

## Controllers

Responsables de exponer endpoints REST.

## Services

Contienen la lógica de negocio.

## Repositories

Acceso a datos mediante Spring Data JPA.

## Security

Configuración JWT y Spring Security.

## DTOs

Separación entre entidades y respuestas API.

---

# 🚀 Preparado para Producción

Preparado para:

* Kubernetes
* CI/CD
* escalabilidad horizontal
* observabilidad
* logging centralizado
* API Gateway futuro

---

# 📌 Próximas Mejoras

* OAuth2 Login
* Google Login
* GitHub Login
* MFA / 2FA
* Verificación Email
* Recuperación Password
* Redis Session Cache

---

# 🧑‍💻 Objetivo

Construir un microservicio de autenticación moderno, seguro y escalable para aplicaciones SaaS y plataformas IA.

