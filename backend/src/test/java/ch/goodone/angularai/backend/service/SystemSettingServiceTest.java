package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.SystemSetting;
import ch.goodone.angularai.backend.repository.SystemSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceTest {

    @Mock
    private SystemSettingRepository repository;

    @InjectMocks
    private SystemSettingService systemSettingService;

    @Test
    void isGeolocationEnabled_ReturnsTrue_WhenValueIsTrue() {
        SystemSetting setting = new SystemSetting(SystemSettingService.GEOLOCATION_ENABLED, "true");
        when(repository.findById(SystemSettingService.GEOLOCATION_ENABLED)).thenReturn(Optional.of(setting));

        assertTrue(systemSettingService.isGeolocationEnabled());
    }

    @Test
    void isGeolocationEnabled_ReturnsFalse_WhenValueIsFalse() {
        SystemSetting setting = new SystemSetting(SystemSettingService.GEOLOCATION_ENABLED, "false");
        when(repository.findById(SystemSettingService.GEOLOCATION_ENABLED)).thenReturn(Optional.of(setting));

        assertFalse(systemSettingService.isGeolocationEnabled());
    }

    @Test
    void isGeolocationEnabled_ReturnsFalse_WhenNotFound() {
        when(repository.findById(SystemSettingService.GEOLOCATION_ENABLED)).thenReturn(Optional.empty());

        assertFalse(systemSettingService.isGeolocationEnabled());
    }

    @Test
    void setGeolocationEnabled_UpdatesExistingSetting() {
        SystemSetting setting = new SystemSetting(SystemSettingService.GEOLOCATION_ENABLED, "false");
        when(repository.findById(SystemSettingService.GEOLOCATION_ENABLED)).thenReturn(Optional.of(setting));

        systemSettingService.setGeolocationEnabled(true);

        assertEquals("true", setting.getValue());
        verify(repository).save(setting);
    }

    @Test
    void setGeolocationEnabled_CreatesNewSetting_WhenNotFound() {
        when(repository.findById(SystemSettingService.GEOLOCATION_ENABLED)).thenReturn(Optional.empty());

        systemSettingService.setGeolocationEnabled(true);

        verify(repository).save(argThat(s -> 
            s.getKey().equals(SystemSettingService.GEOLOCATION_ENABLED) && s.getValue().equals("true")
        ));
    }
}
