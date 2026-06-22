import { describe, it, expect } from 'vitest';
import { schemasEqual, normalizeRows, SchemaTable } from './schema.utils';

const col = (overrides?: Partial<{ name: string; type: string; isPrimaryKey: boolean; isForeignKey: boolean }>) => ({
  name: 'id',
  type: 'INT',
  isPrimaryKey: false,
  isForeignKey: false,
  ...overrides,
});

describe('schemasEqual', () => {
  const tableA: SchemaTable[] = [
    { name: 'users', columns: [col({ name: 'id', type: 'INT' }), col({ name: 'name', type: 'TEXT' })] },
  ];
  const tableB: SchemaTable[] = [
    { name: 'users', columns: [col({ name: 'id', type: 'INT' }), col({ name: 'name', type: 'TEXT' })] },
  ];

  it('returns true for identical schemas', () => {
    expect(schemasEqual(tableA, tableB)).toBe(true);
  });

  it('returns false for schemas with different lengths', () => {
    const empty: SchemaTable[] = [];
    expect(schemasEqual(tableA, empty)).toBe(false);
  });

  it('returns true for same tables in different order', () => {
    const a: SchemaTable[] = [
      { name: 'orders', columns: [col({ name: 'id', type: 'INT' })] },
      { name: 'users', columns: [col({ name: 'id', type: 'INT' })] },
    ];
    const b: SchemaTable[] = [
      { name: 'users', columns: [col({ name: 'id', type: 'INT' })] },
      { name: 'orders', columns: [col({ name: 'id', type: 'INT' })] },
    ];
    expect(schemasEqual(a, b)).toBe(true);
  });

  it('returns false for schemas with different columns', () => {
    const a: SchemaTable[] = [
      { name: 'users', columns: [col({ name: 'id', type: 'INT' })] },
    ];
    const b: SchemaTable[] = [
      { name: 'users', columns: [col({ name: 'id', type: 'BIGINT' })] },
    ];
    expect(schemasEqual(a, b)).toBe(false);
  });
});

describe('normalizeRows', () => {
  it('converts Date objects to ISO date strings', () => {
    const rows = [{ created_at: new Date('2024-01-15T10:30:00Z') }];
    const result = normalizeRows(rows);
    expect(result[0]['created_at']).toBe('2024-01-15');
  });

  it('recursively normalizes nested objects', () => {
    const rows = [{ meta: { joined: new Date('2024-06-01T00:00:00Z') } }];
    const result = normalizeRows(rows);
    expect((result[0]['meta'] as Record<string, unknown>)['joined']).toBe('2024-06-01');
  });

  it('preserves null values', () => {
    const rows = [{ name: null }];
    const result = normalizeRows(rows);
    expect(result[0]['name']).toBeNull();
  });

  it('preserves numbers, strings, and booleans', () => {
    const rows = [{ age: 25, name: 'Alice', active: true }];
    const result = normalizeRows(rows);
    expect(result[0]['age']).toBe(25);
    expect(result[0]['name']).toBe('Alice');
    expect(result[0]['active']).toBe(true);
  });
});
