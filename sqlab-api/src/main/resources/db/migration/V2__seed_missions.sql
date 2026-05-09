-- V2__seed_missions.sql
-- 10 creative missions with clear briefing (narrative) / objective (SQL task) distinction

INSERT INTO missions (id, title, briefing, objective, hint, ddl_script, dml_script, techniques, xp_reward, expected_result, ordered, theme, difficulty)
VALUES

-- ========================================================================
-- MISSION 1 — BEGINNER / CRIMINAL
-- Teaches: SELECT
-- ========================================================================
(
    gen_random_uuid(),
    'O Último Gole',

    'SÃO 3H DA MANHÃ no Blue Moon Cabaret. Um corpo foi encontrado no beco ao lado —
    um tiro, nenhum documento, nenhuma testemunha. O delegado Estranho acaba de chegar
    com o chapéu enterrado nos olhos e um cigarro apagado entre os lábios. Ele aponta
    para o livro de registro na sua mesa sem dizer uma palavra. Você entende: quer
    todos os frequentadores da noite. Nomes, apelidos, o que beberam, a hora que
    foram embora. Cada linha desse livro pode ser a peça que falta. O cinzeiro já
    transborda. O relógio não para.',

    'Liste todos os registros da tabela nightclub_log — nome, apelido, bebida e horário de saída.',

    'SELECT * FROM nightclub_log;',

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
            (''Carlos Mendes'', ''Carlão'',       ''Whisky'',       ''23:15''),
            (''Ana Lima'',      ''Aninha'',       ''Martini'',      ''02:30''),
            (''Pedro Costa'',   ''Pé de Chumbo'', ''Cerveja'',      ''00:45''),
            (''Julia Ramos'',   ''Jujuba'',       ''Gim Tônica'',   ''01:20''),
            (''Miguel Torres'', ''Mick'',          ''Água com Gás'', ''22:50'');
    ',
    ARRAY['SELECT'],
    100,
    '[
        {"id": 1, "patron_name": "Carlos Mendes", "alias": "Carlão",       "drink": "Whisky",       "left_at": "23:15"},
        {"id": 2, "patron_name": "Ana Lima",      "alias": "Aninha",       "drink": "Martini",      "left_at": "02:30"},
        {"id": 3, "patron_name": "Pedro Costa",   "alias": "Pé de Chumbo", "drink": "Cerveja",      "left_at": "00:45"},
        {"id": 4, "patron_name": "Julia Ramos",   "alias": "Jujuba",       "drink": "Gim Tônica",   "left_at": "01:20"},
        {"id": 5, "patron_name": "Miguel Torres", "alias": "Mick",          "drink": "Água com Gás", "left_at": "22:50"}
    ]',
    FALSE,
    'CRIMINAL',
    'BEGINNER'
),

-- ========================================================================
-- MISSION 2 — BEGINNER / CRIMINAL
-- Teaches: SELECT, WHERE
-- ========================================================================
(
    gen_random_uuid(),
    'Madrugada Suspeita',

    'O legista estima que a morte ocorreu entre 1h e 3h da manhã. O delegado Estranho
    se inclina sobre sua mesa — o olhar pesado, a voz rouca de cigarro e café.
    "Quem ainda estava aqui depois da meia-noite?" Ele aperta o palito na boca.
    "O corpo não apareceu sozinho. Alguém viu. Alguém sabe de algo. Me traz os
    nomes de todos que ficaram até mais tarde — e rápido." A fumaça do cigarro
    dele sobe em espiral enquanto ele espera, imóvel como uma estátua.',

    'Selecione todos os frequentadores que estavam no clube depois da meia-noite (left_at maior que ''00:00'').',

    'SELECT * FROM nightclub_log WHERE left_at > ''00:00'';',

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
            (''Carlos Mendes'', ''Carlão'',       ''Whisky'',       ''23:15''),
            (''Ana Lima'',      ''Aninha'',       ''Martini'',      ''02:30''),
            (''Pedro Costa'',   ''Pé de Chumbo'', ''Cerveja'',      ''00:45''),
            (''Julia Ramos'',   ''Jujuba'',       ''Gim Tônica'',   ''01:20''),
            (''Miguel Torres'', ''Mick'',          ''Água com Gás'', ''22:50'');
    ',
    ARRAY['SELECT', 'WHERE'],
    100,
    '[
        {"id": 2, "patron_name": "Ana Lima",    "alias": "Aninha",       "drink": "Martini",    "left_at": "02:30"},
        {"id": 3, "patron_name": "Pedro Costa", "alias": "Pé de Chumbo", "drink": "Cerveja",    "left_at": "00:45"},
        {"id": 4, "patron_name": "Julia Ramos", "alias": "Jujuba",       "drink": "Gim Tônica", "left_at": "01:20"}
    ]',
    FALSE,
    'CRIMINAL',
    'BEGINNER'
),

