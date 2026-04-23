# PGlite Study Notes

## What is PGlite?

PGlite is a lightweight PostgreSQL database that runs entirely in the browser, Node.js, Bun, or Deno. It's a WebAssembly build of PostgreSQL that provides full SQL capabilities without requiring a backend server.

## Installation

```bash
npm install @electric-sql/pglite
# or
pnpm add @electric-sql/pglite
# or
bun install @electric-sql/pglite
# or
deno add npm:@electric-sql/pglite
```

## Creating a Database Instance

### Recommended Method (Static Factory)

```typescript
import { PGlite } from '@electric-sql/pglite'

// Use PGlite.create() for better TypeScript support and automatic readiness
const db = await PGlite.create('./path/to/pgdata')
```

Using `PGlite.create()` is preferred because:
- It automatically awaits database readiness
- Provides better TypeScript type inference with extensions

### Constructor Method

```typescript
const db = new PGlite()
const db = new PGlite('./path/to/pgdata')
const db = new PGlite(options)
```

## Storage Backends

| URI Scheme | Platform | Description |
|-----------|---------|-------------|
| `memory://` | All | In-memory, ephemeral storage |
| `idb://` | Browser | IndexedDB persistence |
| `file://` or none | Node/Bun | Native filesystem |
| `./path/to/dir` | Node/Bun | Native filesystem (unprefixed) |

### Examples

```typescript
// In-memory (ephemeral)
const db = new PGlite('memory://')

// Browser with IndexedDB
const db = new PGlite('idb://sqlab-data')

// Node/Bun filesystem
const db = new PGlite('./data/pgdata')
```

## Query Methods

### .query(sql, params?)

Executes a single parameterized statement. Returns results with `rows`, `affectedRows`, and `fields`.

```typescript
const result = await db.query('SELECT * FROM users WHERE id = $1', [1])
console.log(result.rows) // Array of row objects
```

### .exec(sql)

Executes one or more statements without parameters. Useful for migrations and batch operations.

```typescript
await db.exec(`
  CREATE TABLE IF NOT EXISTS todos (
    id SERIAL PRIMARY KEY,
    task TEXT,
    done BOOLEAN DEFAULT false
  );
  INSERT INTO todos (task) VALUES ('Learn PGlite');
`)
```

### .sql`template string`

Tagged template literal for automatic parameterization.

```typescript
const name = 'Alice'
const result = await db.sql`SELECT * FROM users WHERE name = ${name}`
// Equivalent to: db.query('SELECT * FROM users WHERE name = $1', ['Alice'])
```

### .transaction(callback)

Interactive transactions with automatic commit/rollback.

```typescript
await db.transaction(async (tx) => {
  await tx.query('INSERT INTO accounts (balance) VALUES ($1)', [100])
  await tx.query('UPDATE accounts SET balance = balance - 50 WHERE id = $1', [1])
})
// Auto-commits on success, auto-rollbacks on rejection
```

### Tagged Template Helpers

```typescript
import { identifier, raw, sql, query } from '@electric-sql/pglite/template'

// Escape identifiers (table/column names)
await db.sql`SELECT * FROM ${identifier`my table`}`

// Raw SQL (no parameterization)
await db.sql`SELECT * FROM users ${raw`WHERE active = true`}`

// Nested templates
const filter = (active: boolean) => active ? sql`WHERE active = true` : raw`WHERE 1=1`
await db.sql`SELECT * FROM users ${filter(true)}`

// Generate query without executing
const { query, params } = query`SELECT * FROM users WHERE id = ${userId}`
```

## Options

```typescript
interface PGliteOptions {
  dataDir?: string
  debug?: 1-5                    // Logging level
  relaxedDurability?: boolean    // Faster writes (skipflush)
  fs?: Filesystem                 // Custom filesystem
  loadDataDir?: Blob | File      // Load from dump
  extensions?: Extensions        // Extensions to load
  username?: string              // Database user
  database?: string              // Database name
  initialMemory?: number        // Initial memory allocation
  pgliteWasmModule?: WebAssembly.Module
  initdbWasmModule?: WebAssembly.Module
  fsBundle?: Blob | File
  parsers?: ParserOptions        // Custom type parsers
  serializers?: SerializerOptions // Custom type serializers
  startParams?: string[]        // Postgres启动参数
}
```

