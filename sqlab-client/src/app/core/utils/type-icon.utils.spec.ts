import { describe, it, expect } from 'vitest';
import { getTypeIcon } from './type-icon.utils';
import { ColumnInfo } from './parse-ddl.utils';

function col(type: string, overrides?: Partial<ColumnInfo>): ColumnInfo {
  return { name: 'c', type, isPrimaryKey: false, isForeignKey: false, ...overrides };
}

describe('getTypeIcon', () => {
  it.each(['INT', 'INTEGER', 'INT4', 'BIGINT', 'SMALLINT', 'TINYINT'])('returns hash for %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('hash');
  });

  it.each(['DECIMAL', 'NUMERIC', 'REAL', 'FLOAT', 'MONEY'])('returns hash for %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('hash');
  });

  it.each(['TEXT', 'VARCHAR', 'CHAR', 'NVARCHAR'])('returns type for %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('type');
  });

  it.each(['BOOL', 'BOOLEAN'])('returns toggle-left for %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('toggle-left');
  });

  it.each(['DATE', 'DATETIME', 'TIMESTAMP', 'TIME', 'TIMESTAMPTZ'])('returns calendar for %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('calendar');
  });

  it('returns fingerprint for UUID', () => {
    expect(getTypeIcon(col('UUID'))).toBe('fingerprint');
  });

  it.each(['JSON', 'JSONB'])('returns code for %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('code');
  });

  it('returns key when isPrimaryKey is true regardless of type', () => {
    expect(getTypeIcon(col('TEXT', { isPrimaryKey: true }))).toBe('key');
  });

  it('returns key when isForeignKey is true regardless of type', () => {
    expect(getTypeIcon(col('TEXT', { isForeignKey: true }))).toBe('key');
  });

  it.each(['XML', 'ENUM', 'GEOMETRY'])('returns help-circle for unknown type %s', (type) => {
    expect(getTypeIcon(col(type))).toBe('help-circle');
  });
});
