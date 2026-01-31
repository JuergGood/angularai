-- Migration to add geo-location and user agent info to action_log table
ALTER TABLE action_log ADD COLUMN IF NOT EXISTS ip_address VARCHAR(255);
ALTER TABLE action_log ADD COLUMN IF NOT EXISTS country VARCHAR(255);
ALTER TABLE action_log ADD COLUMN IF NOT EXISTS city VARCHAR(255);
ALTER TABLE action_log ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE action_log ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE action_log ADD COLUMN IF NOT EXISTS user_agent TEXT;
