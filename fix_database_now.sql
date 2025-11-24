-- Step 1: Add the column as nullable (this will work even with existing vehicles)
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;

-- Step 2: Set a default value for existing vehicles (optional - adjust as needed)
-- You can change 50.0 to a more appropriate default for your vehicles
UPDATE vehicles SET tank_capacity_liters = 50.0 WHERE tank_capacity_liters IS NULL;

-- Step 3: (Optional) After updating all vehicles, you can make it NOT NULL
-- Uncomment this line after all vehicles have tank capacity set:
-- ALTER TABLE vehicles ALTER COLUMN tank_capacity_liters SET NOT NULL;

