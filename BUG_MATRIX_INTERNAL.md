# BUG_MATRIX_INTERNAL.md

Documento interno para seguimiento del instructor. Resume los defectos sembrados en el laboratorio y el razonamiento tecnico que deberia emerger durante la investigacion.

## BUG-01

- ID: `BUG-01`
- nombre: Tenant leak en consulta por ID de credit application
- severidad: critica
- dificultad: dificil
- modulo afectado: `applications`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/application/CreditApplicationQueryService.java`
- sintoma observable: un usuario de una institucion puede consultar por ID directo una solicitud de otra institucion aun cuando el listado parezca correctamente acotado.
- regla esperada: el detalle debe respetar el mismo aislamiento tenant que el listado.
- causa tecnica sembrada: `getById` usa `findById` directo y omite la verificacion de `TenantAccessPolicy`.
- forma sugerida de reproduccion: listar solicitudes con un usuario de la institucion A y luego consultar por ID una solicitud conocida de la institucion B usando `GET /api/credit-applications/{id}`.
- comentario/Javadoc estrategico agregado si aplica: "The list already applies institutional scoping; detail keeps a direct lookup to reduce query cost."
- concepto tecnico que evalua: aislamiento multi-tenant en queries de detalle

## BUG-02

- ID: `BUG-02`
- nombre: Tenant leak en reporte de pipeline
- severidad: critica
- dificultad: media
- modulo afectado: `reports`
- archivos modificados:
  - `src/main/java/com/creditflow/reports/application/PipelineReportQueryService.java`
- sintoma observable: usuarios que no son `PLATFORM_ADMIN` reciben un pipeline agregado con datos de mas de una institucion.
- regla esperada: un usuario no transversal debe ver solo datos de su institucion.
- causa tecnica sembrada: el servicio deja `institutionId` en `null` para la mayoria de roles y ejecuta el agregado global.
- forma sugerida de reproduccion: comparar `GET /api/reports/credit-pipeline` con `admin.andina` y `admin.progreso`, observando totales coincidentes o ajenos.
- comentario/Javadoc estrategico agregado si aplica: "Pipeline aggregates remain global for most operational users so the dashboard can be compared across queues."
- concepto tecnico que evalua: scoping tenant en reportes agregados

## BUG-03

- ID: `BUG-03`
- nombre: Branch officer puede crear solicitud para otra sucursal
- severidad: alta
- dificultad: media
- modulo afectado: `applications`, `institutions`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/application/CreateCreditApplicationCommandHandler.java`
- sintoma observable: un `BRANCH_OFFICER` puede originar una solicitud indicando `branchId` de otra sucursal de la misma institucion.
- regla esperada: el branch officer debe originar solo sobre su propia sucursal.
- causa tecnica sembrada: el handler valida escritura a nivel institucion y confia en `branchId` provisto por el request.
- forma sugerida de reproduccion: hacer login con `oficial.lima@creditflow.pe` y crear una solicitud usando un `branchId` distinto al de su token.
- comentario/Javadoc estrategico agregado si aplica: "branchId remains request-driven to support assisted origination scenarios across the institution."
- concepto tecnico que evalua: autorizacion contextual basada en sucursal

## BUG-04

- ID: `BUG-04`
- nombre: Auditor puede cancelar una solicitud
- severidad: alta
- dificultad: facil
- modulo afectado: `applications`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/application/CancelCreditApplicationCommandHandler.java`
- sintoma observable: un usuario con rol `AUDITOR` puede ejecutar `cancel` sobre una solicitud.
- regla esperada: `AUDITOR` debe ser solo lectura.
- causa tecnica sembrada: la condicion de autorizacion permite `AUDITOR` como actor valido junto a `INSTITUTION_ADMIN` y creador.
- forma sugerida de reproduccion: autenticar como `auditor.andina@creditflow.pe` y ejecutar `POST /api/credit-applications/{id}/cancel` sobre una solicitud cancelable.
- comentario/Javadoc estrategico agregado si aplica: no aplica
- concepto tecnico que evalua: autorizacion por rol en operaciones de mutacion

## BUG-05

- ID: `BUG-05`
- nombre: Aprobacion sin evaluacion de riesgo en camino alternativo
- severidad: critica
- dificultad: dificil
- modulo afectado: `applications`, `approvals`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/domain/CreditApplication.java`
  - `src/main/java/com/creditflow/approvals/application/ApproveCreditApplicationCommandHandler.java`
  - `src/main/resources/db/migration/V3__seed_users_and_credit_cases.sql`
- sintoma observable: existe un camino donde una solicitud puede terminar aprobada sin `RiskAssessment` persistido.
- regla esperada: no debe aprobarse ninguna solicitud sin evaluacion de riesgo valida.
- causa tecnica sembrada: el flujo de comite usa un atajo de agregado que no consulta la policy principal ni la evaluacion almacenada; ademas se dejo un caso semilla `PENDING_COMMITTEE` sin `risk_assessment_id`.
- forma sugerida de reproduccion: inspeccionar la solicitud semilla `id=5` y aprobarla desde comite o revisar el flujo alternativo de comite en `requests.http`.
- comentario/Javadoc estrategico agregado si aplica: "Committee keeps an exception lane because the risk file may still be reconciling when the vote is registered."
- concepto tecnico que evalua: consistencia de workflow y aplicacion uniforme de invariantes