-- ========================================================================
-- MISSION 3 — INTERMEDIATE / CRIMINAL
-- Teaches: SELECT, INNER JOIN
-- ========================================================================
(
    gen_random_uuid(),
    'Teia de Mentiras',

    'Três pessoas foram chamadas para depor na delegacia. O delegado Estranho ouviu
    cada uma em silêncio, fumando atrás da mesa, os olhos semicerrados. Agora ele
    encara você. "Cruza a lista do clube com os depoimentos," ele rosna, virando-se
    para a janela escura. "Quem foi interrogado e quem ficou de fora? As respostas
    estão na intersecção. E não quero chute — quero dados." A noite está longe de
    acabar, e a verdade está em algum lugar entre duas tabelas.',

    'Descubra quais frequentadores do Blue Moon foram chamados para depor, combinando as tabelas nightclub_log e interrogations.',

    'SELECT n.* FROM nightclub_log n INNER JOIN interrogations i ON n.patron_name = i.patron_name;',

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
            (''Carlos Mendes'', ''Carlão'',       ''Whisky'',       ''23:15''),
            (''Ana Lima'',      ''Aninha'',       ''Martini'',      ''02:30''),
            (''Pedro Costa'',   ''Pé de Chumbo'', ''Cerveja'',      ''00:45''),
            (''Julia Ramos'',   ''Jujuba'',       ''Gim Tônica'',   ''01:20'');
        INSERT INTO interrogations (patron_name, statement, verdict) VALUES
            (''Ana Lima'',    ''Disse que estava no banheiro na hora do crime.'',  ''Inconclusivo''),
            (''Pedro Costa'', ''Afirmou que viu um homem de chapéu saindo pelo beco.'', ''Verdadeiro''),
            (''Julia Ramos'', ''Tremeu ao ser perguntada sobre o horário.'' ,        ''Inconclusivo'');
    ',
    ARRAY['SELECT', 'INNER JOIN'],
    200,
    '[
        {"id": 2, "patron_name": "Ana Lima",    "alias": "Aninha",       "drink": "Martini",    "left_at": "02:30"},
        {"id": 3, "patron_name": "Pedro Costa", "alias": "Pé de Chumbo", "drink": "Cerveja",    "left_at": "00:45"},
        {"id": 4, "patron_name": "Julia Ramos", "alias": "Jujuba",       "drink": "Gim Tônica", "left_at": "01:20"}
    ]',
    FALSE,
    'CRIMINAL',
    'INTERMEDIATE'
),

-- ========================================================================
-- MISSION 4 — BEGINNER / FINANCE
-- Teaches: SELECT, WHERE (boolean)
-- ========================================================================
(
    gen_random_uuid(),
    'O Gavião e o Urubu',

    'A Mendes & Filhos amanheceu em alvoroço. O contador-chefe, seu Valentim, está
    pálido como cera de vela, segurando um punhado de papéis com mãos trêmulas.
    "Alguém desviou quase meio milhão!" ele gagueja. O delegado Estranho — que
    nunca dorme — já está lá, com um café que esfria na mão. "Quero ver todas as
    contas ativas da empresa. Nomes, departamentos, saldos. Não podemos começar
    uma caça ao tesouro sem saber onde procurar." Ele vira a xícara e bebe o café
    frio de um gole só.',

    'Liste todas as contas ativas (is_active = TRUE) da tabela company_accounts.',

    'SELECT * FROM company_accounts WHERE is_active = TRUE;',

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
            (''Mendes & Filhos Ltda.'',        ''Operacional'',  487250.00, TRUE),
            (''Transportadora Rapidão S.A.'',  ''Logística'',   1230450.00, TRUE),
            (''Distribuidora Aurora'',          ''Suprimentos'',  823700.00, TRUE),
            (''Contabilidade Central'',         ''Serviços'',     312000.00, TRUE),
            (''Depósito Inativo'',              ''Encerrado'',       500.00, FALSE);
    ',
    ARRAY['SELECT', 'WHERE'],
    100,
    '[
        {"id": 1, "holder_name": "Mendes & Filhos Ltda.",       "department": "Operacional",  "balance": 487250.00,  "is_active": true},
        {"id": 2, "holder_name": "Transportadora Rapidão S.A.", "department": "Logística",    "balance": 1230450.00, "is_active": true},
        {"id": 3, "holder_name": "Distribuidora Aurora",        "department": "Suprimentos",  "balance": 823700.00,  "is_active": true},
        {"id": 4, "holder_name": "Contabilidade Central",       "department": "Serviços",     "balance": 312000.00,  "is_active": true}
    ]',
    FALSE,
    'FINANCE',
    'BEGINNER'
),

