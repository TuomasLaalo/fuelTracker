-- Migration script to remove fullTank column and ensure tankCapacityLiters exists
-- Run this in your PostgreSQL database

-- Step 1: Add tank_capacity_liters column if it doesn't exist (as nullable first)
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS tank_capacity_liters DOUBLE PRECISION;

-- Step 2: Set default value for existing vehicles (adjust 50.0 as needed)
UPDATE vehicles SET tank_capacity_liters = 50.0 WHERE tank_capacity_liters IS NULL;

-- Step 3: Make tank_capacity_liters NOT NULL (after all vehicles have values)
ALTER TABLE vehicles ALTER COLUMN tank_capacity_liters SET NOT NULL;

-- Step 4: Drop full_tank column from fuel_entries table
ALTER TABLE fuel_entries DROP COLUMN IF EXISTS full_tank;

