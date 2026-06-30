
-- Themes
INSERT INTO themes (id, name, description, emoji) VALUES
    ('b0000001-0000-0000-0000-000000000001', 'ASTRONOMY',     'Explore the cosmos with SQL',     '🌌'),
    ('b0000002-0000-0000-0000-000000000002', 'BIOLOGY',       'Life sciences and data',        '🧬'),
    ('b0000003-0000-0000-0000-000000000003', 'CRIMINAL',      'Investigation and mystery',     '🔍'),
    ('b0000004-0000-0000-0000-000000000004', 'CYBERSECURITY', 'Digital security and hacking',  '🛡️'),
    ('b0000005-0000-0000-0000-000000000005', 'FINANCE',       'Finance and accounting',        '💰')
ON CONFLICT (id) DO NOTHING;

-- Scenarios
INSERT INTO scenarios (id, title, description, theme_id, required_level) VALUES
    ('00000000-0000-0000-0000-0000000000a1',
     'Night at the Blue Moon',
     'It''s 3 AM and Detective Estranho has just arrived at the Blue Moon Cabaret. A body was found in the alley, and the clues are in the register book. As you investigate, you discover this night holds more secrets than a simple murder — a web of lies involving patrons, interrogations, and alibis that don''t hold up.',
     'b0000003-0000-0000-0000-000000000003',
     1),
    ('00000000-0000-0000-0000-0000000000a2',
     'The Mendes & Sons Affair',
     'The Mendes & Sons accounting scandal unfolds as Detective Estranho follows the money trail from suspicious corporate accounts to a network of money laundering across multiple bank branches.',
     'b0000005-0000-0000-0000-000000000005',
     2)
ON CONFLICT (id) DO NOTHING;

-- Techniques
INSERT INTO techniques (id, name) VALUES
    ('c0000001-0000-0000-0000-000000000001', 'SELECT'),
    ('c0000002-0000-0000-0000-000000000002', 'WHERE'),
    ('c0000003-0000-0000-0000-000000000003', 'INNER JOIN'),
    ('c0000004-0000-0000-0000-000000000004', 'ORDER BY'),
    ('c0000005-0000-0000-0000-000000000005', 'GROUP BY'),
    ('c0000006-0000-0000-0000-000000000006', 'HAVING'),
    ('c0000007-0000-0000-0000-000000000007', 'SUM'),
    ('c0000008-0000-0000-0000-000000000008', 'AVG'),
    ('c0000009-0000-0000-0000-000000000009', 'COUNT'),
    ('c000000a-0000-0000-0000-00000000000a', 'UPDATE'),
    ('c000000b-0000-0000-0000-00000000000b', 'JOIN'),
    ('c000000c-0000-0000-0000-00000000000c', 'INSERT')
ON CONFLICT (id) DO NOTHING;

INSERT INTO missions (id, title, briefing, objective, hint, ddl_script, dml_script, xp_reward, expected_result, ordered, theme_id, difficulty, scenario_id, order_index)
VALUES

-- ========================================================================
-- MISSION 1 — BEGINNER / CRIMINAL
-- Teaches: SELECT
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000001',
    'The Last Drink',

    'IT''S 3 AM at the Blue Moon Cabaret. A body was found in the alley next door —
    a gunshot, no ID, no witnesses. Detective Estranho just walked in, hat pulled low
    over his eyes, a dead cigarette between his lips. He points at the register book
    on your desk without a word. You understand: he wants every patron from tonight.
    Names, aliases, what they drank, what time they left. Every line in that book
    could be the missing piece. The ashtray is overflowing. The clock never stops.',

    'Retrieve all records from the nightclub_log table. Show the id, patron_name, alias, drink, and left_at columns.',

    'Use SELECT * followed by the table name to retrieve all columns and rows from a table.',

    '
        CREATE TABLE nightclub_log (
            id          SERIAL PRIMARY KEY,
            patron_name VARCHAR(100) NOT NULL,
            alias       VARCHAR(100),
            drink       VARCHAR(100) NOT NULL,
            left_at     VARCHAR(5) NOT NULL
        );
    ',
    '
        INSERT INTO nightclub_log (patron_name, alias, drink, left_at) VALUES
            (''Charles Mendes'',  ''Big C'',        ''Whiskey'',         ''23:15''),
            (''Anna Lima'',       ''Little A'',     ''Martini'',         ''02:30''),
            (''Peter Costa'',     ''Leadfoot'',     ''Beer'',            ''00:45''),
            (''Julia Ramos'',     ''Jellybean'',    ''Gin and Tonic'',   ''01:20''),
            (''Michael Torres'',  ''Mick'',          ''Sparkling Water'', ''22:50'');
    ',
    100,
    '[
        {"id": 1, "patron_name": "Charles Mendes",  "alias": "Big C",        "drink": "Whiskey",         "left_at": "23:15"},
        {"id": 2, "patron_name": "Anna Lima",       "alias": "Little A",     "drink": "Martini",         "left_at": "02:30"},
        {"id": 3, "patron_name": "Peter Costa",     "alias": "Leadfoot",     "drink": "Beer",            "left_at": "00:45"},
        {"id": 4, "patron_name": "Julia Ramos",     "alias": "Jellybean",    "drink": "Gin and Tonic",   "left_at": "01:20"},
        {"id": 5, "patron_name": "Michael Torres",  "alias": "Mick",          "drink": "Sparkling Water", "left_at": "22:50"}
    ]',
    FALSE,
    'b0000003-0000-0000-0000-000000000003',
    'BEGINNER',
    '00000000-0000-0000-0000-0000000000a1',
    1
),

