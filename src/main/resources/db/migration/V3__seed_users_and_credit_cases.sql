insert into user_account (id, institution_id, branch_id, full_name, email, password_hash, role, status, created_at) values
    (1, null, null, 'Plataforma CreditFlow Admin', 'platform.admin@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'PLATFORM_ADMIN', 'ACTIVE', timestamp with time zone '2026-05-01 08:15:00+00'),
    (2, 1, null, 'María Quispe Andina', 'admin.andina@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'INSTITUTION_ADMIN', 'ACTIVE', timestamp with time zone '2026-05-01 08:16:00+00'),
    (3, 1, 1, 'José Ramos Lima', 'oficial.lima@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'BRANCH_OFFICER', 'ACTIVE', timestamp with time zone '2026-05-01 08:17:00+00'),
    (4, 1, null, 'Valeria Díaz Analista', 'analista.andina@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'CREDIT_ANALYST', 'ACTIVE', timestamp with time zone '2026-05-01 08:18:00+00'),
    (5, 1, null, 'Ricardo Paredes Riesgo', 'riesgo.andina@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'RISK_OFFICER', 'ACTIVE', timestamp with time zone '2026-05-01 08:19:00+00'),
    (6, 1, null, 'Comité Andina Principal', 'comite.andina@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'COMMITTEE_MEMBER', 'ACTIVE', timestamp with time zone '2026-05-01 08:20:00+00'),
    (7, 1, null, 'Paola Vega Operaciones', 'operaciones.andina@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'OPERATIONS_OFFICER', 'ACTIVE', timestamp with time zone '2026-05-01 08:21:00+00'),
    (8, 1, null, 'Lucía Torres Auditoría', 'auditor.andina@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'AUDITOR', 'ACTIVE', timestamp with time zone '2026-05-01 08:22:00+00'),
    (9, 2, null, 'Edwin Salas Progreso', 'admin.progreso@creditflow.pe', '$2a$10$NaDeXsy98p/VEf5bnmZ4mOuqWsSpAQ9pJg2uiQKNXmTRxfiBcjcLi', 'INSTITUTION_ADMIN', 'ACTIVE', timestamp with time zone '2026-05-01 08:23:00+00');

insert into credit_application (id, application_number, institution_id, branch_id, borrower_id, product_id, requested_amount, term_months, purpose, status, assigned_analyst_id, risk_assessment_id, committee_required, approved_amount, rejection_reason, created_by_user_id, created_at, updated_at, version) values
    (1, 'CFM-2026-000001', 1, 1, 1, 1, 20000.00, 12, 'Capital de trabajo para campaña de invierno', 'DRAFT', null, null, false, null, null, 3, timestamp with time zone '2026-05-02 09:00:00+00', timestamp with time zone '2026-05-02 09:00:00+00', 0),
    (2, 'CFM-2026-000002', 1, 1, 2, 2, 15000.00, 9, 'Compra de mercadería para campaña escolar', 'SUBMITTED', null, null, false, null, null, 3, timestamp with time zone '2026-05-02 09:15:00+00', timestamp with time zone '2026-05-02 10:00:00+00', 0),
    (3, 'CFM-2026-000003', 1, 2, 3, 3, 25000.00, 12, 'Reposición de inventario para ferretería', 'UNDER_REVIEW', 4, null, false, null, null, 2, timestamp with time zone '2026-05-02 10:10:00+00', timestamp with time zone '2026-05-02 11:00:00+00', 0),
    (4, 'CFM-2026-000004', 1, 1, 1, 1, 18000.00, 10, 'Capital operativo para cubrir rotación semanal', 'RISK_REVIEWED', 4, null, false, null, null, 3, timestamp with time zone '2026-05-02 11:10:00+00', timestamp with time zone '2026-05-02 12:10:00+00', 0),
    (5, 'CFM-2026-000005', 1, 2, 2, 4, 85000.00, 36, 'Adquisición de maquinaria textil semindustrial', 'PENDING_COMMITTEE', 4, null, true, null, null, 2, timestamp with time zone '2026-05-02 11:30:00+00', timestamp with time zone '2026-05-02 13:00:00+00', 0),
    (6, 'CFM-2026-000006', 1, 1, 3, 3, 28000.00, 14, 'Compra de stock para campaña comercial', 'APPROVED', 4, null, false, 25000.00, null, 3, timestamp with time zone '2026-05-02 12:00:00+00', timestamp with time zone '2026-05-02 14:15:00+00', 0),
    (7, 'CFM-2026-000007', 1, 1, 1, 1, 32000.00, 18, 'Expansión de capital de trabajo para nueva ruta', 'DISBURSEMENT_PENDING', 4, null, false, 30000.00, null, 2, timestamp with time zone '2026-05-02 12:30:00+00', timestamp with time zone '2026-05-02 15:20:00+00', 0),
    (8, 'CFM-2026-000008', 1, 1, 2, 1, 22000.00, 12, 'Refuerzo de caja para pago de proveedores', 'DISBURSED', 4, null, false, 22000.00, null, 3, timestamp with time zone '2026-05-02 13:00:00+00', timestamp with time zone '2026-05-02 16:30:00+00', 0),
    (9, 'CFM-2026-000009', 1, 3, 3, 4, 95000.00, 24, 'Equipamiento para ampliar taller de corte', 'REJECTED', 4, null, false, null, 'Capacidad de pago insuficiente para el endeudamiento solicitado', 2, timestamp with time zone '2026-05-02 13:30:00+00', timestamp with time zone '2026-05-02 17:00:00+00', 0),
    (10, 'CFM-2026-000010', 2, 4, 5, 5, 16000.00, 12, 'Compra de semillas para campaña agrícola', 'SUBMITTED', null, null, false, null, null, 9, timestamp with time zone '2026-05-02 14:00:00+00', timestamp with time zone '2026-05-02 14:30:00+00', 0);

insert into risk_assessment (id, application_id, institution_id, score, risk_level, debt_to_income_ratio, flags_json, recommendation, assessed_by_user_id, assessed_at) values
    (1, 4, 1, 80, 'LOW', 0.2800, '[]', 'APPROVE', 5, timestamp with time zone '2026-05-02 12:05:00+00'),
    (3, 6, 1, 77, 'LOW', 0.3100, '["supplier_dependency"]', 'APPROVE', 5, timestamp with time zone '2026-05-02 13:40:00+00'),
    (4, 7, 1, 82, 'LOW', 0.2900, '[]', 'APPROVE', 5, timestamp with time zone '2026-05-02 14:40:00+00'),
    (5, 8, 1, 79, 'LOW', 0.2600, '[]', 'APPROVE', 5, timestamp with time zone '2026-05-02 15:10:00+00'),
    (6, 9, 1, 28, 'CRITICAL', 0.8800, '["tax_arrears","negative_cashflow"]', 'REJECT', 5, timestamp with time zone '2026-05-02 16:20:00+00');

update credit_application set risk_assessment_id = 1 where id = 4;
update credit_application set risk_assessment_id = 3 where id = 6;
update credit_application set risk_assessment_id = 4 where id = 7;
update credit_application set risk_assessment_id = 5 where id = 8;
update credit_application set risk_assessment_id = 6 where id = 9;

insert into approval_decision (id, application_id, institution_id, decision, approved_amount, reason, decided_by_user_id, decided_at, decision_source) values
    (1, 6, 1, 'APPROVED', 25000.00, null, 2, timestamp with time zone '2026-05-02 14:15:00+00', 'MANAGER'),
    (2, 8, 1, 'APPROVED', 22000.00, null, 2, timestamp with time zone '2026-05-02 15:35:00+00', 'MANAGER'),
    (3, 9, 1, 'REJECTED', null, 'Capacidad de pago insuficiente para el endeudamiento solicitado', 2, timestamp with time zone '2026-05-02 16:50:00+00', 'MANAGER');

insert into disbursement_order (id, application_id, institution_id, amount, currency, destination_bank, destination_account, status, idempotency_key, created_by_user_id, executed_by_user_id, created_at, executed_at, version) values
    (1, 7, 1, 30000.00, 'PEN', 'BCP', '193-884512-0-44', 'CREATED', null, 7, null, timestamp with time zone '2026-05-02 15:20:00+00', null, 0),
    (2, 8, 1, 22000.00, 'PEN', 'Interbank', '898-220331-1', 'EXECUTED', 'seed-disb-001', 7, 7, timestamp with time zone '2026-05-02 15:50:00+00', timestamp with time zone '2026-05-02 16:30:00+00', 0);

insert into idempotency_record (id, institution_id, idempotency_key, operation_type, resource_type, resource_id, response_status, created_at) values
    (1, 1, 'seed-disb-001', 'EXECUTE_DISBURSEMENT', 'DISBURSEMENT_ORDER', 2, 'EXECUTED', timestamp with time zone '2026-05-02 16:30:00+00');

insert into audit_entry (id, institution_id, actor_user_id, action, entity_type, entity_id, previous_status, new_status, detail_json, created_at) values
    (1, 1, 3, 'CREDIT_APPLICATION_CREATED', 'CREDIT_APPLICATION', '1', null, 'DRAFT', '{"applicationNumber":"CFM-2026-000001","branchId":1}', timestamp with time zone '2026-05-02 09:00:00+00'),
    (2, 1, 3, 'CREDIT_APPLICATION_SUBMITTED', 'CREDIT_APPLICATION', '2', 'DRAFT', 'SUBMITTED', '{"applicationNumber":"CFM-2026-000002"}', timestamp with time zone '2026-05-02 10:00:00+00'),
    (3, 1, 4, 'CREDIT_REVIEW_STARTED', 'CREDIT_APPLICATION', '3', 'SUBMITTED', 'UNDER_REVIEW', '{"assignedAnalystId":4}', timestamp with time zone '2026-05-02 11:00:00+00'),
    (4, 1, 5, 'RISK_ASSESSMENT_RECORDED', 'CREDIT_APPLICATION', '4', 'UNDER_REVIEW', 'RISK_REVIEWED', '{"riskAssessmentId":1,"riskLevel":"LOW"}', timestamp with time zone '2026-05-02 12:05:00+00'),
    (5, 1, 4, 'CREDIT_APPLICATION_SENT_TO_COMMITTEE', 'CREDIT_APPLICATION', '5', 'RISK_REVIEWED', 'PENDING_COMMITTEE', '{"committeeRequired":true}', timestamp with time zone '2026-05-02 13:00:00+00'),
    (6, 1, 2, 'CREDIT_APPLICATION_APPROVED', 'CREDIT_APPLICATION', '6', 'RISK_REVIEWED', 'APPROVED', '{"approvalDecisionId":1,"approvedAmount":25000.00}', timestamp with time zone '2026-05-02 14:15:00+00'),
    (7, 1, 7, 'DISBURSEMENT_ORDER_CREATED', 'DISBURSEMENT_ORDER', '1', null, 'CREATED', '{"applicationId":7,"applicationNewStatus":"DISBURSEMENT_PENDING"}', timestamp with time zone '2026-05-02 15:20:00+00'),
    (8, 1, 2, 'CREDIT_APPLICATION_REJECTED', 'CREDIT_APPLICATION', '9', 'RISK_REVIEWED', 'REJECTED', '{"approvalDecisionId":3,"reason":"Capacidad de pago insuficiente para el endeudamiento solicitado"}', timestamp with time zone '2026-05-02 16:50:00+00'),
    (9, 1, 7, 'DISBURSEMENT_EXECUTED', 'DISBURSEMENT_ORDER', '2', 'CREATED', 'EXECUTED', '{"applicationId":8,"applicationNewStatus":"DISBURSED","idempotencyKey":"seed-disb-001"}', timestamp with time zone '2026-05-02 16:30:00+00');

alter table institution alter column id restart with 100;
alter table branch alter column id restart with 100;
alter table user_account alter column id restart with 100;
alter table borrower alter column id restart with 100;
alter table credit_product alter column id restart with 100;
alter table credit_application alter column id restart with 100;
alter table risk_assessment alter column id restart with 100;
alter table approval_decision alter column id restart with 100;
alter table disbursement_order alter column id restart with 100;
alter table idempotency_record alter column id restart with 100;
alter table audit_entry alter column id restart with 100;
