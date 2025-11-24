-- Quick fix: Add tank_capacity_liters column to vehicles table
-- Connect to your database and run this:

ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;

-- If you have existing vehicles, set a temporary default (optional):
-- UPDATE vehicles SET tank_capacity_liters = 50.0 WHERE tank_capacity_liters IS NULL;

