import { describe, it, expect } from 'vitest';
import { parseDDL } from './parse-ddl.utils';

describe('parseDDL', () => {
  it('parses CREATE TABLE with two columns', () => {
    const result = parseDDL('CREATE TABLE users (id INT, name TEXT);');
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('users');
    expect(result[0].columns).toHaveLength(2);
    expect(result[0].columns[0].name).toBe('id');
    expect(result[0].columns[0].type).toBe('INT');
    expect(result[0].columns[1].name).toBe('name');
    expect(result[0].columns[1].type).toBe('TEXT');
  });

  it('parses CREATE TABLE with PRIMARY KEY inline', () => {
    const result = parseDDL('CREATE TABLE users (id INT PRIMARY KEY, name TEXT);');
    expect(result).toHaveLength(1);
    expect(result[0].columns[0].isPrimaryKey).toBe(true);
    expect(result[0].columns[0].isForeignKey).toBe(false);
  });

  it('parses CREATE TABLE with FOREIGN KEY REFERENCES', () => {
    const result = parseDDL('CREATE TABLE orders (id INT PRIMARY KEY, user_id INT REFERENCES users(id));');
    expect(result).toHaveLength(1);
    expect(result[0].columns[1].isForeignKey).toBe(true);
  });

  it('parses CREATE TABLE with NOT NULL, DEFAULT, UNIQUE', () => {
    const ddl = 'CREATE TABLE products (id INT NOT NULL, name VARCHAR(100) NOT NULL, price DECIMAL(10,2) DEFAULT 0.00, sku VARCHAR(50) UNIQUE);';
    const result = parseDDL(ddl);
    expect(result).toHaveLength(1);
    expect(result[0].columns).toHaveLength(4);
  });

  it('parses CREATE TABLE IF NOT EXISTS', () => {
    const result = parseDDL('CREATE TABLE IF NOT EXISTS users (id INT, name TEXT);');
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('users');
  });

  it('parses identifiers with double quotes', () => {
    const result = parseDDL('CREATE TABLE "users" ("id" INT, "name" TEXT);');
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('users');
    expect(result[0].columns[0].name).toBe('id');
  });

  it('parses multi-column definitions on separate lines', () => {
    const ddl = `CREATE TABLE users (
      id INT,
      name TEXT,
      email VARCHAR(255)
    );`;
    const result = parseDDL(ddl);
    expect(result).toHaveLength(1);
    expect(result[0].columns).toHaveLength(3);
  });

  it('returns empty array for DDL without semicolon', () => {
    const result = parseDDL('CREATE TABLE users (id INT, name TEXT)');
    expect(result).toHaveLength(0);
  });

  it('parses multiple CREATE TABLE statements', () => {
    const ddl = 'CREATE TABLE users (id INT, name TEXT); CREATE TABLE orders (id INT, total DECIMAL(10,2));';
    const result = parseDDL(ddl);
    expect(result).toHaveLength(2);
    expect(result[0].name).toBe('users');
    expect(result[1].name).toBe('orders');
  });

  it('returns empty array for DML only (INSERT)', () => {
    const result = parseDDL('INSERT INTO users VALUES (1, \'Alice\');');
    expect(result).toHaveLength(0);
  });

  it('returns empty array for empty DDL', () => {
    const result = parseDDL('');
    expect(result).toHaveLength(0);
  });

  it('parses VARCHAR(255), DECIMAL(10,2), TIMESTAMPTZ types', () => {
    const ddl = 'CREATE TABLE t (v VARCHAR(255), d DECIMAL(10,2), ts TIMESTAMPTZ);';
    const result = parseDDL(ddl);
    expect(result).toHaveLength(1);
    expect(result[0].columns[0].type).toBe('VARCHAR(255)');
    expect(result[0].columns[1].type).toBe('DECIMAL(10,2)');
    expect(result[0].columns[2].type).toBe('TIMESTAMPTZ');
  });

  it('skips SQL comments (lines starting with --)', () => {
    const ddl = `-- Create the users table
CREATE TABLE users (
  id INT, -- primary key
  name TEXT
);`;
    const result = parseDDL(ddl);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('users');
  });
});