-- ========================================================================
-- MISSION 2 — BEGINNER / CRIMINAL
-- Teaches: SELECT, WHERE
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000002',
    'Suspicious Dawn',

    'The coroner estimates the death occurred between 1 AM and 3 AM. Detective Estranho
    leans over your desk — his gaze heavy, voice hoarse from cigarettes and coffee.
    "Who was still here after midnight?" He bites down on a toothpick.
    "The body didn''t walk in alone. Someone saw something. Someone knows something.
    Get me the names of everyone who stayed late — and make it quick." The cigarette
    smoke curls upward as he waits, motionless as a statue.',

    'Find all patrons who were still at the club after midnight (left_at greater than ''00:00''). Show the id, patron_name, alias, drink, and left_at columns.',

    'Add a WHERE clause to filter rows. Use the > operator to compare the left_at column against ''00:00''.',

    '
        CREATE TABLE nightclub_log (
            id          SERIAL PRIMARY KEY,
            patron_name VARCHAR(100) NOT NULL,
            alias       VARCHAR(100),
            drink       VARCHAR(100) NOT NULL,
            left_at     VARCHAR(5) NOT NULL
        );
    ',
    '
        INSERT INTO nightclub_log (patron_name, alias, drink, left_at) VALUES
            (''Charles Mendes'',  ''Big C'',        ''Whiskey'',         ''23:15''),
            (''Anna Lima'',       ''Little A'',     ''Martini'',         ''02:30''),
            (''Peter Costa'',     ''Leadfoot'',     ''Beer'',            ''00:45''),
            (''Julia Ramos'',     ''Jellybean'',    ''Gin and Tonic'',   ''01:20''),
            (''Michael Torres'',  ''Mick'',          ''Sparkling Water'', ''22:50'');
    ',
    100,
    '[
        {"id": 2, "patron_name": "Anna Lima",    "alias": "Little A",  "drink": "Martini",       "left_at": "02:30"},
        {"id": 3, "patron_name": "Peter Costa",  "alias": "Leadfoot",  "drink": "Beer",          "left_at": "00:45"},
        {"id": 4, "patron_name": "Julia Ramos",  "alias": "Jellybean", "drink": "Gin and Tonic", "left_at": "01:20"}
    ]',
    FALSE,
    'b0000003-0000-0000-0000-000000000003',
    'BEGINNER',
    '00000000-0000-0000-0000-0000000000a1',
    2
),

-- ========================================================================
-- MISSION 3 — INTERMEDIATE / CRIMINAL
-- Teaches: SELECT, INNER JOIN
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000003',
    'Web of Lies',

    'Three people were called to the station for questioning. Detective Estranho
    listened to each in silence, smoking behind his desk, eyes half-closed. Now he
    stares at you. "Cross-reference the club register with the depositions," he growls,
    turning to the dark window. "Who was interrogated and who was left out? The answers
    are at the intersection. And I don''t want guesses — I want data." The night is
    far from over, and the truth lies somewhere between two tables.',

    'Find which Blue Moon patrons were called for questioning. Show all columns from the nightclub_log table (id, patron_name, alias, drink, left_at).',

    'Use an INNER JOIN to combine nightclub_log with interrogations on the patron_name column.',

    '
        CREATE TABLE nightclub_log (
            id          SERIAL PRIMARY KEY,
            patron_name VARCHAR(100) NOT NULL,
            alias       VARCHAR(100),
            drink       VARCHAR(100) NOT NULL,
            left_at     VARCHAR(5) NOT NULL
        );
        CREATE TABLE interrogations (
            id           SERIAL PRIMARY KEY,
            patron_name  VARCHAR(100) NOT NULL,
            statement    TEXT NOT NULL,
            verdict      VARCHAR(50) NOT NULL
        );
    ',
    '
        INSERT INTO nightclub_log (patron_name, alias, drink, left_at) VALUES
            (''Charles Mendes'', ''Big C'',        ''Whiskey'',         ''23:15''),
            (''Anna Lima'',      ''Little A'',     ''Martini'',         ''02:30''),
            (''Peter Costa'',    ''Leadfoot'',     ''Beer'',            ''00:45''),
            (''Julia Ramos'',    ''Jellybean'',    ''Gin and Tonic'',   ''01:20'');
        INSERT INTO interrogations (patron_name, statement, verdict) VALUES
            (''Anna Lima'',   ''Claimed to be in the bathroom at the time of the crime.'', ''Inconclusive''),
            (''Peter Costa'', ''Reported seeing a man in a hat leaving through the alley.'', ''Truthful''),
            (''Julia Ramos'', ''Trembled when asked about the time.'', ''Inconclusive'');
    ',
    200,
    '[
        {"id": 2, "patron_name": "Anna Lima",   "alias": "Little A",  "drink": "Martini",       "left_at": "02:30"},
        {"id": 3, "patron_name": "Peter Costa", "alias": "Leadfoot",  "drink": "Beer",          "left_at": "00:45"},
        {"id": 4, "patron_name": "Julia Ramos", "alias": "Jellybean", "drink": "Gin and Tonic", "left_at": "01:20"}
    ]',
    FALSE,
    'b0000003-0000-0000-0000-000000000003',
    'INTERMEDIATE',
    '00000000-0000-0000-0000-0000000000a1',
    3
),

