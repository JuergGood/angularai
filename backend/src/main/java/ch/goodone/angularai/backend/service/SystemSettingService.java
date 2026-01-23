package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.SystemSetting;
import ch.goodone.angularai.backend.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SystemSettingService {

    public static final String GEOLOCATION_ENABLED = "geolocation_enabled";

    private final SystemSettingRepository repository;

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
}
