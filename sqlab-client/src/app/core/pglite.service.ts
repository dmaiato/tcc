import { Injectable, signal } from '@angular/core';

export interface QueryResult {
  rows: Record<string, unknown>[];
  fields: { name: string; dataTypeID: number }[];
  affectedRows: number;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PgliteService {
  private db: any = null;
  private storedDdl = '';
  private storedDml = '';

  private static PGliteClass: any = null;

  isReady = signal(false);
  isLoading = signal(false);
  lastError = signal<string | null>(null);
  isModified = signal(false);

  private static async loadPGlite(): Promise<any> {
    if (PgliteService.PGliteClass) {
      return PgliteService.PGliteClass;
    }

    const module = await import('@electric-sql/pglite');
    PgliteService.PGliteClass = module.PGlite;
    return PgliteService.PGliteClass;
  }

  async createSession(ddl: string, dml: string): Promise<void> {
    await this.disposeSession();

    this.storedDdl = ddl;
    this.storedDml = dml;
    this.isLoading.set(true);
    this.lastError.set(null);

    try {
      const PGlite = await PgliteService.loadPGlite();

      const [pgliteWasmModule, initdbWasmModule, fsBundle] = await Promise.all([
        WebAssembly.compileStreaming(fetch('/pglite.wasm')),
        WebAssembly.compileStreaming(fetch('/initdb.wasm')),
        fetch('/pglite.data').then(res => res.blob())
      ]);

      this.db = await PGlite.create({
        pgliteWasmModule,
        initdbWasmModule,
        fsBundle,
      });

      if (ddl.trim()) {
        await this.db.exec(ddl);
      }

      if (dml.trim()) {
        await this.db.exec(dml);
      }

      this.isReady.set(true);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to initialize database';
      this.lastError.set(message);
      this.isReady.set(false);
      throw error;
    } finally {
      this.isLoading.set(false);
    }
  }

  async executeQuery(sql: string): Promise<QueryResult> {
    if (!this.db || !this.isReady()) {
      return {
        rows: [],
        fields: [],
        affectedRows: 0,
        error: 'Database not initialized'
      };
    }

    try {
      const result = await this.db.query(sql);
      const isDataModifying = this.isDataModifyingQuery(sql);
      if (isDataModifying && result.affectedRows > 0) {
        this.isModified.set(true);
      }
      return {
        rows: result.rows as Record<string, unknown>[],
        fields: result.fields || [],
        affectedRows: result.affectedRows || 0
      };
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Query execution failed';
      this.lastError.set(message);
      return {
        rows: [],
        fields: [],
        affectedRows: 0,
        error: message
      };
    }
  }

  private isDataModifyingQuery(sql: string): boolean {
    const trimmed = sql.trim().toUpperCase();
    return trimmed.startsWith('INSERT') ||
           trimmed.startsWith('UPDATE') ||
           trimmed.startsWith('DELETE') ||
           trimmed.startsWith('CREATE') ||
           trimmed.startsWith('DROP') ||
           trimmed.startsWith('ALTER');
  }

  async getSchema(): Promise<{ name: string; columns: { name: string; type: string; isPrimaryKey: boolean; isForeignKey: boolean }[] }[]> {
    if (!this.db || !this.isReady()) {
      return [];
    }

    try {
      const colResult = await this.db.query(`
        SELECT table_name, column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_schema = 'public'
        ORDER BY table_name, ordinal_position
      `);

      const pkResult = await this.db.query(`
        SELECT tc.table_name, kcu.column_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
          AND tc.table_schema = kcu.table_schema
        WHERE tc.table_schema = 'public'
          AND tc.constraint_type = 'PRIMARY KEY'
      `);

      const fkResult = await this.db.query(`
        SELECT tc.table_name, kcu.column_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
          AND tc.table_schema = kcu.table_schema
        WHERE tc.table_schema = 'public'
          AND tc.constraint_type = 'FOREIGN KEY'
      `);

      const pkSet = new Set((pkResult.rows as Record<string, unknown>[]).map(
        r => `${r['table_name']}.${r['column_name']}`
      ));
      const fkSet = new Set((fkResult.rows as Record<string, unknown>[]).map(
        r => `${r['table_name']}.${r['column_name']}`
      ));

      const tableMap = new Map<string, { name: string; columns: { name: string; type: string; isPrimaryKey: boolean; isForeignKey: boolean }[] }>();

      for (const row of colResult.rows as Record<string, unknown>[]) {
        const tableName = row['table_name'] as string;
        const columnName = row['column_name'] as string;
        if (!tableMap.has(tableName)) {
          tableMap.set(tableName, { name: tableName, columns: [] });
        }
        tableMap.get(tableName)!.columns.push({
          name: columnName,
          type: (row['data_type'] as string).toUpperCase(),
          isPrimaryKey: pkSet.has(`${tableName}.${columnName}`),
          isForeignKey: fkSet.has(`${tableName}.${columnName}`)
        });
      }

      return Array.from(tableMap.values());
    } catch {
      return [];
    }
  }

  async exec(sql: string): Promise<void> {
    if (!this.db || !this.isReady()) {
      throw new Error('Database not initialized');
    }

    try {
      await this.db.exec(sql);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'SQL execution failed';
      this.lastError.set(message);
      throw error;
    }
  }

  async resetToOriginalState(): Promise<void> {
    if (!this.db) {
      return;
    }

    this.isLoading.set(true);
    this.lastError.set(null);

    try {
      await this.db.exec(`
        DROP SCHEMA public CASCADE;
        CREATE SCHEMA public;
      `);

      if (this.storedDdl.trim()) {
        await this.db.exec(this.storedDdl);
      }

      if (this.storedDml.trim()) {
        await this.db.exec(this.storedDml);
      }

      this.isModified.set(false);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to reset database';
      this.lastError.set(message);
      throw error;
    } finally {
      this.isLoading.set(false);
    }
  }

  async disposeSession(): Promise<void> {
    if (this.db) {
      try {
        await this.db.close();
      } catch {
      }
      this.db = null;
    }
    this.isReady.set(false);
    this.storedDdl = '';
    this.storedDml = '';
  }

  isSessionReady(): boolean {
    return this.isReady();
  }

  isDbModified(): boolean {
    return this.isModified();
  }

  clearError(): void {
    this.lastError.set(null);
  }
}