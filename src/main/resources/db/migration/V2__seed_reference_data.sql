insert into institution (id, code, legal_name, trade_name, status, created_at) values
    (1, 'ANDINA', 'Caja Andina Perú S.A.', 'Caja Andina Perú', 'ACTIVE', timestamp with time zone '2026-05-01 08:00:00+00'),
    (2, 'PROGRESO', 'Cooperativa Progreso Sur', 'Cooperativa Progreso Sur', 'ACTIVE', timestamp with time zone '2026-05-01 08:05:00+00'),
    (3, 'MYPEDIG', 'Financiera MYPE Digital S.A.C.', 'Financiera MYPE Digital', 'ACTIVE', timestamp with time zone '2026-05-01 08:10:00+00');

insert into branch (id, institution_id, code, name, city, status) values
    (1, 1, 'LIM-CEN', 'Lima Centro', 'Lima', 'ACTIVE'),
    (2, 1, 'AQP-CER', 'Arequipa Cercado', 'Arequipa', 'ACTIVE'),
    (3, 1, 'TRU-NOR', 'Trujillo Norte', 'Trujillo', 'ACTIVE'),
    (4, 2, 'CUS-WAN', 'Cusco Wanchaq', 'Cusco', 'ACTIVE'),
    (5, 3, 'CHI-CEN', 'Chiclayo Centro', 'Chiclayo', 'ACTIVE');

insert into borrower (id, institution_id, document_type, document_number, legal_name, trade_name, economic_activity, monthly_revenue, status, created_at) values
    (1, 1, 'RUC', '20123456789', 'Comercial Santa Rosa S.A.C.', 'Santa Rosa', 'Comercio minorista de abarrotes', 18500.00, 'ACTIVE', timestamp with time zone '2026-05-01 09:00:00+00'),
    (2, 1, 'RUC', '20598765432', 'Textiles del Sur E.I.R.L.', 'Textiles del Sur', 'Fabricación y venta de prendas', 26400.00, 'ACTIVE', timestamp with time zone '2026-05-01 09:10:00+00'),
    (3, 1, 'RUC', '20645678901', 'Distribuidora Norteña S.A.C.', 'Distribuidora Norteña', 'Distribución de insumos ferreteros', 31750.00, 'ACTIVE', timestamp with time zone '2026-05-01 09:20:00+00'),
    (4, 1, 'RUC', '20456789012', 'Servicios Cusco Andino S.R.L.', 'Cusco Andino', 'Servicios turísticos y transporte', 14200.00, 'BLOCKED', timestamp with time zone '2026-05-01 09:25:00+00'),
    (5, 2, 'RUC', '20876543210', 'Agro Progreso Sur S.A.C.', 'Agro Progreso', 'Comercialización de semillas y fertilizantes', 22800.00, 'ACTIVE', timestamp with time zone '2026-05-01 09:30:00+00'),
    (6, 3, 'RUC', '20987654321', 'Digital Market Chiclayo S.A.C.', 'Digital Market', 'Servicios de marketing digital', 19600.00, 'ACTIVE', timestamp with time zone '2026-05-01 09:40:00+00');

insert into credit_product (id, institution_id, code, name, currency, min_amount, max_amount, min_term_months, max_term_months, annual_rate, status) values
    (1, 1, 'CT-MYPE', 'Capital de Trabajo MYPE', 'PEN', 5000.00, 80000.00, 6, 36, 0.2450, 'ACTIVE'),
    (2, 1, 'CAMP-ESC', 'Campaña Escolar', 'PEN', 3000.00, 30000.00, 4, 12, 0.1990, 'ACTIVE'),
    (3, 1, 'INV-2026', 'Compra de Inventario', 'PEN', 7000.00, 60000.00, 6, 24, 0.2290, 'ACTIVE'),
    (4, 1, 'EQUIP-01', 'Crédito Equipamiento', 'PEN', 10000.00, 120000.00, 12, 48, 0.2590, 'ACTIVE'),
    (5, 2, 'PROG-CAP', 'Capital de Trabajo Progreso', 'PEN', 5000.00, 50000.00, 6, 24, 0.2390, 'ACTIVE'),
    (6, 3, 'DIGI-EXP', 'Expansión Comercial Digital', 'PEN', 4000.00, 40000.00, 6, 18, 0.2190, 'ACTIVE');