-- ========================================================================
-- MISSION 5 — INTERMEDIATE / ASTRONOMY
-- Teaches: SELECT, ORDER BY
-- ========================================================================
(
    gen_random_uuid(),
    'Sinais do Cosmos',

    'O radiotelescópio captou algo inexplicável. Uma sequência de pulsos eletromagnéticos
    vindos de diferentes sistemas estelares — alguns conhecidos, outros que nem constam
    nos mapas. A astrônoma-chefe Dra. Tavares está tensa, os olhos fixos no espectrógrafo.
    "Pode ser interferência de satélite," ela murmura, "ou pode ser a descoberta do
    século." Ela se vira para você: "Organize todas as leituras por frequência, da
    mais grave à mais aguda. Quero enxergar padrões. Se for inteligente, vai se
    repetir." O silêncio da sala de controle é cortado apenas pelo zumbido dos
    servidores.',

    'Liste todas as leituras do telescópio ordenadas pela frequência em ordem crescente.',

    'SELECT * FROM telescope_readings ORDER BY frequency_mhz ASC;',

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
            (''Alpha Centauri'',  ''Rádio'',        1420.00, 8.5, ''2025-03-15 22:30:00''),
            (''Betelgeuse'',      ''Infravermelho'', 350.50, 4.2, ''2025-03-15 22:35:00''),
            (''Proxima Centauri'', ''Rádio'',        1420.05, 9.1, ''2025-03-15 22:40:00''),
            (''Sirius'',          ''Óptico'',        520.75, 6.8, ''2025-03-15 22:45:00''),
            (''Andrômeda'',       ''Rádio'',        1420.10, 7.3, ''2025-03-15 22:50:00''),
            (''Vega'',            ''Infravermelho'', 310.20, 3.9, ''2025-03-15 22:55:00''),
            (''Alpha Centauri'',  ''Óptico'',        480.00, 7.8, ''2025-03-15 23:00:00'');
    ',
    ARRAY['SELECT', 'ORDER BY'],
    200,
    '[
        {"id": 6, "star_system": "Vega",            "signal_type": "Infravermelho", "frequency_mhz": 310.20, "strength": 3.9, "recorded_at": "2025-03-15 22:55:00"},
        {"id": 2, "star_system": "Betelgeuse",      "signal_type": "Infravermelho", "frequency_mhz": 350.50, "strength": 4.2, "recorded_at": "2025-03-15 22:35:00"},
        {"id": 7, "star_system": "Alpha Centauri",  "signal_type": "Óptico",        "frequency_mhz": 480.00, "strength": 7.8, "recorded_at": "2025-03-15 23:00:00"},
        {"id": 4, "star_system": "Sirius",          "signal_type": "Óptico",        "frequency_mhz": 520.75, "strength": 6.8, "recorded_at": "2025-03-15 22:45:00"},
        {"id": 1, "star_system": "Alpha Centauri",  "signal_type": "Rádio",         "frequency_mhz": 1420.00,"strength": 8.5, "recorded_at": "2025-03-15 22:30:00"},
        {"id": 3, "star_system": "Proxima Centauri","signal_type": "Rádio",         "frequency_mhz": 1420.05,"strength": 9.1, "recorded_at": "2025-03-15 22:40:00"},
        {"id": 5, "star_system": "Andrômeda",       "signal_type": "Rádio",         "frequency_mhz": 1420.10,"strength": 7.3, "recorded_at": "2025-03-15 22:50:00"}
    ]',
    TRUE,
    'ASTRONOMY',
    'INTERMEDIATE'
),

