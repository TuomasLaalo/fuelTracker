# Quick Database Fix

## The Problem
The `vehicles` table is missing the `tank_capacity_liters` column.

## Solution: Add the Column Manually

### Option 1: Using psql (Command Line)

```bash
# Connect to your database
psql -U postgres -d fueltracker -h localhost -p 5432

# Then run:
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;

# Exit psql
\q
```

### Option 2: Using pgAdmin (GUI)

1. Open pgAdmin
2. Connect to your database
3. Right-click on `fueltracker` database → Query Tool
4. Run this SQL:
```sql
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;
```
5. Click Execute (F5)

### Option 3: If you have existing vehicles

If you already have vehicles in the database, you may want to set a default value:

```sql
-- Add the column
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;

-- Set default for existing vehicles (optional - adjust the value as needed)
UPDATE vehicles SET tank_capacity_liters = 50.0 WHERE tank_capacity_liters IS NULL;
```

## After Adding the Column

1. **Restart your Spring Boot application**
2. Try creating a vehicle again - it should work now!

## Verify the Column Was Added

You can check if the column exists:
```sql
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'vehicles' AND column_name = 'tank_capacity_liters';
```

If it returns a row, the column exists!

