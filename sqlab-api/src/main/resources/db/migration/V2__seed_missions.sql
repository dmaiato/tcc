-- V2__seed_missions.sql

INSERT INTO missions (id, title, briefing, objective, hint, ddl_script, dml_script, techniques, xp_reward, expected_result, ordered, theme, difficulty)
VALUES

-- MISSION 1: BEGINNER / CRIMINAL
(
    gen_random_uuid(),
    'A Lista de Suspeitos',
    'Um crime foi cometido na cidade. Sua primeira tarefa é simples: liste todos os suspeitos cadastrados no banco de dados. O detetive precisa saber com quem está lidando.',
    'Liste todos os suspeitos cadastrados no banco de dados.',
    'SELECT * FROM suspects;',
    '
        CREATE TABLE suspects (
            id      SERIAL PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            age     INTEGER NOT NULL,
            city    VARCHAR(100) NOT NULL
        );
    ',
    '
        INSERT INTO suspects (name, age, city) VALUES
            (''Carlos Mendes'', 34, ''São Paulo''),
            (''Ana Lima'', 28, ''Rio de Janeiro''),
            (''Pedro Costa'', 45, ''Belo Horizonte''),
            (''Julia Ramos'', 31, ''São Paulo'');
    ',
    ARRAY['SELECT'],
    100,
    '[
      {"id": 1, "name": "Carlos Mendes", "age": 34, "city": "São Paulo"},
      {"id": 2, "name": "Ana Lima", "age": 28, "city": "Rio de Janeiro"},
      {"id": 3, "name": "Pedro Costa", "age": 45, "city": "Belo Horizonte"},
      {"id": 4, "name": "Julia Ramos", "age": 31, "city": "São Paulo"}
    ]',
    FALSE,
    'CRIMINAL',
    'BEGINNER'
),

-- MISSION 2: BEGINNER / CRIMINAL
(
    gen_random_uuid(),
    'Suspeitos de São Paulo',
    'O crime aconteceu em São Paulo. Filtre apenas os suspeitos que residem nessa cidade para estreitar as investigações.',
    'Filtre apenas os suspeitos que residem em São Paulo.',
    'SELECT * FROM suspects WHERE city = ''São Paulo'';',
    '
        CREATE TABLE suspects (
            id      SERIAL PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            age     INTEGER NOT NULL,
            city    VARCHAR(100) NOT NULL
        );
    ',
    '
        INSERT INTO suspects (name, age, city) VALUES
            (''Carlos Mendes'', 34, ''São Paulo''),
            (''Ana Lima'', 28, ''Rio de Janeiro''),
            (''Pedro Costa'', 45, ''Belo Horizonte''),
            (''Julia Ramos'', 31, ''São Paulo'');
    ',
    ARRAY['SELECT', 'WHERE'],
    100,
    '[
        {"id": 1, "name": "Carlos Mendes", "age": 34, "city": "São Paulo"},
        {"id": 4, "name": "Julia Ramos", "age": 31, "city": "São Paulo"}
    ]',
    FALSE,
    'CRIMINAL',
    'BEGINNER'
),

-- MISSION 3: INTERMEDIATE / CRIMINAL
(
    gen_random_uuid(),
    'Cruzando Evidências',
    'Temos duas tabelas: suspeitos e registros de ocorrências. Descubra quais suspeitos possuem ao menos uma ocorrência registrada em seu nome.',
    'Descubra quais suspeitos possuem ao menos uma ocorrência registrada.',
    'SELECT DISTINCT suspects.name FROM suspects INNER JOIN occurrences ON suspects.id = occurrences.suspect_id;',
    '
        CREATE TABLE suspects (
            id      SERIAL PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            city    VARCHAR(100) NOT NULL
        );
        CREATE TABLE occurrences (
            id          SERIAL PRIMARY KEY,
            suspect_id  INTEGER NOT NULL REFERENCES suspects(id),
            description TEXT NOT NULL
        );
    ',
    '
        INSERT INTO suspects (name, city) VALUES
            (''Carlos Mendes'', ''São Paulo''),
            (''Ana Lima'', ''Rio de Janeiro''),
            (''Pedro Costa'', ''Belo Horizonte'');
        INSERT INTO occurrences (suspect_id, description) VALUES
            (1, ''Suspeito de roubo em 2023''),
            (3, ''Envolvido em briga em local público'');
    ',
    ARRAY['SELECT', 'INNER JOIN'],
    200,
    '[
        {"id": 1, "name": "Carlos Mendes", "city": "São Paulo"},
        {"id": 3, "name": "Pedro Costa", "city": "Belo Horizonte"}
    ]',
    FALSE,
    'CRIMINAL',
    'INTERMEDIATE'
),

