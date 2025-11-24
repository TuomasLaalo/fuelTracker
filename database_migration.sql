-- Migration script to add tank_capacity_liters column to vehicles table
-- Run this in your PostgreSQL database

-- Step 1: Add the column as nullable first (for existing rows)
ALTER TABLE vehicles 
ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;

-- Step 2: If you have existing vehicles, you can set a default value temporarily
-- (Optional - only if you have existing vehicles without tank capacity)
-- UPDATE vehicles SET tank_capacity_liters = 50.0 WHERE tank_capacity_liters IS NULL;

-- Step 3: After updating all existing vehicles, you can make it NOT NULL
-- (Uncomment this after all vehicles have tank capacity set)
-- ALTER TABLE vehicles ALTER COLUMN tank_capacity_liters SET NOT NULL;