-- ========================================================================
-- MISSION 4 — BEGINNER / FINANCE
-- Teaches: SELECT, WHERE (boolean)
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000004',
    'The Hawk and the Vulture',

    'Mendes & Sons started the morning in turmoil. The chief accountant, Mr. Valentim,
    is pale as candle wax, clutching a stack of papers with trembling hands.
    "Someone embezzled nearly half a million!" he stammers. Detective Estranho — who
    never sleeps — is already there, coffee cooling in his hand. "I want to see every
    active account. Names, departments, balances. We can''t start a treasure hunt
    without knowing where to look." He tips the cup and downs the cold coffee in one gulp.',

    'List all active accounts (is_active = TRUE) from the company_accounts table. Show the id, holder_name, department, balance, and is_active columns.',

    'Add a WHERE clause filtering for is_active = TRUE. Boolean columns can be compared directly to TRUE.',

    '
        CREATE TABLE company_accounts (
            id            SERIAL PRIMARY KEY,
            holder_name   VARCHAR(100) NOT NULL,
            department    VARCHAR(100) NOT NULL,
            balance       NUMERIC(14,2) NOT NULL,
            is_active     BOOLEAN NOT NULL
        );
    ',
    '
        INSERT INTO company_accounts (holder_name, department, balance, is_active) VALUES
            (''Mendes & Sons Ltd.'',       ''Operations'', 487250.00, TRUE),
            (''Rapidão Transport S.A.'',   ''Logistics'', 1230450.00, TRUE),
            (''Aurora Distributors'',       ''Supplies'',  823700.00, TRUE),
            (''Central Accounting'',        ''Services'',   312000.00, TRUE),
            (''Inactive Warehouse'',        ''Closed'',         500.00, FALSE);
    ',
    100,
    '[
        {"id": 1, "holder_name": "Mendes & Sons Ltd.",       "department": "Operations", "balance": 487250.00,  "is_active": true},
        {"id": 2, "holder_name": "Rapidão Transport S.A.",   "department": "Logistics",  "balance": 1230450.00, "is_active": true},
        {"id": 3, "holder_name": "Aurora Distributors",       "department": "Supplies",  "balance": 823700.00,  "is_active": true},
        {"id": 4, "holder_name": "Central Accounting",        "department": "Services",  "balance": 312000.00,  "is_active": true}
    ]',
    FALSE,
    'b0000005-0000-0000-0000-000000000005',
    'BEGINNER',
    '00000000-0000-0000-0000-0000000000a2',
    1
),

-- ========================================================================
-- MISSION 5 — INTERMEDIATE / ASTRONOMY
-- Teaches: SELECT, ORDER BY
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000005',
    'Signals from the Cosmos',

    'The radio telescope picked up something inexplicable. A sequence of electromagnetic
    pulses coming from different star systems — some known, others not even on the
    charts. Chief astronomer Dr. Tavares is tense, eyes fixed on the spectrograph.
    "Could be satellite interference," she murmurs, "or it could be the discovery of
    the century." She turns to you: "Sort all readings by frequency, from lowest to
    highest. I need to see patterns. If it''s intelligent, it will repeat." The control
    room silence is broken only by the hum of servers.',

    'List all telescope readings ordered by frequency_mhz in ascending order. Show the id, star_system, signal_type, frequency_mhz, strength, and recorded_at columns.',

    'Append ORDER BY followed by the column name. Use ASC to sort from lowest to highest.',

    '
        CREATE TABLE telescope_readings (
            id            SERIAL PRIMARY KEY,
            star_system   VARCHAR(100) NOT NULL,
            signal_type   VARCHAR(50) NOT NULL,
            frequency_mhz NUMERIC(10,2) NOT NULL,
            strength      NUMERIC(3,1) NOT NULL,
            recorded_at   VARCHAR(19) NOT NULL
        );
    ',
    '
        INSERT INTO telescope_readings (star_system, signal_type, frequency_mhz, strength, recorded_at) VALUES
            (''Alpha Centauri'',  ''Radio'',    1420.00, 8.5, ''2025-03-15 22:30:00''),
            (''Betelgeuse'',      ''Infrared'', 350.50, 4.2, ''2025-03-15 22:35:00''),
            (''Proxima Centauri'', ''Radio'',    1420.05, 9.1, ''2025-03-15 22:40:00''),
            (''Sirius'',          ''Optical'',  520.75, 6.8, ''2025-03-15 22:45:00''),
            (''Andromeda'',       ''Radio'',    1420.10, 7.3, ''2025-03-15 22:50:00''),
            (''Vega'',            ''Infrared'', 310.20, 3.9, ''2025-03-15 22:55:00''),
            (''Alpha Centauri'',  ''Optical'',  480.00, 7.8, ''2025-03-15 23:00:00'');
    ',
    200,
    '[
        {"id": 6, "star_system": "Vega",            "signal_type": "Infrared", "frequency_mhz": 310.20, "strength": 3.9, "recorded_at": "2025-03-15 22:55:00"},
        {"id": 2, "star_system": "Betelgeuse",      "signal_type": "Infrared", "frequency_mhz": 350.50, "strength": 4.2, "recorded_at": "2025-03-15 22:35:00"},
        {"id": 7, "star_system": "Alpha Centauri",  "signal_type": "Optical",  "frequency_mhz": 480.00, "strength": 7.8, "recorded_at": "2025-03-15 23:00:00"},
        {"id": 4, "star_system": "Sirius",          "signal_type": "Optical",  "frequency_mhz": 520.75, "strength": 6.8, "recorded_at": "2025-03-15 22:45:00"},
        {"id": 1, "star_system": "Alpha Centauri",  "signal_type": "Radio",    "frequency_mhz": 1420.00,"strength": 8.5, "recorded_at": "2025-03-15 22:30:00"},
        {"id": 3, "star_system": "Proxima Centauri","signal_type": "Radio",    "frequency_mhz": 1420.05,"strength": 9.1, "recorded_at": "2025-03-15 22:40:00"},
        {"id": 5, "star_system": "Andromeda",       "signal_type": "Radio",    "frequency_mhz": 1420.10,"strength": 7.3, "recorded_at": "2025-03-15 22:50:00"}
    ]',
    TRUE,
    'b0000001-0000-0000-0000-000000000001',
    'INTERMEDIATE',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 6 — ADVANCED / FINANCE