-- MISSION 4: INTERMEDIATE / ASTRONOMY
(
    gen_random_uuid(),
    'Planetas do Sistema Solar',
    'O observatório registrou dados sobre os planetas do sistema solar. Liste os planetas em ordem crescente de distância do Sol.',
    'Liste os planetas em ordem crescente de distância do Sol.',
    'SELECT * FROM planets ORDER BY distance_from_sun_km ASC;',
    '
        CREATE TABLE planets (
            id                   SERIAL PRIMARY KEY,
            name                 VARCHAR(100) NOT NULL,
            distance_from_sun_km BIGINT NOT NULL,
            has_rings            BOOLEAN NOT NULL
        );
    ',
    '
        INSERT INTO planets (name, distance_from_sun_km, has_rings) VALUES
            (''Mercúrio'',    57900000,  FALSE),
            (''Vênus'',      108200000,  FALSE),
            (''Terra'',      149600000,  FALSE),
            (''Marte'',      227900000,  FALSE),
            (''Júpiter'',    778500000,  TRUE),
            (''Saturno'',   1432000000,  TRUE),
            (''Urano'',     2867000000,  TRUE),
            (''Netuno'',    4515000000,  TRUE);
    ',
    ARRAY['SELECT', 'ORDER BY'],
    200,
    '[
        {"id": 1, "name": "Mercúrio",  "distance_from_sun_km": 57900000,   "has_rings": false},
        {"id": 2, "name": "Vênus",     "distance_from_sun_km": 108200000,  "has_rings": false},
        {"id": 3, "name": "Terra",     "distance_from_sun_km": 149600000,  "has_rings": false},
        {"id": 4, "name": "Marte",     "distance_from_sun_km": 227900000,  "has_rings": false},
        {"id": 5, "name": "Júpiter",   "distance_from_sun_km": 778500000,  "has_rings": true},
        {"id": 6, "name": "Saturno",   "distance_from_sun_km": 1432000000, "has_rings": true},
        {"id": 7, "name": "Urano",     "distance_from_sun_km": 2867000000, "has_rings": true},
        {"id": 8, "name": "Netuno",    "distance_from_sun_km": 4515000000, "has_rings": true}
    ]',
    TRUE,
    'ASTRONOMY',
    'INTERMEDIATE'
),

-- MISSION 5: ADVANCED / ASTRONOMY
(
    gen_random_uuid(),
    'Média de Distância por Tipo',
    'Calcule a distância média do Sol para planetas com anéis e para planetas sem anéis. Retorne o resultado ordenado pelo campo has_rings.',
    'Calcule a distância média do Sol para planetas com anéis e sem anéis, ordenado por has_rings.',
    'SELECT has_rings, AVG(distance_from_sun_km) AS avg_distance FROM planets GROUP BY has_rings ORDER BY has_rings;',
    '
        CREATE TABLE planets (
            id                   SERIAL PRIMARY KEY,
            name                 VARCHAR(100) NOT NULL,
            distance_from_sun_km BIGINT NOT NULL,
            has_rings            BOOLEAN NOT NULL
        );
    ',
    '
        INSERT INTO planets (name, distance_from_sun_km, has_rings) VALUES
            (''Mercúrio'',    57900000,  FALSE),
            (''Vênus'',      108200000,  FALSE),
            (''Terra'',      149600000,  FALSE),
            (''Marte'',      227900000,  FALSE),
            (''Júpiter'',    778500000,  TRUE),
            (''Saturno'',   1432000000,  TRUE),
            (''Urano'',     2867000000,  TRUE),
            (''Netuno'',    4515000000,  TRUE);
    ',
    ARRAY['SELECT', 'GROUP BY', 'AVG'],
    350,
    '[
        {"has_rings": false, "avg_distance": 135850000.0},
        {"has_rings": true,  "avg_distance": 2398125000.0}
    ]',
    TRUE,
    'ASTRONOMY',
    'ADVANCED'
),

