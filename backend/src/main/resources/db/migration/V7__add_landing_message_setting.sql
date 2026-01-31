-- Use MERGE to avoid duplicate key errors on inserts
MERGE INTO system_settings KEY (setting_key) VALUES ('landing_message_enabled', 'true');