-- Teaches: SELECT, GROUP BY, HAVING, SUM, AVG
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000006',
    'The Underworld''s Fortune',

    'The embezzlement probe at Mendes & Sons revealed a suspicious pattern: certain
    bank branches hold balances far above expectations. The prosecutor — a sharp-eyed
    woman who hasn''t slept in two days — slams a folder on your desk. "Group by branch.
    I want the total balance and the average for each. And I only want to see branches
    where the average exceeds seven hundred thousand. That''s where the dirty money
    smells strongest." She crosses her arms. "Let''s find out who''s been laundering
    money under cover of night."',

    'Calculate the total balance and the average balance per bank branch. Show only branches with an average balance greater than 700,000. Display the branch, total, and average columns.',

    'Use GROUP BY branch with SUM(balance) AS total and AVG(balance) AS average. Filter grouped results with HAVING, not WHERE.',

    '
        CREATE TABLE accounts (
            id          SERIAL PRIMARY KEY,
            holder_name VARCHAR(100) NOT NULL,
            branch      VARCHAR(100) NOT NULL,
            balance     NUMERIC(14,2) NOT NULL
        );
    ',
    '
        INSERT INTO accounts (holder_name, branch, balance) VALUES
            (''Mendes & Sons Ltd.'',     ''Downtown'',      950000.00),
            (''Rapidão Transport S.A.'', ''Downtown'',     1250000.00),
            (''Aurora Distributors'',     ''Downtown'',      180000.00),
            (''Tech Solutions Ltd.'',     ''Downtown'',     2200000.00),
            (''Auto Parts Brazil'',       ''South District'', 720000.00),
            (''Nova Era Realty'',         ''South District'', 860000.00),
            (''Popular Market'',          ''North District'', 320000.00),
            (''Health Pharmacy'',         ''North District'', 450000.00);
    ',
    350,
    '[
        {"branch": "Downtown",       "total": 4580000.00, "average": 1145000.00},
        {"branch": "South District", "total": 1580000.00, "average": 790000.00}
    ]',
    FALSE,
    'b0000005-0000-0000-0000-000000000005',
    'ADVANCED',
    '00000000-0000-0000-0000-0000000000a2',
    2
),

-- ========================================================================
-- MISSION 7 — ADVANCED / ASTRONOMY
-- Teaches: SELECT, WHERE, GROUP BY, HAVING, COUNT
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000007',
    'Stellar Map',

    'Dr. Tavares is building an updated astrophysical map. She spent weeks cataloging
    celestial bodies and now needs a summary. "I''ve classified dozens of objects —
    stars, exoplanets, nebulae, and more," she says, rubbing her tired eyes. "I need
    to know how many of each type were discovered before 1950 — before the space age,
    when everything was done with telescopes and patience. And I only want types with
    more than one body. Single-item categories are exceptions, not patterns." She leans
    back in her chair and waits.',

    'Count how many celestial bodies of each type were discovered before 1950. Show only types with more than 1 body. Display the body_type and count columns.',

    'Filter pre-1950 discoveries with WHERE, GROUP BY body_type with COUNT(*) AS count, then HAVING COUNT(*) > 1.',

    '
        CREATE TABLE celestial_bodies (
            id              SERIAL PRIMARY KEY,
            name            VARCHAR(100) NOT NULL,
            body_type       VARCHAR(50) NOT NULL,
            distance_ly     NUMERIC(12,2),
            discovered_year INTEGER NOT NULL,
            is_anomalous    BOOLEAN NOT NULL DEFAULT FALSE
        );
    ',
    '
        INSERT INTO celestial_bodies (name, body_type, distance_ly, discovered_year, is_anomalous) VALUES
            (''Proxima Centauri b'', ''Exoplanet'',   4.25,       2016, FALSE),
            (''Betelgeuse'',         ''Star'',        642.50,     1835, FALSE),
            (''Andromeda XXXI'',     ''Galaxy'',      2500000.00, 1923, FALSE),
            (''Kepler-442b'',        ''Exoplanet'',   1206.00,    2015, FALSE),
            (''Sirius'',             ''Star'',        8.60,       1750, FALSE),
            (''Crab Nebula'',        ''Nebula'',      6500.00,    1731, FALSE),
            (''TRAPPIST-1e'',        ''Exoplanet'',   39.50,      2017, TRUE),
            (''Halley'',             ''Comet'',       0.00,       1705, FALSE),
            (''Vela Pulsar'',        ''Star'',        959.00,     1968, FALSE),
            (''M87*'',               ''Black Hole'',  55000000.00, 2019, TRUE);
    ',
    350,
    '[
        {"body_type": "Star", "count": 2}
    ]',
    FALSE,
    'b0000001-0000-0000-0000-000000000001',
    'ADVANCED',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 8 — EXPERT / CYBERSECURITY
-- Teaches: SELECT, WHERE, GROUP BY, HAVING, COUNT, ORDER BY
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000008',
    'The Ghost in the Matrix',

    'The clock reads 3:14 AM when the alarm goes off at NexusCorp. The monitoring
    center screens flash red. The CTO — a balding man with deep dark circles —
    grips the mouse with a trembling hand: "Someone is accessing classified data
    from inside the network — in the dead of night, when nobody should be here."
    He points at the logs. "Internal IPs. Sensitive resource access. Witching hour.
    More than three suspicious accesses per employee. Find out who they are."
    The only sound in the room is the hum of servers and your racing heart as you
    stare at the screen.',

    'Identify employees who performed more than 3 suspicious accesses (is_suspicious = TRUE). Show the employee_name and access_count. Order results alphabetically by name.',

    'Filter suspicious accesses with WHERE is_suspicious = TRUE, GROUP BY employee_name with COUNT(*) AS access_count. Use HAVING COUNT(*) > 3. Order alphabetically with ORDER BY employee_name.',

    '
        CREATE TABLE access_logs (
            id               SERIAL PRIMARY KEY,
            employee_name    VARCHAR(100) NOT NULL,
            ip_address       VARCHAR(15) NOT NULL,
            resource_accessed VARCHAR(100) NOT NULL,
            access_time      VARCHAR(5) NOT NULL,
            is_suspicious    BOOLEAN NOT NULL DEFAULT FALSE
        );
    ',
    '
        INSERT INTO access_logs (employee_name, ip_address, resource_accessed, access_time, is_suspicious) VALUES
            (''Alex Silva'',  ''10.0.0.15'', ''/financial/balance'',   ''03:14'', TRUE),
            (''Alex Silva'',  ''10.0.0.15'', ''/hr/personal_data'',    ''03:16'', TRUE),
            (''Alex Silva'',  ''10.0.0.15'', ''/admin/users'',         ''03:18'', TRUE),
            (''Alex Silva'',  ''10.0.0.15'', ''/servers/config'',      ''03:20'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/admin/backup'',        ''02:00'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/admin/logs'',          ''02:05'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/financial/balance'',   ''02:10'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/admin/users'',         ''02:15'', TRUE),
            (''Maria Santos'', ''192.168.1.10'', ''/dashboard'',       ''09:00'', FALSE),
            (''Maria Santos'', ''192.168.1.10'', ''/reports'',         ''09:15'', FALSE),
            (''Carla Dias'',  ''192.168.1.50'', ''/email'',            ''08:30'', FALSE);
    ',
    500,
    '[
        {"employee_name": "Alex Silva", "access_count": 4},
        {"employee_name": "João Lima",  "access_count": 4}
    ]',
    FALSE,
    'b0000004-0000-0000-0000-000000000004',
    'EXPERT',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 9 — INTERMEDIATE / BIOLOGY