## BUG-06

- ID: `BUG-06`
- nombre: Riesgo CRITICAL puede aprobarse por comite
- severidad: alta
- dificultad: media
- modulo afectado: `approvals`, `risk`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/domain/CreditApplication.java`
  - `src/main/java/com/creditflow/approvals/application/ApproveCreditApplicationCommandHandler.java`
- sintoma observable: una solicitud con `riskLevel = CRITICAL` puede quedar aprobada si entra por el camino de comite.
- regla esperada: `CRITICAL` no debe aprobarse.
- causa tecnica sembrada: la restriccion de riesgo se mantiene en el camino normal, pero el camino de comite usa `approveFromCommittee` sin revisar `RiskLevel`.
- forma sugerida de reproduccion: generar una solicitud, registrar una evaluacion critica que derive a comite y aprobar como `COMMITTEE_MEMBER`.
- comentario/Javadoc estrategico agregado si aplica: "The committee retains an exception lane for elevated risk cases."
- concepto tecnico que evalua: politicas de riesgo y autorizacion de aprobacion

## BUG-07

- ID: `BUG-07`
- nombre: Monto aprobado puede superar al solicitado por comparacion incorrecta
- severidad: alta
- dificultad: media
- modulo afectado: `applications`, `approvals`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/domain/CreditApplication.java`
- sintoma observable: en ciertos montos con decimales, el sistema acepta un `approvedAmount` ligeramente superior al monto solicitado.
- regla esperada: `approvedAmount <= requestedAmount`.
- causa tecnica sembrada: `approveFromCommittee` compara usando `intValue()` en lugar de `BigDecimal.compareTo`.
- forma sugerida de reproduccion: aprobar por comite una solicitud pedida por `12000.10` con un monto aprobado de `12000.20`.
- comentario/Javadoc estrategico agregado si aplica: no aplica
- concepto tecnico que evalua: precision monetaria y reglas de dominio

## BUG-08

- ID: `BUG-08`
- nombre: Rechazo acepta motivo compuesto solo por espacios
- severidad: media
- dificultad: facil
- modulo afectado: `applications`, `web`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/domain/CreditApplicationTransitionPolicy.java`
  - `src/main/java/com/creditflow/applications/web/CreditApplicationController.java`
- sintoma observable: el endpoint de rechazo acepta `"reason": "     "`.
- regla esperada: el motivo de rechazo debe ser un texto no vacio y no compuesto solo por espacios.
- causa tecnica sembrada: se reemplazo `@NotBlank` por `@NotEmpty` y la policy valida `isEmpty()` en lugar de `isBlank()`.
- forma sugerida de reproduccion: rechazar una solicitud con un body cuyo `reason` contenga solo espacios.
- comentario/Javadoc estrategico agregado si aplica: no aplica
- concepto tecnico que evalua: Bean Validation y validacion de invariantes

## BUG-09

- ID: `BUG-09`
- nombre: Idempotency-Key no esta correctamente scopeada por operacion
- severidad: alta
- dificultad: dificil
- modulo afectado: `disbursements`
- archivos modificados:
  - `src/main/java/com/creditflow/disbursements/application/IdempotencyService.java`
- sintoma observable: una misma `Idempotency-Key` reutilizada en otra operacion compatible dentro de desembolsos puede devolver un resultado incorrecto o tratarse como repeticion valida.
- regla esperada: la idempotencia debe considerar al menos institucion, tipo de operacion y referencia del recurso.
- causa tecnica sembrada: la compatibilidad se evalua por institucion, key y `resourceType`, ignorando `operationType` y `resourceId`.
- forma sugerida de reproduccion: ejecutar un desembolso con una key y reutilizar la misma key sobre otra orden o camino de disbursement, observando replay/conflicto impropio.
- comentario/Javadoc estrategico agregado si aplica: "Idempotency is evaluated at institution level to avoid cross-tenant key collisions."
- concepto tecnico que evalua: diseno correcto de idempotencia

## BUG-10

- ID: `BUG-10`
- nombre: Reintento idempotente duplica auditoria
- severidad: media
- dificultad: media
- modulo afectado: `disbursements`, `audit`
- archivos modificados:
  - `src/main/java/com/creditflow/disbursements/application/ExecuteDisbursementCommandHandler.java`
- sintoma observable: repetir `execute` con la misma `Idempotency-Key` no duplica el desembolso, pero agrega una nueva entrada de auditoria.
- regla esperada: un replay idempotente compatible debe reutilizar el resultado sin registrar una nueva ejecucion exitosa.
- causa tecnica sembrada: el handler publica un `DisbursementExecutedEvent` antes de retornar el replay detectado.
- forma sugerida de reproduccion: ejecutar una orden dos veces con la misma key y luego revisar `GET /api/audit`.
- comentario/Javadoc estrategico agregado si aplica: "Audit stays at the edge of the operational flow so retries leave the same observable trace as the original attempt."
- concepto tecnico que evalua: interaccion entre idempotencia y trazabilidad

## BUG-11

- ID: `BUG-11`
- nombre: Falta de transaccion en ejecucion de desembolso
- severidad: media
- dificultad: dificil
- modulo afectado: `disbursements`, `applications`
- archivos modificados:
  - `src/main/java/com/creditflow/disbursements/application/ExecuteDisbursementCommandHandler.java`
- sintoma observable: ante una excepcion intermedia o ciertos recorridos, la orden puede quedar `EXECUTED` mientras la solicitud no pasa a `DISBURSED`.
- regla esperada: la ejecucion del desembolso y la actualizacion de la solicitud deben confirmarse o revertirse juntas.
- causa tecnica sembrada: se quito `@Transactional` del handler y se separaron los pasos persistiendo la orden antes de completar el resto del flujo.
- forma sugerida de reproduccion: inspeccionar codigo, forzar una excepcion posterior al guardado de la orden o verificar despues de ejecutar que application y order no siempre quedan sincronizadas.
- comentario/Javadoc estrategico agregado si aplica: "The execution steps stay separated to keep the operational path easier to read during incident review."
- concepto tecnico que evalua: limites transaccionales y consistencia entre agregados

## BUG-12

- ID: `BUG-12`
- nombre: Optimistic locking degradado en inicio de revision
- severidad: media
- dificultad: media
- modulo afectado: `applications`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/application/StartCreditReviewCommandHandler.java`
  - `src/main/java/com/creditflow/applications/infrastructure/CreditApplicationRepository.java`
