import { PGlite } from '@electric-sql/pglite';

describe('PGlite integration', () => {
  let db: PGlite;

  afterEach(async () => {
    if (db) {
      await db.close();
    }
  });

  it('deve criar banco e executar query simple', async () => {
    db = new PGlite();
    const result = await db.query<{ message: string }>("select 'hello world' as message;");
    expect(result.rows).toEqual([{ message: 'hello world' }]);
  });

  it('deve criar tabela, inserir e selecionar dados', async () => {
    db = new PGlite();
    await db.exec(`
      CREATE TABLE test (id SERIAL PRIMARY KEY, name TEXT);
      INSERT INTO test (name) VALUES ('foo');
      INSERT INTO test (name) VALUES ('bar');
    `);
    const result = await db.query<{ id: number; name: string }>('SELECT * FROM test ORDER BY id;');
    expect(result.rows).toHaveLength(2);
    expect(result.rows[0].name).toBe('foo');
    expect(result.rows[1].name).toBe('bar');
  });

  it('deve atualizar e verificar dados alterados', async () => {
    db = new PGlite();
    await db.exec(`
      CREATE TABLE products (id SERIAL PRIMARY KEY, price INTEGER);
      INSERT INTO products (price) VALUES (100);
    `);
    await db.exec('UPDATE products SET price = 200 WHERE id = 1;');
    const result = await db.query<{ price: number }>('SELECT price FROM products WHERE id = 1;');
    expect(result.rows[0].price).toBe(200);
  });

  it('deve inspecionar schema via information_schema', async () => {
    db = new PGlite();
    await db.exec(`
      CREATE TABLE users (
        id SERIAL PRIMARY KEY,
        email TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT NOW()
      );
    `);
    const result = await db.query<{ table_name: string; column_name: string; data_type: string }>(`
      SELECT table_name, column_name, data_type
      FROM information_schema.columns
      WHERE table_schema = 'public'
      ORDER BY ordinal_position
    `);
    expect(result.rows).toHaveLength(3);
    expect(result.rows[0]).toMatchObject({ table_name: 'users', column_name: 'id' });
    expect(result.rows[1]).toMatchObject({ table_name: 'users', column_name: 'email' });
    expect(result.rows[2]).toMatchObject({ table_name: 'users', column_name: 'created_at' });
  });

  it('deve detectar primary keys via information_schema', async () => {
    db = new PGlite();
    await db.exec('CREATE TABLE items (id SERIAL PRIMARY KEY, label TEXT);');
    const result = await db.query<{ table_name: string; column_name: string }>(`
      SELECT tc.table_name, kcu.column_name
      FROM information_schema.table_constraints tc
      JOIN information_schema.key_column_usage kcu
        ON tc.constraint_name = kcu.constraint_name
        AND tc.table_schema = kcu.table_schema
      WHERE tc.table_schema = 'public'
        AND tc.constraint_type = 'PRIMARY KEY'
    `);
    expect(result.rows).toHaveLength(1);
    expect(result.rows[0]).toMatchObject({ table_name: 'items', column_name: 'id' });
  });

  it('deve retornar linhas vazias para tabela sem dados', async () => {
    db = new PGlite();
    await db.exec('CREATE TABLE empty_table (id SERIAL, name TEXT);');
    const result = await db.query('SELECT * FROM empty_table;');
    expect(result.rows).toHaveLength(0);
  });

  it('deve suportar múltiplas queries no mesmo exec', async () => {
    db = new PGlite();
    await db.exec(`
      CREATE TABLE a (x INT);
      CREATE TABLE b (y INT);
      INSERT INTO a VALUES (1), (2);
      INSERT INTO b VALUES (3);
    `);
    const ra = await db.query<{ x: number }>('SELECT * FROM a ORDER BY x;');
    const rb = await db.query<{ y: number }>('SELECT * FROM b;');
    expect(ra.rows).toHaveLength(2);
    expect(rb.rows).toHaveLength(1);
  });
});
