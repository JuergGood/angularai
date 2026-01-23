CREATE TABLE system_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    setting_value VARCHAR(255)
);

INSERT INTO system_settings (setting_key, setting_value) VALUES ('geolocation_enabled', 'false');