-- MISSION 6: ADVANCED / CYBERSECURITY
(
    gen_random_uuid(),
    'Usuários sem Autenticação Recente',
    'O sistema de segurança identificou contas suspeitas. Liste os usuários que nunca realizaram login ou cujo último login foi antes de 2023, ordenados pelo nome.',
    'Liste os usuários sem login recente (nunca ou antes de 2023), ordenados por nome.',
    'SELECT * FROM system_users WHERE last_login IS NULL OR last_login < ''2023-01-01'' ORDER BY username;',
    '
        CREATE TABLE system_users (
            id          SERIAL PRIMARY KEY,
            username    VARCHAR(100) NOT NULL,
            last_login  DATE
        );
    ',
    '
        INSERT INTO system_users (username, last_login) VALUES
            (''admin'',   ''2024-03-15''),
            (''guest'',    NULL),
            (''jsmith'',  ''2022-11-01''),
            (''mlopez'',  ''2023-06-20''),
            (''asilva'',   NULL);
    ',
    ARRAY['SELECT', 'WHERE', 'IS NULL', 'OR', 'ORDER BY'],
    350,
    '[
        {"id": 5, "username": "asilva",  "last_login": null},
        {"id": 2, "username": "guest",   "last_login": null},
        {"id": 3, "username": "jsmith",  "last_login": "2022-11-01"}
    ]',
    TRUE,
    'CYBERSECURITY',
    'ADVANCED'
),

-- MISSION 7: EXPERT / FINANCE
(
    gen_random_uuid(),
    'Clientes Acima da Média',
    'Liste os clientes cujo saldo é superior à média geral de todos os clientes. Retorne nome e saldo, ordenados pelo saldo de forma decrescente.',
    'Liste os clientes com saldo acima da média, do maior para o menor saldo.',
    'SELECT name, balance FROM clients WHERE balance > (SELECT AVG(balance) FROM clients) ORDER BY balance DESC;',
    '
        CREATE TABLE clients (
            id      SERIAL PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            balance NUMERIC(12, 2) NOT NULL
        );
    ',
    '
        INSERT INTO clients (name, balance) VALUES
            (''Alice Ferreira'',  15000.00),
            (''Bruno Souza'',      3200.00),
            (''Carla Nunes'',     27500.00),
            (''Diego Alves'',      8900.00),
            (''Elena Castro'',    42000.00);
    ',
    ARRAY['SELECT', 'WHERE', 'AVG', 'SUBQUERY', 'ORDER BY'],
    500,
    '[
        {"name": "Elena Castro",   "balance": 42000.00},
        {"name": "Carla Nunes",    "balance": 27500.00},
        {"name": "Alice Ferreira", "balance": 15000.00}
    ]',
    TRUE,
    'FINANCE',
    'EXPERT'
),

-- MISSION 8: BEGINNER / FINANCE (DML)
(
    gen_random_uuid(),
    'Corrigindo o Cadastro',
    'Um cliente teve seu saldo registrado incorretamente. Atualize o saldo de ''Bruno Souza'' para 5000.00 e em seguida liste todos os clientes.',
    'Atualize o saldo de Bruno Souza para 5000.00 e liste todos os clientes.',
    'UPDATE clients SET balance = 5000.00 WHERE name = ''Bruno Souza''; SELECT * FROM clients;',
    '
        CREATE TABLE clients (
            id      SERIAL PRIMARY KEY,
            name    VARCHAR(100) NOT NULL,
            balance NUMERIC(12, 2) NOT NULL
        );
    ',
    '
        INSERT INTO clients (name, balance) VALUES
            (''Alice Ferreira'',  15000.00),
            (''Bruno Souza'',      3200.00),
            (''Carla Nunes'',     27500.00);
    ',
    ARRAY['UPDATE', 'WHERE', 'SELECT'],
    150,
    '[
        {"id": 1, "name": "Alice Ferreira", "balance": 15000.00},
        {"id": 2, "name": "Bruno Souza",    "balance": 5000.00},
        {"id": 3, "name": "Carla Nunes",    "balance": 27500.00}
    ]',
    FALSE,
    'FINANCE',
    'BEGINNER'
);