- sintoma observable: actualizaciones concurrentes sobre la misma solicitud pueden sobrescribirse sin respetar claramente el `@Version`.
- regla esperada: una actualizacion concurrente incompatible debe producir conflicto de concurrencia o al menos respetar el versionado.
- causa tecnica sembrada: el inicio de revision usa un `update` directo por JPQL que no incluye control de version del agregado.
- forma sugerida de reproduccion: revisar el handler y simular dos cambios cercanos sobre la misma solicitud, especialmente alrededor de `start-review`.
- comentario/Javadoc estrategico agregado si aplica: "Review assignment uses a lean update path because analysts typically work this queue at high volume."
- concepto tecnico que evalua: optimistic locking y bypass de JPA versioning

## BUG-13

- ID: `BUG-13`
- nombre: Auditoria con previousStatus y newStatus invertidos en cancelacion
- severidad: baja
- dificultad: facil
- modulo afectado: `applications`, `audit`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/application/CancelCreditApplicationCommandHandler.java`
- sintoma observable: la auditoria de cancelacion registra estados invertidos entre antes y despues.
- regla esperada: `previousStatus` debe reflejar el estado previo y `newStatus` el estado resultante.
- causa tecnica sembrada: el evento de cancelacion se construye con los campos `previousStatus` y `newStatus` invertidos.
- forma sugerida de reproduccion: cancelar una solicitud y comparar el estado real con la entrada generada en `GET /api/audit`.
- comentario/Javadoc estrategico agregado si aplica: no aplica
- concepto tecnico que evalua: integridad semantica de eventos y auditoria

## BUG-14

- ID: `BUG-14`
- nombre: DTO de usuario expone passwordHash
- severidad: alta
- dificultad: facil
- modulo afectado: `identity`
- archivos modificados:
  - `src/main/java/com/creditflow/identity/application/AuthService.java`
- sintoma observable: `GET /api/me` devuelve `passwordHash` dentro del perfil autenticado.
- regla esperada: nunca debe exponerse `passwordHash` al cliente.
- causa tecnica sembrada: el DTO `CurrentUserProfile` incluye y mapea el hash de password desde la entidad.
- forma sugerida de reproduccion: autenticarse con cualquier usuario y consultar `GET /api/me`.
- comentario/Javadoc estrategico agregado si aplica: no aplica
- concepto tecnico que evalua: seguridad de datos y mapping de respuestas

## BUG-15

- ID: `BUG-15`
- nombre: Listado de solicitudes con N+1 en el mapping
- severidad: baja
- dificultad: media
- modulo afectado: `applications`, `reports`
- archivos modificados:
  - `src/main/java/com/creditflow/applications/application/CreditApplicationQueryService.java`
- sintoma observable: el listado funciona, pero emite multiples consultas adicionales para borrower, producto y sucursal por cada solicitud.
- regla esperada: los listados empresariales deben usar proyecciones o cargas agrupadas para evitar N+1 evidente.
- causa tecnica sembrada: el mapper consulta tres repositorios por cada item en lugar de precargar por lote.
- forma sugerida de reproduccion: habilitar SQL log o perfilar `GET /api/credit-applications` con varias solicitudes semilla.
- comentario/Javadoc estrategico agregado si aplica: "Explicit lookups keep the mapping easy to trace while the list sizes stay operationally small."
- concepto tecnico que evalua: eficiencia de consultas JPA y diagnostico de N+1
