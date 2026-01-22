-- Migration to add geo-location and user agent info to action_log table
ALTER TABLE action_log ADD COLUMN ip_address VARCHAR(255);
ALTER TABLE action_log ADD COLUMN country VARCHAR(255);
ALTER TABLE action_log ADD COLUMN city VARCHAR(255);
ALTER TABLE action_log ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE action_log ADD COLUMN longitude DOUBLE PRECISION;
ALTER TABLE action_log ADD COLUMN user_agent TEXT;