-- ========================================================================
-- MISSION 6 — ADVANCED / FINANCE
-- Teaches: SELECT, GROUP BY, HAVING, SUM, AVG
-- ========================================================================
(
    gen_random_uuid(),
    'A Fortuna do Submundo',

    'O desvio de recursos na Mendes & Filhos revelou um padrão suspeito: certas
    agências bancárias concentram valores muito acima do esperado. A promotora
    de justiça — uma mulher de olhar afiado que não dorme há dois dias — joga
    uma pasta na sua mesa. "Agrupe por agência. Quero o saldo total e a média
    de cada uma. E só me interessa ver as agências onde a média ultrapassa
    setecentos mil reais. É lá que o cheiro de dinheiro sujo é mais forte."
    Ela cruza os braços. "Vamos descobrir quem está lavando dinheiro na
    calada da noite."',

    'Calcule o saldo total e a média por agência bancária. Mostre apenas as agências com saldo médio superior a 700.000.',

    'SELECT branch, SUM(balance) AS total, AVG(balance) AS media FROM accounts GROUP BY branch HAVING AVG(balance) > 700000 ORDER BY total DESC;',

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
            (''Mendes & Filhos Ltda.'',       ''Centro'',    950000.00),
            (''Transportadora Rapidão S.A.'', ''Centro'',   1250000.00),
            (''Distribuidora Aurora'',         ''Centro'',    180000.00),
            (''Tech Solutions Ltda.'',         ''Centro'',   2200000.00),
            (''Auto Peças Brasil'',            ''Zona Sul'',  720000.00),
            (''Imobiliária Nova Era'',         ''Zona Sul'',  860000.00),
            (''Mercado Popular'',              ''Zona Norte'',320000.00),
            (''Farmácia Saúde'',               ''Zona Norte'',450000.00);
    ',
    ARRAY['SELECT', 'GROUP BY', 'HAVING', 'SUM', 'AVG'],
    350,
    '[
        {"branch": "Centro",  "total": 4580000.00, "media": 1145000.00},
        {"branch": "Zona Sul", "total": 1580000.00, "media": 790000.00}
    ]',
    FALSE,
    'FINANCE',
    'ADVANCED'
),

-- ========================================================================
-- MISSION 7 — ADVANCED / ASTRONOMY
-- Teaches: SELECT, WHERE, GROUP BY, HAVING, COUNT
-- ========================================================================
(
    gen_random_uuid(),
    'Mapa Estelar',

    'A Dra. Tavares está construindo um mapa astrofísico atualizado. Ela passou
    semanas catalogando corpos celestes e agora precisa de um resumo. "Classifiquei
    dezenas de objetos entre estrelas, exoplanetas, nebulosas e outros," ela diz,
    esfregando os olhos cansados. "Preciso saber quantos de cada tipo foram descobertos
    antes de 1950 — antes da era espacial, quando tudo era feito com telescópio e
    paciência. E quero ver apenas os tipos com mais de um corpo. Categorias com um
    só item são exceções, não padrões." Ela se recosta na cadeira e espera.',

    'Conte quantos corpos celestes de cada tipo foram descobertos antes de 1950, mostrando apenas os tipos com mais de 1 corpo.',

    'SELECT body_type, COUNT(*) AS quantidade FROM celestial_bodies WHERE discovered_year < 1950 GROUP BY body_type HAVING COUNT(*) > 1;',

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
            (''Proxima Centauri b'', ''Exoplaneta'',   4.25,       2016, FALSE),
            (''Betelgeuse'',         ''Estrela'',      642.50,     1835, FALSE),
            (''Andrômeda XXXI'',     ''Galáxia'',      2500000.00, 1923, FALSE),
            (''Kepler-442b'',        ''Exoplaneta'',   1206.00,    2015, FALSE),
            (''Sirius'',             ''Estrela'',      8.60,       1750, FALSE),
            (''Nebulosa do Caranguejo'', ''Nebulosa'', 6500.00,    1731, FALSE),
            (''TRAPPIST-1e'',        ''Exoplaneta'',   39.50,      2017, TRUE),
            (''Halley'',             ''Cometa'',       0.00,       1705, FALSE),
            (''Vela Pulsar'',        ''Estrela'',      959.00,     1968, FALSE),
            (''M87*'',               ''Buraco Negro'', 55000000.00, 2019, TRUE);
    ',
    ARRAY['SELECT', 'WHERE', 'GROUP BY', 'HAVING', 'COUNT'],
    350,
    '[
        {"body_type": "Estrela", "quantidade": 2}
    ]',
    FALSE,
    'ASTRONOMY',
    'ADVANCED'
),

