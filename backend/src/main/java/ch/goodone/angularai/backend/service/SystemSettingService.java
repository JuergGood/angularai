package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.SystemSetting;
import ch.goodone.angularai.backend.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemSettingService {

    public static final String GEOLOCATION_ENABLED = "geolocation_enabled";
    public static final String RECAPTCHA_CONFIG_INDEX = "recaptcha_config_index";

    private final SystemSettingRepository repository;

    @org.springframework.beans.factory.annotation.Value("${google.recaptcha.default.config:1}")
    private String defaultRecaptchaConfig;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    public boolean isGeolocationEnabled() {
        return repository.findById(GEOLOCATION_ENABLED)
                .map(setting -> Boolean.parseBoolean(setting.getValue()))
                .orElse(false);
    }

    @Transactional
    public void setGeolocationEnabled(boolean enabled) {
        SystemSetting setting = repository.findById(GEOLOCATION_ENABLED)
                .orElse(new SystemSetting(GEOLOCATION_ENABLED, "false"));
        setting.setValue(String.valueOf(enabled));
        repository.save(setting);
    }

    public int getRecaptchaConfigIndex() {
        return repository.findById(RECAPTCHA_CONFIG_INDEX)
                .map(setting -> {
                    try {
                        return Integer.parseInt(setting.getValue());
                    } catch (NumberFormatException e) {
                        return parseDefaultConfig();
                    }
                })
                .orElseGet(this::parseDefaultConfig);
    }

    private int parseDefaultConfig() {
        try {
            return Integer.parseInt(defaultRecaptchaConfig);
        } catch (NumberFormatException e) {
            return 1; // absolute fallback
        }
    }

    @Transactional
    public void setRecaptchaConfigIndex(int index) {
        SystemSetting setting = repository.findById(RECAPTCHA_CONFIG_INDEX)
                .orElse(new SystemSetting(RECAPTCHA_CONFIG_INDEX, defaultRecaptchaConfig));
        setting.setValue(String.valueOf(index));
        repository.save(setting);
    }
}
