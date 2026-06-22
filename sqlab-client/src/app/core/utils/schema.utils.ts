import { ColumnInfo } from './parse-ddl.utils';

export interface SchemaTable {
  name: string;
  columns: ColumnInfo[];
}

export function normalizeRows(rows: Record<string, unknown>[]): Record<string, unknown>[] {
  return rows.map(row => {
    const normalized: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(row)) {
      if (value instanceof Date) {
        normalized[key] = value.toISOString().split('T')[0];
      } else if (typeof value === 'object' && value !== null && !(value instanceof Array)) {
        normalized[key] = normalizeRows([value as Record<string, unknown>])[0];
      } else {
        normalized[key] = value;
      }
    }
    return normalized;
  });
}

export function schemasEqual(
  a: SchemaTable[],
  b: SchemaTable[]
): boolean {
  if (a.length !== b.length) return false;

  const sortedA = [...a].sort((x, y) => x.name.localeCompare(y.name));
  const sortedB = [...b].sort((x, y) => x.name.localeCompare(y.name));

  for (let i = 0; i < sortedA.length; i++) {
    const colsA = sortedA[i].columns.map(c => `${c.name}:${c.type}`).sort().join(',');
    const colsB = sortedB[i].columns.map(c => `${c.name}:${c.type}`).sort().join(',');
    if (colsA !== colsB) return false;
  }
  return true;
}
