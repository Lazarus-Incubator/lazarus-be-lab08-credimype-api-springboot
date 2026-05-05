# creditflow-mype-api

`creditflow-mype-api` es una API REST empresarial construida con Spring Boot para la originacion, evaluacion, aprobacion y desembolso de creditos MYPE en una entidad financiera peruana multi-tenant.

## Stack

- Java 21
- Spring Boot 3.5.14
- Maven Wrapper
- Spring Web
- Spring Data JPA
- Spring Security con JWT simple implementado dentro del proyecto
- Bean Validation
- PostgreSQL
- Flyway
- OpenAPI / Swagger UI
- JUnit 5
- H2 en perfil `test` para integracion automatizada

## Arquitectura

El codigo esta organizado como monolito modular con DDD y CQRS:

- `shared`: seguridad, configuracion, value objects, errores comunes
- `identity`: autenticacion y usuarios internos
- `institutions`: instituciones, sucursales y politicas tenant
- `borrowers`: clientes MYPE
- `creditproducts`: productos crediticios
- `applications`: agregado principal y workflow
- `risk`: evaluacion de riesgo
- `approvals`: decisiones de aprobacion/rechazo
- `disbursements`: ordenes de desembolso e idempotencia
- `audit`: auditoria de eventos relevantes
- `reports`: consultas agregadas operativas

## Requisitos

- JDK 21 o superior disponible en `JAVA_HOME`
- Docker Desktop o motor compatible para PostgreSQL local
- PowerShell en Windows

## Levantar PostgreSQL

```powershell
docker compose up -d
```

Base expuesta:

- host: `localhost`
- port: `5432`
- database: `creditflow`
- username: `creditflow`
- password: `creditflow`

Para detener:

```powershell
docker compose down
```

Para limpiar volumenes:

```powershell
docker compose down -v
```

## Ejecutar la aplicacion

Variables soportadas por `application.yml`:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_SECONDS`

Valores por defecto locales:

- `DB_URL=jdbc:postgresql://localhost:5432/creditflow`
- `DB_USERNAME=creditflow`
- `DB_PASSWORD=creditflow`
- `JWT_SECRET=change-me-creditflow-secret-key-change-me`
- `JWT_EXPIRATION_SECONDS=3600`

Ejecucion esperada:

```powershell
.\mvnw.cmd spring-boot:run
```

## Flyway

Migraciones incluidas:

- `V1__create_schema.sql`
- `V2__seed_reference_data.sql`
- `V3__seed_users_and_credit_cases.sql`

Las migraciones crean el esquema completo y siembran instituciones, sucursales, usuarios, borrowers, productos y casos de credito listos para probar.

## Swagger

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Usuarios semilla

Todas las cuentas usan la contrasena `password123`.

| Email | Rol | Institucion / ambito |
|---|---|---|
| `platform.admin@creditflow.pe` | `PLATFORM_ADMIN` | Plataforma completa |
| `admin.andina@creditflow.pe` | `INSTITUTION_ADMIN` | Caja Andina Peru |
| `oficial.lima@creditflow.pe` | `BRANCH_OFFICER` | Caja Andina Peru / Lima Centro |
| `analista.andina@creditflow.pe` | `CREDIT_ANALYST` | Caja Andina Peru |
| `riesgo.andina@creditflow.pe` | `RISK_OFFICER` | Caja Andina Peru |
| `comite.andina@creditflow.pe` | `COMMITTEE_MEMBER` | Caja Andina Peru |
| `operaciones.andina@creditflow.pe` | `OPERATIONS_OFFICER` | Caja Andina Peru |
| `auditor.andina@creditflow.pe` | `AUDITOR` | Caja Andina Peru |
| `admin.progreso@creditflow.pe` | `INSTITUTION_ADMIN` | Cooperativa Progreso Sur |

## Roles

- `PLATFORM_ADMIN`: consulta transversal multi-tenant
- `INSTITUTION_ADMIN`: administra y decide dentro de su institucion
- `BRANCH_OFFICER`: origina borrowers y solicitudes para su sucursal
- `CREDIT_ANALYST`: inicia revision
- `RISK_OFFICER`: registra evaluacion de riesgo
- `COMMITTEE_MEMBER`: decide casos enviados a comite
- `OPERATIONS_OFFICER`: crea y ejecuta desembolsos
- `AUDITOR`: solo lectura

## Flujo recomendado de prueba

1. Hacer login como `BRANCH_OFFICER`
2. Crear borrower
3. Crear credit application
4. Submit de la solicitud
5. Login como `CREDIT_ANALYST` y `start-review`
6. Login como `RISK_OFFICER` y registrar `risk-assessment`
7. Login como `INSTITUTION_ADMIN` y aprobar
8. Login como `OPERATIONS_OFFICER` y crear `disbursement-order`
9. Ejecutar desembolso con `Idempotency-Key`
10. Repetir la misma ejecucion con la misma key
11. Consultar auditoria y reportes

## Endpoints principales

- `POST /api/auth/login`
- `GET /api/me`
- `GET /api/institutions`
- `GET /api/branches`
- `GET /api/borrowers`
- `POST /api/borrowers`
- `GET /api/credit-products`
- `GET /api/credit-applications`
- `POST /api/credit-applications`
- `POST /api/credit-applications/{id}/submit`
- `POST /api/credit-applications/{id}/start-review`
- `POST /api/credit-applications/{id}/risk-assessment`
- `POST /api/credit-applications/{id}/send-to-committee`
- `POST /api/credit-applications/{id}/approve`
- `POST /api/credit-applications/{id}/reject`
- `POST /api/credit-applications/{id}/cancel`
- `POST /api/credit-applications/{id}/disbursement-orders`
- `POST /api/disbursement-orders/{id}/execute`
- `GET /api/audit`
- `GET /api/reports/credit-pipeline`
- `GET /api/reports/risk-summary`

## Requests HTTP

Archivo listo para pruebas manuales:

- evidence/requests.http

## Pruebas

Ejecutar:

```powershell
.\mvnw.cmd test
```

Cobertura base incluida:

- unit tests de `CreditApplicationTransitionPolicy`
- unit tests de `ApprovalLimitPolicy`
- unit tests de `DisbursementEligibilityPolicy`
- unit tests de `TenantAccessPolicy`
- unit test de idempotencia para `ExecuteDisbursementCommandHandler`
- integration test de originacion -> submit -> review -> risk assessment
- integration test de creacion de orden y ejecucion idempotente

## Comandos

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
docker compose up -d
docker compose down
docker compose down -v
```