-- Teaches: SELECT, INNER JOIN, WHERE
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000009',
    'The Outbreak',

    'The Rapid Response Unit has been mobilized. An outbreak of unknown origin is
    spreading through the Lapa neighborhood at an alarming rate. Dr. Moraes is
    exhausted — wrinkled lab coat, graying hair escaping her bun — but her eyes
    still burn with fire. "Patients with severe symptoms need immediate isolation.
    Severity above 7," she says, turning to the monitor. "Cross-reference the patient
    records with the symptom reports. I need names, ages, and which symptoms each one
    presented. Time is life now, and I''m not losing anyone else."',

    'Find patients who have at least one symptom with severity greater than 7. Show the patient''s name, age, and symptom_name columns.',

    'Use INNER JOIN to combine patients with symptoms on patient_id, then filter with WHERE for severity > 7.',

    '
        CREATE TABLE patients (
            id                 SERIAL PRIMARY KEY,
            name               VARCHAR(100) NOT NULL,
            age                INTEGER NOT NULL,
            neighborhood       VARCHAR(100) NOT NULL,
            vaccination_status BOOLEAN NOT NULL
        );
        CREATE TABLE symptoms (
            id           SERIAL PRIMARY KEY,
            patient_id   INTEGER NOT NULL REFERENCES patients(id),
            symptom_name VARCHAR(100) NOT NULL,
            severity     INTEGER NOT NULL CHECK (severity >= 1 AND severity <= 10)
        );
    ',
    '
        INSERT INTO patients (name, age, neighborhood, vaccination_status) VALUES
            (''Mr. George'',  67, ''Lapa'', TRUE),
            (''Mrs. Maria'',  72, ''Lapa'', TRUE),
            (''Claudio'',     34, ''Lapa'', FALSE),
            (''Renata'',      28, ''Lapa'', TRUE),
            (''Mr. Ambrose'', 81, ''Lapa'', FALSE);
        INSERT INTO symptoms (patient_id, symptom_name, severity) VALUES
            (1, ''High Fever'',           9),
            (1, ''Dry Cough'',            6),
            (1, ''Shortness of Breath'',  8),
            (2, ''High Fever'',           8),
            (2, ''Mental Confusion'',     7),
            (3, ''High Fever'',           9),
            (3, ''Shortness of Breath'',  9),
            (3, ''Nausea'',              5),
            (3, ''Headache'',            6),
            (4, ''Sore Throat'',          4),
            (5, ''High Fever'',           9),
            (5, ''Dry Cough'',            7),
            (5, ''Shortness of Breath'', 10),
            (5, ''Cyanosis'',            9),
            (5, ''Seizure'',            10);
    ',
    250,
    '[
        {"name": "Mr. George",  "age": 67, "symptom_name": "High Fever"},
        {"name": "Mr. George",  "age": 67, "symptom_name": "Shortness of Breath"},
        {"name": "Mrs. Maria",  "age": 72, "symptom_name": "High Fever"},
        {"name": "Claudio",     "age": 34, "symptom_name": "High Fever"},
        {"name": "Claudio",     "age": 34, "symptom_name": "Shortness of Breath"},
        {"name": "Mr. Ambrose", "age": 81, "symptom_name": "High Fever"},
        {"name": "Mr. Ambrose", "age": 81, "symptom_name": "Shortness of Breath"},
        {"name": "Mr. Ambrose", "age": 81, "symptom_name": "Cyanosis"},
        {"name": "Mr. Ambrose", "age": 81, "symptom_name": "Seizure"}
    ]',
    FALSE,
    'b0000002-0000-0000-0000-000000000002',
    'INTERMEDIATE',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 10 — EXPERT / BIOLOGY
