export interface ColumnInfo {
  name: string;
  type: string;
  isPrimaryKey: boolean;
  isForeignKey: boolean;
}

export interface TableInfo {
  name: string;
  columns: ColumnInfo[];
}

export function parseDDL(ddl: string): TableInfo[] {
  const tables: TableInfo[] = [];
  const createTableRegex = /CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?["']?(\w+)["']?\s*\(([\s\S]*?)\);/gi;
  let match: RegExpExecArray | null;

  while ((match = createTableRegex.exec(ddl)) !== null) {
    const tableName = match[1];
    const columnsStr = match[2];
    const columns: ColumnInfo[] = [];

    const columnRegex = /["']?(\w+)["']?\s+([A-Z][A-Z0-9_]+(?:\([^)]+\))?)\s*(?:,|NOT\s+NULL|NULL|PRIMARY\s+KEY|REFERENCES|DEFAULT|UNIQUE|CONSTRAINT|$)/gi;
    let colMatch: RegExpExecArray | null;

    while ((colMatch = columnRegex.exec(columnsStr)) !== null) {
      const fullMatch = colMatch[0].toUpperCase();
      columns.push({
        name: colMatch[1],
        type: colMatch[2].toUpperCase(),
        isPrimaryKey: fullMatch.includes('PRIMARY KEY'),
        isForeignKey: fullMatch.includes('REFERENCES')
      });
    }

    if (columns.length > 0) {
      tables.push({ name: tableName, columns });
    }
  }

  return tables;
}
