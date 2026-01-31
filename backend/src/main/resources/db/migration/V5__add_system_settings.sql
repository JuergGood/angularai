CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    setting_value VARCHAR(255)
);

-- Use MERGE to avoid duplicate key errors on inserts
MERGE INTO system_settings KEY (setting_key) VALUES ('geolocation_enabled', 'false');
MERGE INTO system_settings KEY (setting_key) VALUES ('recaptcha_config_index', '2');