-- Teaches: UPDATE, WHERE, SELECT (DML + DQL)
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000010',
    'A Cure in Drops',

    'The outbreak is spreading and the hospital pharmacy is running critically low.
    Dr. Moraes is pale, lips pressed tight. She holds a clipboard with the medication
    list, her voice faltering: "Several batches expired and nobody updated the system.
    Expired meds are worse than no meds at all — they can mask symptoms, cause adverse
    reactions, cost lives." She shoves the pen into your hand. "Zero out the stock
    for everything that expired before 2025. Then show me what''s left. We need to
    know what we actually have to fight with."',

    'Set the quantity to 0 for medications with an expiry_date before 2025-01-01. Then list all records from the medication_stock table to verify.',

    'Use an UPDATE statement with a WHERE clause filtering by expiry_date. Then run a SELECT to see the updated stock.',

    '
        CREATE TABLE medication_stock (
            id           SERIAL PRIMARY KEY,
            medication   VARCHAR(100) NOT NULL,
            batch        VARCHAR(50) NOT NULL,
            quantity     INTEGER NOT NULL,
            expiry_date  DATE NOT NULL
        );
    ',
    '
        INSERT INTO medication_stock (medication, batch, quantity, expiry_date) VALUES
            (''Ivermectin'',   ''IVM-23A'', 500,  ''2026-12-01''),
            (''Zanamivir'',    ''ZAN-22B'', 200,  ''2024-06-15''),
            (''Oseltamivir'',  ''OSL-21C'', 150,  ''2024-03-01''),
            (''Remdesivir'',   ''REM-24A'', 300,  ''2027-01-15''),
            (''Dipyrone'',     ''DIP-22A'', 1000, ''2025-11-30''),
            (''Paracetamol'',  ''PAR-20B'', 750,  ''2023-08-20'');
    ',
    400,
    '[
        {"id": 1, "medication": "Ivermectin",  "batch": "IVM-23A", "quantity": 500,  "expiry_date": "2026-12-01"},
        {"id": 2, "medication": "Zanamivir",    "batch": "ZAN-22B", "quantity": 0,   "expiry_date": "2024-06-15"},
        {"id": 3, "medication": "Oseltamivir",  "batch": "OSL-21C", "quantity": 0,   "expiry_date": "2024-03-01"},
        {"id": 4, "medication": "Remdesivir",   "batch": "REM-24A", "quantity": 300, "expiry_date": "2027-01-15"},
        {"id": 5, "medication": "Dipyrone",     "batch": "DIP-22A", "quantity": 1000,"expiry_date": "2025-11-30"},
        {"id": 6, "medication": "Paracetamol",  "batch": "PAR-20B", "quantity": 0,   "expiry_date": "2023-08-20"}
    ]',
    FALSE,
    'b0000002-0000-0000-0000-000000000002',
    'EXPERT',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 11 — EXPERT / CYBERSECURITY
-- Teaches: JOIN (3 tables), WHERE, GROUP BY, HAVING, COUNT, ORDER BY
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000011',
    'Data Exfiltration',

    'The CyberCrime unit intercepted chatter about a massive data leak at AethelCorp. Commander Torres spreads printouts across your desk — access logs, employee records, and a list of classified files. "Three tables," she says, tapping each printout. "Employees. File access logs. Confidential files. Someone on the inside accessed more than three top-secret files. That''s not a mistake — that''s a coordinated exfiltration." She crosses her arms. "Find them before the data hits the dark web."',

    'Find employees who accessed more than 3 top-secret files (classification = ''Top Secret''). Show the employee''s name, department, and access_count columns. Order results by access_count descending, then alphabetically by name.',

    'Join all three tables with INNER JOIN: employees → file_access → confidential_files. Filter with WHERE cf.classification = ''Top Secret''. Group by employee columns (id, name, department), count with COUNT(*), filter groups with HAVING COUNT(*) > 3, then ORDER BY access_count DESC, name.',

    '
        CREATE TABLE employees (
            id              SERIAL PRIMARY KEY,
            name            VARCHAR(100) NOT NULL,
            department      VARCHAR(50) NOT NULL,
            clearance_level VARCHAR(20) NOT NULL
        );
        CREATE TABLE confidential_files (
            id             SERIAL PRIMARY KEY,
            filename       VARCHAR(200) NOT NULL,
            classification VARCHAR(50) NOT NULL
        );
        CREATE TABLE file_access (
            id          SERIAL PRIMARY KEY,
            employee_id INTEGER NOT NULL REFERENCES employees(id),
            file_id     INTEGER NOT NULL REFERENCES confidential_files(id),
            access_time TIMESTAMP NOT NULL,
            action      VARCHAR(20) NOT NULL
        );
    ',
    '
        INSERT INTO employees (name, department, clearance_level) VALUES
            (''John Carter'',  ''Engineering'', ''Level 3''),
            (''Mary Wilson'',  ''Engineering'', ''Level 2''),
            (''Alex Rivera'',  ''HR'',          ''Level 2''),
            (''Lisa Chen'',    ''IT Security'', ''Level 4''),
            (''Bob Torres'',   ''IT Security'', ''Level 3''),
            (''Diana Park'',   ''Legal'',       ''Level 2''),
            (''Steve Murphy'', ''Executive'',   ''Level 5'');
        INSERT INTO confidential_files (filename, classification) VALUES
            (''financial_report_2026.pdf'', ''Top Secret''),
            (''merger_plans.docx'',         ''Top Secret''),
            (''employee_salaries.xlsx'',    ''Confidential''),
            (''source_code_vault.zip'',     ''Top Secret''),
            (''board_minutes.pdf'',         ''Top Secret''),
            (''marketing_plan.pptx'',       ''Public''),
            (''patent_applications.pdf'',   ''Confidential'');
        INSERT INTO file_access (employee_id, file_id, access_time, action) VALUES
            (1, 1, ''2026-05-10 02:14:00'', ''READ''),
            (1, 4, ''2026-05-10 02:16:00'', ''READ''),
            (1, 2, ''2026-05-10 02:18:00'', ''DOWNLOAD''),
            (1, 5, ''2026-05-10 02:20:00'', ''READ''),
            (1, 6, ''2026-05-10 09:00:00'', ''READ''),
            (2, 6, ''2026-05-09 10:00:00'', ''READ''),
            (2, 3, ''2026-05-08 11:00:00'', ''READ''),
            (3, 3, ''2026-05-07 14:30:00'', ''READ''),
            (3, 6, ''2026-05-07 15:00:00'', ''READ''),
            (4, 1, ''2026-05-09 03:00:00'', ''READ''),
            (4, 4, ''2026-05-09 03:05:00'', ''READ''),
            (4, 2, ''2026-05-09 03:10:00'', ''DOWNLOAD''),
            (4, 7, ''2026-05-09 03:15:00'', ''READ''),
            (5, 1, ''2026-05-08 01:00:00'', ''READ''),
            (5, 2, ''2026-05-08 01:30:00'', ''READ''),
            (6, 5, ''2026-05-07 22:00:00'', ''READ''),
            (6, 1, ''2026-05-07 22:15:00'', ''READ''),
            (7, 1, ''2026-05-10 04:00:00'', ''DOWNLOAD''),
            (7, 2, ''2026-05-10 04:05:00'', ''DOWNLOAD''),
            (7, 4, ''2026-05-10 04:10:00'', ''DOWNLOAD''),
            (7, 5, ''2026-05-10 04:15:00'', ''DOWNLOAD''),
            (7, 3, ''2026-05-10 04:20:00'', ''READ'');
    ',
    600,
    '[
        {"name": "John Carter",  "department": "Engineering", "access_count": 4},
        {"name": "Steve Murphy", "department": "Executive",   "access_count": 4}
    ]',
    FALSE,
    'b0000004-0000-0000-0000-000000000004',
    'EXPERT',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 12 — BEGINNER / ASTRONOMY
