-- Add pending_email column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS pending_email VARCHAR(255);
