import { TestBed } from '@angular/core/testing';

const mockQuery = vi.fn();
const mockExec = vi.fn();
const mockClose = vi.fn();

const mockDb = {
  query: mockQuery,
  exec: mockExec,
  close: mockClose,
};

const mockCreate = vi.fn().mockResolvedValue(mockDb);

vi.mock('@electric-sql/pglite', () => ({
  PGlite: {
    create: mockCreate,
  },
}));

const mockFetch = vi.fn().mockResolvedValue({
  arrayBuffer: vi.fn().mockResolvedValue(new ArrayBuffer(0)),
  blob: vi.fn().mockResolvedValue(new Blob()),
});

globalThis.fetch = mockFetch as any;

(globalThis as any).WebAssembly = {
  compileStreaming: vi.fn().mockResolvedValue({}),
};

import { PgliteService } from './pglite.service';

async function bootstrapSession(
  service: PgliteService,
  ddl = '',
  dml = ''
): Promise<void> {
  await service.createSession(ddl, dml);
}

describe('PgliteService', () => {
  let service: PgliteService;

  beforeEach(() => {
    vi.clearAllMocks();
    mockCreate.mockResolvedValue(mockDb);
    service = TestBed.inject(PgliteService);
    service.isReady.set(false);
    service.isLoading.set(false);
    service.lastError.set(null);
    service.isModified.set(false);
  });

  afterEach(async () => {
    const db = (service as any).db;
    if (db) {
      (service as any).db = null;
    }
    service.isReady.set(false);
    service.isModified.set(false);
    service.lastError.set(null);
  });

  // ── createSession ───────────────────────────────────────────────

  describe('createSession', () => {
    it('should successfully create a session with no DDL/DML', async () => {
      await service.createSession('', '');

      expect(mockCreate).toHaveBeenCalledOnce();
      expect(service.isReady()).toBe(true);
      expect(service.isLoading()).toBe(false);
    });

    it('should successfully create a session and exec DDL', async () => {
      const ddl = 'CREATE TABLE foo (id INT);';
      await service.createSession(ddl, '');

      expect(mockExec).toHaveBeenCalledWith(ddl);
      expect(service.isReady()).toBe(true);
    });

    it('should successfully create a session and exec DML', async () => {
      const dml = 'INSERT INTO foo VALUES (1);';
      await service.createSession('', dml);

      expect(mockExec).toHaveBeenCalledWith(dml);
      expect(service.isReady()).toBe(true);
    });

    it('should exec DDL then DML when both provided', async () => {
      const ddl = 'CREATE TABLE foo (id INT);';
      const dml = 'INSERT INTO foo VALUES (1);';
      await service.createSession(ddl, dml);

      expect(mockExec).toHaveBeenNthCalledWith(1, ddl);
      expect(mockExec).toHaveBeenNthCalledWith(2, dml);
      expect(service.isReady()).toBe(true);
    });

    it('should not exec empty/whitespace DDL', async () => {
      await service.createSession('   ', '');

      expect(mockExec).not.toHaveBeenCalled();
      expect(service.isReady()).toBe(true);
    });

    it('should not exec empty/whitespace DML', async () => {
      await service.createSession('', '   ');

      expect(mockExec).not.toHaveBeenCalled();
      expect(service.isReady()).toBe(true);
    });

    it('should set isReady=false and lastError when PGlite.create fails', async () => {
      const err = new Error('WASM load failed');
      mockCreate.mockRejectedValueOnce(err);

      await expect(service.createSession('', '')).rejects.toThrow('WASM load failed');
      expect(service.lastError()).toBe('WASM load failed');
      expect(service.isReady()).toBe(false);
      expect(service.isLoading()).toBe(false);
    });

    it('should set isReady=false and lastError when exec throws', async () => {
      const err = new Error('Syntax error in DDL');
      mockExec.mockRejectedValueOnce(err);

      await expect(service.createSession('BAD SQL', '')).rejects.toThrow('Syntax error in DDL');
      expect(service.lastError()).toBe('Syntax error in DDL');
      expect(service.isReady()).toBe(false);
      expect(service.isLoading()).toBe(false);
    });

    it('should set isLoading=true during creation', async () => {
      let capturedLoading = false;
      mockCreate.mockImplementationOnce(() => {
        capturedLoading = service.isLoading();
        return Promise.resolve(mockDb);
      });

      await service.createSession('', '');
      expect(capturedLoading).toBe(true);
      expect(service.isLoading()).toBe(false);
    });

    it('should dispose previous session before creating a new one', async () => {
      await bootstrapSession(service);
      expect(service.isReady()).toBe(true);

      await service.createSession('', '');

      expect(mockClose).toHaveBeenCalledOnce();
      expect(mockCreate).toHaveBeenCalledTimes(2);
      expect(service.isReady()).toBe(true);
    });

    it('should store DDL and DML for later resets', async () => {
      const ddl = 'CREATE TABLE t1 (id INT);';
      const dml = 'INSERT INTO t1 VALUES (1);';
      await service.createSession(ddl, dml);

      await service.resetToOriginalState();

      expect(mockExec).toHaveBeenCalledWith(expect.stringContaining('DROP SCHEMA public CASCADE'));
      expect(mockExec).toHaveBeenCalledWith(ddl);
      expect(mockExec).toHaveBeenCalledWith(dml);
    });

    it('should handle non-Error throws from PGlite.create', async () => {
      mockCreate.mockRejectedValueOnce('raw string error');

      await expect(service.createSession('', '')).rejects.toBe('raw string error');
      expect(service.lastError()).toBe('Failed to initialize database');
      expect(service.isReady()).toBe(false);
    });

    it('should set isLoading=true and clear lastError at start', async () => {
      service.lastError.set('old error');
      expect(service.lastError()).toBe('old error');

      await service.createSession('', '');

      expect(service.lastError()).toBeNull();
      expect(service.isLoading()).toBe(false);
    });
  });

  // ── executeQuery ────────────────────────────────────────────────

  describe('executeQuery', () => {
    it('should return error when db is not initialized', async () => {
      service.isReady.set(false);
      const result = await service.executeQuery('SELECT 1');
      expect(result.error).toBe('Database not initialized');
    });

    it('should return error when db is null', async () => {
      (service as any).db = null;
      service.isReady.set(false);
      const result = await service.executeQuery('SELECT 1');
      expect(result.error).toBe('Database not initialized');
    });

    it('should return rows for a successful SELECT', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({
        rows: [{ id: 1, name: 'test' }],
        fields: [
          { name: 'id', dataTypeID: 23 },
          { name: 'name', dataTypeID: 25 },
        ],
        affectedRows: 0,
      });

      const result = await service.executeQuery('SELECT * FROM test');

      expect(result.error).toBeUndefined();
      expect(result.rows).toEqual([{ id: 1, name: 'test' }]);
      expect(result.fields).toHaveLength(2);
      expect(result.affectedRows).toBe(0);
      expect(service.isModified()).toBe(false);
    });

    it('should set isModified=true for INSERT', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({
        rows: [],
        fields: [],
        affectedRows: 1,
      });

      const result = await service.executeQuery('INSERT INTO test VALUES (1)');

      expect(result.error).toBeUndefined();
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=true for UPDATE', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 2 });

      await service.executeQuery('UPDATE test SET x = 1');
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=true for DELETE', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 1 });

      await service.executeQuery('DELETE FROM test');
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=true for CREATE', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 0 });

      await service.executeQuery('CREATE TABLE foo (id INT)');
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=true for DROP', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 0 });

      await service.executeQuery('DROP TABLE foo');
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=true for ALTER', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 0 });

      await service.executeQuery('ALTER TABLE foo ADD bar INT');
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=true for TRUNCATE', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 0 });

      await service.executeQuery('TRUNCATE TABLE foo');
      expect(service.isModified()).toBe(true);
    });

    it('should set isModified=false for SELECT (explicit check)', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 0 });

      await service.executeQuery('SELECT * FROM foo');
      expect(service.isModified()).toBe(false);
    });

    it('should handle case-insensitive prefixes', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [], fields: [], affectedRows: 1 });

      await service.executeQuery('insert into test values (1)');
      expect(service.isModified()).toBe(true);
    });

    it('should return error on query failure without throwing', async () => {
      await bootstrapSession(service);
      mockQuery.mockRejectedValueOnce(new Error('division by zero'));

      const result = await service.executeQuery('SELECT 1/0');

      expect(result.error).toBe('division by zero');
      expect(result.rows).toEqual([]);
      expect(service.lastError()).toBe('division by zero');
    });

    it('should handle non-Error query rejections', async () => {
      await bootstrapSession(service);
      mockQuery.mockRejectedValueOnce('broken');

      const result = await service.executeQuery('BAD');

      expect(result.error).toBe('Query execution failed');
    });

    it('should handle query result without fields or affectedRows', async () => {
      await bootstrapSession(service);
      mockQuery.mockResolvedValueOnce({ rows: [{ x: 1 }] });

      const result = await service.executeQuery('SELECT x');

      expect(result.fields).toEqual([]);
      expect(result.affectedRows).toBe(0);
    });

    it('should not throw when !db even if isReady was true', async () => {
      await bootstrapSession(service);
      (service as any).db = null;

      const result = await service.executeQuery('SELECT 1');

      expect(result.error).toBe('Database not initialized');
    });
  });

  // ── getSchema ───────────────────────────────────────────────────

  describe('getSchema', () => {
    it('should return empty array when db is not ready', async () => {
      const schema = await service.getSchema();
      expect(schema).toEqual([]);
    });

    it('should return empty array when db is null', async () => {
      service.isReady.set(true);
      (service as any).db = null;
      const schema = await service.getSchema();
      expect(schema).toEqual([]);
    });

    it('should return table schema on success', async () => {
      await bootstrapSession(service);
      mockQuery
        .mockResolvedValueOnce({
          rows: [
            { table_name: 'users', column_name: 'id', data_type: 'integer', is_nullable: 'NO' },
            { table_name: 'users', column_name: 'name', data_type: 'varchar', is_nullable: 'YES' },
          ],
        })
        .mockResolvedValueOnce({
          rows: [{ table_name: 'users', column_name: 'id' }],
        })
        .mockResolvedValueOnce({
          rows: [],
        });

      const schema = await service.getSchema();

      expect(schema).toHaveLength(1);
      expect(schema[0].name).toBe('users');
      expect(schema[0].columns).toHaveLength(2);
      expect(schema[0].columns[0]).toEqual({
        name: 'id',
        type: 'INTEGER',
        isPrimaryKey: true,
        isForeignKey: false,
      });
      expect(schema[0].columns[1]).toEqual({
        name: 'name',
        type: 'VARCHAR',
        isPrimaryKey: false,
        isForeignKey: false,
      });
      expect(mockQuery).toHaveBeenCalledTimes(3);
    });

    it('should handle foreign keys', async () => {
      await bootstrapSession(service);
      mockQuery
        .mockResolvedValueOnce({
          rows: [
            { table_name: 'orders', column_name: 'user_id', data_type: 'integer', is_nullable: 'NO' },
          ],
        })
        .mockResolvedValueOnce({ rows: [] })
        .mockResolvedValueOnce({
          rows: [{ table_name: 'orders', column_name: 'user_id' }],
        });

      const schema = await service.getSchema();

      expect(schema[0].columns[0].isPrimaryKey).toBe(false);
      expect(schema[0].columns[0].isForeignKey).toBe(true);
    });

    it('should return empty array on query error', async () => {
      await bootstrapSession(service);
      mockQuery.mockRejectedValueOnce(new Error('schema query failed'));

      const schema = await service.getSchema();

      expect(schema).toEqual([]);
    });

    it('should return multiple tables', async () => {
      await bootstrapSession(service);
      mockQuery
        .mockResolvedValueOnce({
          rows: [
            { table_name: 'users', column_name: 'id', data_type: 'integer', is_nullable: 'NO' },
            { table_name: 'orders', column_name: 'id', data_type: 'integer', is_nullable: 'NO' },
          ],
        })
        .mockResolvedValueOnce({ rows: [] })
        .mockResolvedValueOnce({ rows: [] });

      const schema = await service.getSchema();

      expect(schema).toHaveLength(2);
      expect(schema.map(t => t.name)).toEqual(['users', 'orders']);
    });
  });

  // ── exec ────────────────────────────────────────────────────────

  describe('exec', () => {
    it('should throw when db is not ready', async () => {
      await expect(service.exec('CREATE TABLE foo (id INT)')).rejects.toThrow(
        'Database not initialized'
      );
    });

    it('should throw when db is null', async () => {
      service.isReady.set(true);
      (service as any).db = null;

      await expect(service.exec('SQL')).rejects.toThrow('Database not initialized');
    });

    it('should call db.exec on success', async () => {
      await bootstrapSession(service);
      mockExec.mockResolvedValueOnce(undefined);

      await service.exec('CREATE TABLE foo (id INT)');

      expect(mockExec).toHaveBeenCalledWith('CREATE TABLE foo (id INT)');
    });

    it('should set lastError and throw on exec error', async () => {
      await bootstrapSession(service);
      const err = new Error('bad SQL');
      mockExec.mockRejectedValueOnce(err);

      await expect(service.exec('BAD')).rejects.toThrow('bad SQL');
      expect(service.lastError()).toBe('bad SQL');
    });

    it('should handle non-Error exec rejections', async () => {
      await bootstrapSession(service);
      mockExec.mockRejectedValueOnce('raw');

      await expect(service.exec('X')).rejects.toBe('raw');
      expect(service.lastError()).toBe('SQL execution failed');
    });
  });

  // ── resetToOriginalState ────────────────────────────────────────

  describe('resetToOriginalState', () => {
    it('should return early when no db (no error)', async () => {
      (service as any).db = null;

      await expect(service.resetToOriginalState()).resolves.toBeUndefined();
    });

    it('should reset db to original DDL/DML state', async () => {
      await service.createSession('CREATE TABLE t (id INT)', 'INSERT INTO t VALUES (1)');
      mockExec.mockClear();

      service.isModified.set(true);
      await service.resetToOriginalState();

      expect(mockExec).toHaveBeenNthCalledWith(1, expect.stringContaining('DROP SCHEMA public CASCADE'));
      expect(mockExec).toHaveBeenNthCalledWith(2, 'CREATE TABLE t (id INT)');
      expect(mockExec).toHaveBeenNthCalledWith(3, 'INSERT INTO t VALUES (1)');
      expect(service.isModified()).toBe(false);
    });

    it('should re-exec only DDL when DML is empty', async () => {
      await service.createSession('CREATE TABLE t (id INT)', '');
      mockExec.mockClear();

      await service.resetToOriginalState();

      expect(mockExec).toHaveBeenCalledTimes(2);
      expect(service.isModified()).toBe(false);
    });

    it('should re-exec only DML when DDL is empty', async () => {
      await service.createSession('', 'INSERT INTO t VALUES (1)');
      mockExec.mockClear();

      await service.resetToOriginalState();

      expect(mockExec).toHaveBeenCalledTimes(2);
      expect(service.isModified()).toBe(false);
    });

    it('should set lastError and throw on failure', async () => {
      await service.createSession('CREATE TABLE t (id INT)', '');
      mockExec.mockClear();
      const err = new Error('reset failed');
      mockExec.mockRejectedValueOnce(err);

      await expect(service.resetToOriginalState()).rejects.toThrow('reset failed');
      expect(service.lastError()).toBe('reset failed');
      expect(service.isModified()).toBe(false);
    });

    it('should handle non-Error from reset exec', async () => {
      await service.createSession('CREATE TABLE t (id INT)', '');
      mockExec.mockClear();
      mockExec.mockRejectedValueOnce('raw reset error');

      await expect(service.resetToOriginalState()).rejects.toBe('raw reset error');
      expect(service.lastError()).toBe('Failed to reset database');
    });
  });

  // ── disposeSession ──────────────────────────────────────────────

  describe('disposeSession', () => {
    it('should close db and reset all state', async () => {
      await bootstrapSession(service);
      service.isModified.set(true);

      await service.disposeSession();

      expect(mockClose).toHaveBeenCalledOnce();
      expect((service as any).db).toBeNull();
      expect(service.isReady()).toBe(false);
    });

    it('should reset signals even when db is null', async () => {
      (service as any).db = null;
      service.isReady.set(true);
      service.isModified.set(true);

      await service.disposeSession();

      expect(mockClose).not.toHaveBeenCalled();
      expect(service.isReady()).toBe(false);
    });

    it('should clear state even if db.close() throws', async () => {
      await bootstrapSession(service);
      mockClose.mockRejectedValueOnce(new Error('close exploded'));

      await service.disposeSession();

      expect((service as any).db).toBeNull();
      expect(service.isReady()).toBe(false);
    });

    it('should reset stored DDL and DML', async () => {
      await service.createSession('DDL', 'DML');
      expect((service as any).storedDdl).toBe('DDL');
      expect((service as any).storedDml).toBe('DML');

      await service.disposeSession();

      expect((service as any).storedDdl).toBe('');
      expect((service as any).storedDml).toBe('');
    });
  });

  // ── wrapper methods ─────────────────────────────────────────────

  describe('wrapper methods', () => {
    it('isSessionReady() should return isReady()', () => {
      service.isReady.set(true);
      expect(service.isSessionReady()).toBe(true);

      service.isReady.set(false);
      expect(service.isSessionReady()).toBe(false);
    });

    it('isDbModified() should return isModified()', () => {
      service.isModified.set(true);
      expect(service.isDbModified()).toBe(true);

      service.isModified.set(false);
      expect(service.isDbModified()).toBe(false);
    });

    it('clearError() should reset lastError', () => {
      service.lastError.set('something went wrong');
      service.clearError();
      expect(service.lastError()).toBeNull();
    });
  });
});