-- Teaches: INSERT, SELECT
-- Expected solution:
--   INSERT INTO asteroid_catalog (designation, diameter_km, discovery_year, is_potentially_hazardous)
--   VALUES ('2026-XK1', 1.20, 2026, TRUE);
--   SELECT * FROM asteroid_catalog;
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000012',
    'The New Discovery',

    'Dr. Tavares bursts into the control room at 2 AM, her eyes wide with excitement — something she hasn''t felt in years. The automated survey telescope detected an unknown object in the main asteroid belt, and the initial readings are intriguing. "It''s a new one," she whispers, pulling up the orbital data on the main screen. "Designation 2026-XK1. About 1.2 kilometers across. And it''s flagged as potentially hazardous." She turns to you, a faint smile crossing her exhausted face. "Add it to the catalog. We can''t afford to lose track of a rock that could wipe out a city."',

    'Add the newly discovered asteroid to the asteroid_catalog table. Insert a row with designation = ''2026-XK1'', diameter_km = 1.20, discovery_year = 2026, and is_potentially_hazardous = TRUE. Then select all rows to verify.',

    'Use INSERT INTO table_name (column1, column2, ...) VALUES (value1, value2, ...). The id column is automatically generated. After inserting, run SELECT * FROM asteroid_catalog to see all records.',

    '
        CREATE TABLE asteroid_catalog (
            id                      SERIAL PRIMARY KEY,
            designation             VARCHAR(50) NOT NULL,
            diameter_km             NUMERIC(8,2) NOT NULL,
            discovery_year          INTEGER NOT NULL,
            is_potentially_hazardous BOOLEAN NOT NULL DEFAULT FALSE
        );
    ',
    '
        INSERT INTO asteroid_catalog (designation, diameter_km, discovery_year, is_potentially_hazardous) VALUES
            (''Ceres'',    939.40, 1801, FALSE),
            (''Vesta'',    525.40, 1807, FALSE),
            (''Apophis'',    0.37, 2004, TRUE);
    ',
    100,
    '[
        {"id": 1, "designation": "Ceres",     "diameter_km": 939.40, "discovery_year": 1801, "is_potentially_hazardous": false},
        {"id": 2, "designation": "Vesta",     "diameter_km": 525.40, "discovery_year": 1807, "is_potentially_hazardous": false},
        {"id": 3, "designation": "Apophis",   "diameter_km": 0.37,   "discovery_year": 2004, "is_potentially_hazardous": true},
        {"id": 4, "designation": "2026-XK1",  "diameter_km": 1.20,   "discovery_year": 2026, "is_potentially_hazardous": true}
    ]',
    FALSE,
    'b0000001-0000-0000-0000-000000000001',
    'BEGINNER',
    NULL,
    NULL
),

-- ========================================================================
-- MISSION 13 — INTERMEDIATE / CYBERSECURITY
-- Teaches: UPDATE, WHERE, SELECT (subquery)
-- Expected solution:
--   UPDATE employee_credentials
--   SET access_level = 'Level 1'
--   WHERE employee_name IN (SELECT employee_name FROM breach_report);
--   SELECT * FROM employee_credentials;
-- ========================================================================
(
    '00000000-0000-0000-0000-000000000013',
    'Access Revoked',

    'The forensic analysis of the NexusCorp breach is complete. Dra. Nakamura, head of IT Security, stares at the screen with bloodshot eyes, her knuckles white against the edge of the table. The breach report on your terminal lists compromised employees — names and the data they exposed. "Cross-reference with our employee credentials table," she says, her voice ice-cold. "Anyone on that list gets their access dropped to Level 1 immediately. No exceptions. No second chances. Then show me the full roster — I want everyone''s status at a glance."',

    'Lower the access level to ''Level 1'' for all employees whose names appear on the breach_report. Then list all rows from employee_credentials.',

    'Use UPDATE with a subquery in the WHERE clause: UPDATE ... SET ... WHERE employee_name IN (SELECT employee_name FROM breach_report). Then run SELECT * FROM employee_credentials.',

    '
        CREATE TABLE employee_credentials (
            id             SERIAL PRIMARY KEY,
            employee_name  VARCHAR(100) NOT NULL,
            department     VARCHAR(50) NOT NULL,
            access_level   VARCHAR(20) NOT NULL,
            is_compromised BOOLEAN NOT NULL DEFAULT FALSE
        );
        CREATE TABLE breach_report (
            id             SERIAL PRIMARY KEY,
            employee_name  VARCHAR(100) NOT NULL,
            breached_data  VARCHAR(100) NOT NULL
        );
    ',
    '
        INSERT INTO employee_credentials (employee_name, department, access_level, is_compromised) VALUES
            (''Alex Silva'',   ''Engineering'', ''Level 3'', TRUE),
            (''João Lima'',    ''Engineering'', ''Level 2'', TRUE),
            (''Maria Santos'', ''HR'',          ''Level 2'', FALSE),
            (''Carla Dias'',   ''IT Security'', ''Level 4'', FALSE),
            (''Bob Torres'',   ''IT Security'', ''Level 3'', FALSE);
        INSERT INTO breach_report (employee_name, breached_data) VALUES
            (''Alex Silva'', ''/financial/balance, /admin/users''),
            (''João Lima'',  ''/admin/backup, /admin/logs'');
    ',
    250,
    '[
        {"id": 1, "employee_name": "Alex Silva",   "department": "Engineering", "access_level": "Level 1", "is_compromised": true},
        {"id": 2, "employee_name": "João Lima",    "department": "Engineering", "access_level": "Level 1", "is_compromised": true},
        {"id": 3, "employee_name": "Maria Santos", "department": "HR",          "access_level": "Level 2", "is_compromised": false},
        {"id": 4, "employee_name": "Carla Dias",   "department": "IT Security", "access_level": "Level 4", "is_compromised": false},
        {"id": 5, "employee_name": "Bob Torres",   "department": "IT Security", "access_level": "Level 3", "is_compromised": false}
    ]',
    FALSE,
    'b0000004-0000-0000-0000-000000000004',
    'INTERMEDIATE',
    NULL,
    NULL
);

