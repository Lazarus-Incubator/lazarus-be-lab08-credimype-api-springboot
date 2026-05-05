# LAB.md

## Contexto

`creditflow-mype-api` representa una API REST multi-tenant usada por instituciones financieras peruanas para operar creditos MYPE. El sistema cubre originacion, evaluacion, aprobacion, desembolso y auditoria dentro de un backend Spring Boot modular que sigue un enfoque de DDD pragmatico y CQRS.

En este laboratorio el proyecto ya existe, compila y levanta, pero el equipo reporta comportamientos inconsistentes que deben investigarse antes de proponer estabilizaciones.

## Objetivo del laboratorio

Tu rol es entrar como backend a una API heredada y estabilizarla.

El trabajo esperado es:

1. reproducir sintomas con Swagger o con evidence/requests.http
2. aislar la capa donde aparece el comportamiento
3. contrastar el resultado observado con la regla de negocio esperada
4. escribir o ajustar pruebas que capturen el problema
5. aplicar correcciones con criterio de dominio, seguridad y consistencia

## Dominios principales

- `identity`: autenticacion, JWT y usuario autenticado
- `institutions`: instituciones, sucursales y alcance tenant
- `borrowers`: clientes MYPE
- `creditproducts`: productos crediticios
- `applications`: agregado principal de solicitudes de credito
- `risk`: evaluacion de riesgo y score
- `approvals`: aprobacion o rechazo
- `disbursements`: ordenes de desembolso e idempotencia
- `audit`: trazabilidad de acciones relevantes
- `reports`: consultas operativas y agregados

## Roles operativos

- `PLATFORM_ADMIN`: visibilidad transversal
- `INSTITUTION_ADMIN`: opera dentro de su institucion
- `BRANCH_OFFICER`: origina borrowers y solicitudes
- `CREDIT_ANALYST`: inicia revision
- `RISK_OFFICER`: registra evaluacion de riesgo
- `COMMITTEE_MEMBER`: decide casos enviados a comite
- `OPERATIONS_OFFICER`: crea y ejecuta desembolsos
- `AUDITOR`: perfil de consulta

## Flujo de credito esperado

Estados de `CreditApplication`:

- `DRAFT`
- `SUBMITTED`
- `UNDER_REVIEW`
- `RISK_REVIEWED`
- `PENDING_COMMITTEE`
- `APPROVED`
- `REJECTED`
- `DISBURSEMENT_PENDING`
- `DISBURSED`
- `CANCELLED`

Recorrido de referencia:

1. crear borrower
2. crear solicitud en `DRAFT`
3. enviar con `submit`
4. iniciar revision
5. registrar evaluacion de riesgo
6. aprobar, rechazar o enviar a comite segun politica
7. crear orden de desembolso
8. ejecutar desembolso con `Idempotency-Key`
9. revisar auditoria y reportes

## Reglas de negocio que debes proteger

- una institucion suspendida no debe originar nuevas solicitudes
- una sucursal inactiva no debe originar nuevas solicitudes
- un borrower bloqueado no debe recibir nuevas solicitudes
- solo productos activos aceptan nuevas originaciones
- una solicitud no debe aprobarse sin la evaluacion requerida
- `HIGH` requiere comite
- `CRITICAL` no debe aprobarse
- el monto aprobado no debe superar al solicitado
- el rechazo requiere un motivo valido
- el desembolso solo debe ejecutarse una vez por intento compatible
- usuarios fuera del alcance tenant no deben consultar ni mutar datos ajenos

## Sintomas reportados por el negocio

El equipo funcional y operativo comparte estos hallazgos iniciales:

- algunos usuarios ven diferencias entre listados y consultas por ID directo
- auditoria no siempre refleja con claridad ciertas transiciones
- operaciones tiene dudas con reintentos de desembolso y trazabilidad asociada
- algunos reportes no parecen coincidir cuando se comparan por institucion
- riesgo detecto casos aprobados en condiciones discutibles
- QA sospecha huecos en validaciones de motivos y montos
- ciertos perfiles parecen tener mas alcance del esperado en acciones puntuales

No asumas que todo esta roto. Parte del laboratorio consiste en distinguir entre flujo sano, borde funcional y defecto real.

## Recomendaciones

- compara lo que devuelve un listado contra el detalle del mismo recurso
- prueba el mismo flujo con distintos roles y con usuarios de otra institucion
- revisa command handlers, policies y query services por separado
- sigue el rastro desde controller hasta repository antes de corregir
- observa que varias reglas se aplican en distintos puntos del flujo
- no limites el analisis a seguridad: tambien hay consistencia, mapping y reportes
- usa logs, respuestas HTTP, auditoria y pruebas automatizadas como evidencia

## Rutas importantes

- `src/main/java/com/creditflow/shared`
- `src/main/java/com/creditflow/identity`
- `src/main/java/com/creditflow/institutions`
- `src/main/java/com/creditflow/applications`
- `src/main/java/com/creditflow/risk`
- `src/main/java/com/creditflow/approvals`
- `src/main/java/com/creditflow/disbursements`
- `src/main/java/com/creditflow/audit`
- `src/main/java/com/creditflow/reports`
- `src/main/resources/db/migration`
- `src/test/java/com/creditflow`

## Como trabajar el laboratorio

1. levanta PostgreSQL con `docker compose up -d`
2. ejecuta la aplicacion con `.\mvnw.cmd spring-boot:run`
3. abre Swagger en `http://localhost:8080/swagger-ui.html`
4. usa evidence/requests.http como punto de partida
5. crea pruebas adicionales cuando encuentres un comportamiento sospechoso
6. documenta reproduccion, causa raiz y propuesta de correccion antes de cerrar cada hallazgo

## Resultado esperado

Al finalizar deberias poder explicar:

- que reglas de negocio gobiernan el workflow
- donde se aplica el aislamiento multi-tenant
- como se decide la aprobacion segun riesgo y rol
- como se protege la idempotencia del desembolso
- donde conviene reforzar validaciones, auditoria y consultas