-- ========================================================================
-- MISSION 8 — EXPERT / CYBERSECURITY
-- Teaches: SELECT, WHERE, GROUP BY, HAVING, COUNT, ORDER BY
-- ========================================================================
(
    gen_random_uuid(),
    'O Fantasma na Matrix',

    'O relógio marca 3h14 da manhã quando o alarme dispara na NexusCorp. As telas
    do centro de monitoramento piscam em vermelho. O CTO, um homem calvo de olheiras
    profundas, agarra o mouse com mão trêmula: "Alguém está acessando dados
    classificados de dentro da rede — de madrugada, quando ninguém deveria estar
    aqui." Ele aponta para os logs. "IPs internos. Acessos a recursos sensíveis.
    Hora morta. Mais de três acessos suspeitos por funcionário. Descubra quem
    são." O único som na sala é o zumbido dos servidores e o seu coração
    acelerado enquanto você encara a tela.',

    'Identifique os funcionários que realizaram mais de 3 acessos classificados como suspeitos (is_suspicious = TRUE). Mostre o nome e a contagem.',

    'SELECT employee_name, COUNT(*) AS acesso_count FROM access_logs WHERE is_suspicious = TRUE GROUP BY employee_name HAVING COUNT(*) > 3 ORDER BY employee_name;',

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
            (''Alex Silva'',  ''10.0.0.15'', ''/financeiro/balanco'',    ''03:14'', TRUE),
            (''Alex Silva'',  ''10.0.0.15'', ''/rh/dados_pessoais'',     ''03:16'', TRUE),
            (''Alex Silva'',  ''10.0.0.15'', ''/admin/users'',           ''03:18'', TRUE),
            (''Alex Silva'',  ''10.0.0.15'', ''/servidores/config'',     ''03:20'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/admin/backup'',          ''02:00'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/admin/logs'',            ''02:05'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/financeiro/balanco'',    ''02:10'', TRUE),
            (''João Lima'',   ''10.0.0.45'', ''/admin/usuarios'',        ''02:15'', TRUE),
            (''Maria Santos'', ''192.168.1.10'', ''/dashboard'',         ''09:00'', FALSE),
            (''Maria Santos'', ''192.168.1.10'', ''/relatorios'',        ''09:15'', FALSE),
            (''Carla Dias'',  ''192.168.1.50'', ''/email'',              ''08:30'', FALSE);
    ',
    ARRAY['SELECT', 'WHERE', 'GROUP BY', 'HAVING', 'COUNT', 'ORDER BY'],
    500,
    '[
        {"employee_name": "Alex Silva", "acesso_count": 4},
        {"employee_name": "João Lima",  "acesso_count": 4}
    ]',
    FALSE,
    'CYBERSECURITY',
    'EXPERT'
),

-- ========================================================================
-- MISSION 9 — INTERMEDIATE / BIOLOGY
-- Teaches: SELECT, INNER JOIN, WHERE
-- ========================================================================
(
    gen_random_uuid(),
    'O Surto',

    'A Unidade de Resposta Rápida foi mobilizada. Um surto de origem desconhecida
    se espalha pelo bairro da Lapa com uma velocidade assustadora. A doutora Moraes
    está exausta — jaleco amarrotado, cabelo grisalho escapando do coque — mas os
    olhos ainda têm fogo. "Os pacientes com sintomas graves precisam de isolamento
    imediato. Severidade acima de 7," ela diz, virando-se para o monitor. "Cruza
    a ficha dos pacientes com o relatório de sintomas. Quero nomes, idades e quais
    sintomas cada um apresentou. Tempo é vida agora, e eu não vou perder mais
    ninguém."',

    'Encontre os pacientes que apresentam pelo menos um sintoma com severidade maior que 7. Mostre o nome, a idade e o nome do sintoma.',

    'SELECT p.name, p.age, s.symptom_name FROM patients p INNER JOIN symptoms s ON p.id = s.patient_id WHERE s.severity > 7;',

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
            (''Seu Jorge'',   67, ''Lapa'', TRUE),
            (''Dona Maria'',  72, ''Lapa'', TRUE),
            (''Cláudio'',     34, ''Lapa'', FALSE),
            (''Renata'',      28, ''Lapa'', TRUE),
            (''Sr. Ambrósio'', 81, ''Lapa'', FALSE);
        INSERT INTO symptoms (patient_id, symptom_name, severity) VALUES
            (1, ''Febre Alta'',   9),
            (1, ''Tosse Seca'',   6),
            (1, ''Falta de Ar'',  8),
            (2, ''Febre Alta'',   8),
            (2, ''Confusão Mental'', 7),
            (3, ''Febre Alta'',   9),
            (3, ''Falta de Ar'',  9),
            (3, ''Náusea'',      5),
            (3, ''Cefaleia'',    6),
            (4, ''Dor de Garganta'', 4),
            (5, ''Febre Alta'',   9),
            (5, ''Tosse Seca'',   7),
            (5, ''Falta de Ar'', 10),
            (5, ''Cianose'',     9),
            (5, ''Convulsão'',  10);
    ',
    ARRAY['SELECT', 'INNER JOIN', 'WHERE'],
    250,
    '[
        {"name": "Seu Jorge",   "age": 67, "symptom_name": "Febre Alta"},
        {"name": "Seu Jorge",   "age": 67, "symptom_name": "Falta de Ar"},
        {"name": "Dona Maria",  "age": 72, "symptom_name": "Febre Alta"},
        {"name": "Cláudio",     "age": 34, "symptom_name": "Febre Alta"},
        {"name": "Cláudio",     "age": 34, "symptom_name": "Falta de Ar"},
        {"name": "Sr. Ambrósio","age": 81, "symptom_name": "Febre Alta"},
        {"name": "Sr. Ambrósio","age": 81, "symptom_name": "Falta de Ar"},
        {"name": "Sr. Ambrósio","age": 81, "symptom_name": "Cianose"},
        {"name": "Sr. Ambrósio","age": 81, "symptom_name": "Convulsão"}
    ]',
    FALSE,
    'BIOLOGY',
    'INTERMEDIATE'
),