-- Mission-Techniques associations
INSERT INTO mission_techniques (mission_id, technique_id) VALUES
    -- Mission 1: SELECT
    ('00000000-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001'),
    -- Mission 2: SELECT, WHERE
    ('00000000-0000-0000-0000-000000000002', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000002', 'c0000002-0000-0000-0000-000000000002'),
    -- Mission 3: SELECT, INNER JOIN
    ('00000000-0000-0000-0000-000000000003', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000003', 'c0000003-0000-0000-0000-000000000003'),
    -- Mission 4: SELECT, WHERE
    ('00000000-0000-0000-0000-000000000004', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000004', 'c0000002-0000-0000-0000-000000000002'),
    -- Mission 5: SELECT, ORDER BY
    ('00000000-0000-0000-0000-000000000005', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000005', 'c0000004-0000-0000-0000-000000000004'),
    -- Mission 6: SELECT, GROUP BY, HAVING, SUM, AVG
    ('00000000-0000-0000-0000-000000000006', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000006', 'c0000005-0000-0000-0000-000000000005'),
    ('00000000-0000-0000-0000-000000000006', 'c0000006-0000-0000-0000-000000000006'),
    ('00000000-0000-0000-0000-000000000006', 'c0000007-0000-0000-0000-000000000007'),
    ('00000000-0000-0000-0000-000000000006', 'c0000008-0000-0000-0000-000000000008'),
    -- Mission 7: SELECT, WHERE, GROUP BY, HAVING, COUNT
    ('00000000-0000-0000-0000-000000000007', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000007', 'c0000002-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000007', 'c0000005-0000-0000-0000-000000000005'),
    ('00000000-0000-0000-0000-000000000007', 'c0000006-0000-0000-0000-000000000006'),
    ('00000000-0000-0000-0000-000000000007', 'c0000009-0000-0000-0000-000000000009'),
    -- Mission 8: SELECT, WHERE, GROUP BY, HAVING, COUNT, ORDER BY
    ('00000000-0000-0000-0000-000000000008', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000008', 'c0000002-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000008', 'c0000005-0000-0000-0000-000000000005'),
    ('00000000-0000-0000-0000-000000000008', 'c0000006-0000-0000-0000-000000000006'),
    ('00000000-0000-0000-0000-000000000008', 'c0000009-0000-0000-0000-000000000009'),
    ('00000000-0000-0000-0000-000000000008', 'c0000004-0000-0000-0000-000000000004'),
    -- Mission 9: SELECT, INNER JOIN, WHERE
    ('00000000-0000-0000-0000-000000000009', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000009', 'c0000003-0000-0000-0000-000000000003'),
    ('00000000-0000-0000-0000-000000000009', 'c0000002-0000-0000-0000-000000000002'),
    -- Mission 10: UPDATE, WHERE, SELECT
    ('00000000-0000-0000-0000-000000000010', 'c000000a-0000-0000-0000-00000000000a'),
    ('00000000-0000-0000-0000-000000000010', 'c0000002-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000010', 'c0000001-0000-0000-0000-000000000001'),
    -- Mission 11: SELECT, JOIN, WHERE, GROUP BY, HAVING, COUNT, ORDER BY
    ('00000000-0000-0000-0000-000000000011', 'c0000001-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000011', 'c000000b-0000-0000-0000-00000000000b'),
    ('00000000-0000-0000-0000-000000000011', 'c0000002-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000011', 'c0000005-0000-0000-0000-000000000005'),
    ('00000000-0000-0000-0000-000000000011', 'c0000006-0000-0000-0000-000000000006'),
    ('00000000-0000-0000-0000-000000000011', 'c0000009-0000-0000-0000-000000000009'),
    ('00000000-0000-0000-0000-000000000011', 'c0000004-0000-0000-0000-000000000004'),
    -- Mission 12: INSERT, SELECT
    ('00000000-0000-0000-0000-000000000012', 'c000000c-0000-0000-0000-00000000000c'),
    ('00000000-0000-0000-0000-000000000012', 'c0000001-0000-0000-0000-000000000001'),
    -- Mission 13: UPDATE, WHERE, SELECT
    ('00000000-0000-0000-0000-000000000013', 'c000000a-0000-0000-0000-00000000000a'),
    ('00000000-0000-0000-0000-000000000013', 'c0000002-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000013', 'c0000001-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;
