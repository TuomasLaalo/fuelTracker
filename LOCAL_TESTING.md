# Local Testing Guide

## Quick Start for Local Development

### 1. Check PostgreSQL Status

Your application is configured to use PostgreSQL on port **5433** with:
- Database: `fueltracker`
- Username: `postgres`
- Password: `jack`

**Check if PostgreSQL is running:**
```bash
# Windows PowerShell
Get-NetTCPConnection -LocalPort 5433 -ErrorAction SilentlyContinue

# Or check if PostgreSQL service is running
Get-Service -Name postgresql*
```

### 2. Start PostgreSQL

**Option A: If PostgreSQL is installed as a Windows service:**
```powershell
# Start PostgreSQL service
Start-Service postgresql-x64-XX  # Replace XX with your version number
```

**Option B: If using Docker:**
```bash
docker run --name postgres-fueltracker -e POSTGRES_PASSWORD=jack -e POSTGRES_USER=postgres -p 5433:5432 -d postgres
```

**Option C: If PostgreSQL is installed but on different port:**
- Check your PostgreSQL configuration (usually port 5432)
- Update `application-dev.properties` to match your PostgreSQL port

### 3. Create Database

Once PostgreSQL is running, create the database:

```sql
-- Connect to PostgreSQL (using psql or pgAdmin)
CREATE DATABASE fueltracker;
```

Or via command line:
```bash
psql -U postgres -h localhost -p 5433
CREATE DATABASE fueltracker;
\q
```

### 4. Verify Configuration

Your current setup:
- **Profile**: `dev` (for local testing)
- **Database**: PostgreSQL on port 5433
- **Auto-create tables**: Yes (`ddl-auto=update`)

### 5. Start the Application

The application will:
- Connect to PostgreSQL on port 5433
- Automatically create tables on first run
- Show SQL queries in console (for debugging)

### 6. Alternative: Use Different PostgreSQL Port

If your PostgreSQL runs on port **5432** (default), update `application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fueltracker
```

### Troubleshooting

**Error: "Connection refused"**
- PostgreSQL is not running
- PostgreSQL is running on a different port
- Firewall is blocking the connection

**Error: "Database does not exist"**
- Create the database: `CREATE DATABASE fueltracker;`

**Error: "Authentication failed"**
- Check username/password in `application-dev.properties`
- Verify PostgreSQL user permissions

### Test Connection

You can test the connection manually:
```bash
# Using psql
psql -U postgres -h localhost -p 5433 -d fueltracker

# Or using pgAdmin (GUI tool)
```

Once connected, the application should start successfully! 🚀