-- ========================================================================
-- MISSION 10 — EXPERT / BIOLOGY
-- Teaches: UPDATE, WHERE, SELECT (DML + DQL)
-- ========================================================================
(
    gen_random_uuid(),
    'A Cura em Gotas',

    'O surto está se alastrando e a farmácia do hospital está com estoque crítico.
    A doutora Moraes está pálida, os lábios apertados. Ela segura uma prancheta
    com a listagem de medicamentos e a voz falha ao falar: "Vários lotes venceram
    e ninguém atualizou o sistema. Remédios vencidos são pior que remédio nenhum
    — podem mascarar sintomas, causar reações adversas, custar vidas." Ela enfia
    a caneta na sua mão. "Zera o estoque de tudo que venceu antes de 2025. Depois
    me mostra como ficou. Precisamos saber o que realmente temos para lutar."',

    'Atualize a quantidade (quantity) para 0 dos medicamentos com data de validade (expiry_date) anterior a 2025-01-01. Depois liste todos os registros.',

    'UPDATE medication_stock SET quantity = 0 WHERE expiry_date < ''2025-01-01''; SELECT * FROM medication_stock;',

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
            (''Ivermectina'',  ''IVM-23A'', 500,  ''2026-12-01''),
            (''Zanamivir'',    ''ZAN-22B'', 200,  ''2024-06-15''),
            (''Oseltamivir'',  ''OSL-21C'', 150,  ''2024-03-01''),
            (''Remdesivir'',   ''REM-24A'', 300,  ''2027-01-15''),
            (''Dipirona'',     ''DIP-22A'', 1000, ''2025-11-30''),
            (''Paracetamol'',  ''PAR-20B'', 750,  ''2023-08-20'');
    ',
    ARRAY['UPDATE', 'WHERE', 'SELECT'],
    400,
    '[
        {"id": 1, "medication": "Ivermectina",  "batch": "IVM-23A", "quantity": 500,  "expiry_date": "2026-12-01"},
        {"id": 2, "medication": "Zanamivir",    "batch": "ZAN-22B", "quantity": 0,   "expiry_date": "2024-06-15"},
        {"id": 3, "medication": "Oseltamivir",  "batch": "OSL-21C", "quantity": 0,   "expiry_date": "2024-03-01"},
        {"id": 4, "medication": "Remdesivir",   "batch": "REM-24A", "quantity": 300, "expiry_date": "2027-01-15"},
        {"id": 5, "medication": "Dipirona",     "batch": "DIP-22A", "quantity": 1000,"expiry_date": "2025-11-30"},
        {"id": 6, "medication": "Paracetamol",  "batch": "PAR-20B", "quantity": 0,   "expiry_date": "2023-08-20"}
    ]',
    FALSE,
    'BIOLOGY',
    'EXPERT'
);