### Important Options

- **`debug: 1-5`**: Enable debug logging
- **`relaxedDurability: true`**: Skip flushing to storage after each query (faster writes, useful with IndexedDB)
- **`extensions: { live }`**: Load extensions like live queries
- **`loadDataDir: Blob`**: Initialize from a previously dumped database

## Additional Methods

### Listen/Notify

```typescript
// Subscribe to notifications
const unsub = await db.listen('channel', (payload) => {
  console.log('Received:', payload)
})

// Send notification
await db.query("NOTIFY channel, 'Hello!'")

// Unsubscribe
await unsub()
```

### Dump/Load

```typescript
// Export database to tarball
const dump = await db.dumpDataDir() // Returns Blob

// Load from dump on initialization
const db = await PGlite.create({
  loadDataDir: dump
})
```

### Describe Query

```typescript
// Get query metadata without executing
const info = await db.describeQuery('SELECT id, name FROM users WHERE id = $1')
// Returns: { queryParams: [...], resultFields: [...] }
```

## Framework Hooks (React)

```typescript
import { usePGlite } from '@electric-sql/pglite/react'

function App() {
  const { db, ready, error } = usePGlite()

  if (!ready) return <div>Loading...</div>

  return <div>Database ready!</div>
}
```

For Vue and other frameworks, see the official docs.

## Live Queries Extension

For reactive UI updates when database changes:

```typescript
import { PGlite } from '@electric-sql/pglite'
import { live } from '@electric-sql/pglite/live'

const db = await PGlite.create({
  extensions: { live }
})

// Create a live query
const { rows } = await db.live.query('SELECT * FROM todos')

// rows automatically updates when data changes
```

## TypeScript Support

```typescript
interface User {
  id: number
  name: string
  email: string
}

const result = await db.query<User[]>('SELECT * FROM users')
// result.rows is typed as User[]
```

## Best Practices for Browser Usage

1. **Use `PGlite.create()`** instead of `new PGlite()` for automatic readiness checking
2. **Use IndexedDB** for persistence: `PGlite.create('idb://my-data')`
3. **Enable relaxed durability** for better performance: `{ relaxedDurability: true }`
4. **Use parameterized queries** to prevent SQL injection
5. **Handle errors** with try/catch in async operations
6. **Close database** when done: `await db.close()`

## Common Patterns

### Initialization with Error Handling

```typescript
async function initDatabase() {
  try {
    const db = await PGlite.create('idb://sqlab', {
      relaxedDurability: true,
      debug: 1
    })
    return db
  } catch (error) {
    console.error('Failed to initialize database:', error)
    throw error
  }
}
```

### Migration Pattern

```typescript
async function runMigrations(db: PGlite) {
  const migrations = [
    `CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name TEXT)`,
    `CREATE TABLE IF NOT EXISTS posts (id SERIAL PRIMARY KEY, user_id INT REFERENCES users(id), title TEXT)`
  ]

  for (const migration of migrations) {
    await db.exec(migration)
  }
}
```

### Using Transactions

```typescript
async function transferFunds(db: PGlite, fromId: number, toId: number, amount: number) {
  await db.transaction(async (tx) => {
    await tx.query('UPDATE accounts SET balance = balance - $1 WHERE id = $2', [amount, fromId])
    await tx.query('UPDATE accounts SET balance = balance + $1 WHERE id = $2', [amount, toId])
  })
}
```

## Resources

- Official Docs: https://pglite.dev/docs/
- NPM Package: https://www.npmjs.com/package/@electric-sql/pglite
- GitHub: https://github.com/electric-sql/pglite