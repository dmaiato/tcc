import { ColumnInfo } from './parse-ddl.utils';

export function getTypeIcon(col: ColumnInfo): string {
  if (col.isPrimaryKey || col.isForeignKey) return 'key';
  const t = col.type;
  if (t.includes('INT') || t.includes('DECIMAL') || t.includes('NUMERIC')
      || t.includes('REAL') || t.includes('MONEY') || t.includes('FLOAT'))
    return 'hash';
  if (t.includes('TEXT') || t.includes('VARCHAR') || t.includes('CHAR'))
    return 'type';
  if (t.includes('BOOL'))
    return 'toggle-left';
  if (t.includes('DATE') || t.includes('TIMESTAMP') || t.includes('TIME'))
    return 'calendar';
  if (t.includes('UUID'))
    return 'fingerprint';
  if (t.includes('JSON'))
    return 'code';
  return 'help-circle';
}
