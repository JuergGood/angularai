package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.SystemSetting;
import ch.goodone.angularai.backend.repository.SystemSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void getRecaptchaConfigIndex_ReturnsValueFromDatabase() {
        SystemSetting setting = new SystemSetting(SystemSettingService.RECAPTCHA_CONFIG_INDEX, "3");
        when(repository.findById(SystemSettingService.RECAPTCHA_CONFIG_INDEX)).thenReturn(Optional.of(setting));

        assertEquals(3, systemSettingService.getRecaptchaConfigIndex());
    }

    @Test
    void getRecaptchaConfigIndex_ReturnsFallback_WhenNotFoundAndDefaultIsNull() {
        when(repository.findById(SystemSettingService.RECAPTCHA_CONFIG_INDEX)).thenReturn(Optional.empty());
        // defaultRecaptchaConfig is null here
        assertEquals(1, systemSettingService.getRecaptchaConfigIndex());
    }

    @Test
    void getRecaptchaConfigIndex_ReturnsFallback_WhenDatabaseValueInvalid() {
        SystemSetting setting = new SystemSetting(SystemSettingService.RECAPTCHA_CONFIG_INDEX, "invalid");
        when(repository.findById(SystemSettingService.RECAPTCHA_CONFIG_INDEX)).thenReturn(Optional.of(setting));
        assertEquals(1, systemSettingService.getRecaptchaConfigIndex());
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

    @Test
    void isLandingMessageEnabled_ReturnsTrue_ByDefault() {
        when(repository.findById(SystemSettingService.LANDING_MESSAGE_ENABLED)).thenReturn(Optional.empty());
        assertTrue(systemSettingService.isLandingMessageEnabled());
    }

    @Test
    void isLandingMessageEnabled_ReturnsFalse_WhenDisabled() {
        SystemSetting setting = new SystemSetting(SystemSettingService.LANDING_MESSAGE_ENABLED, "false");
        when(repository.findById(SystemSettingService.LANDING_MESSAGE_ENABLED)).thenReturn(Optional.of(setting));
        assertFalse(systemSettingService.isLandingMessageEnabled());
    }

    @Test
    void setLandingMessageEnabled_UpdatesExistingSetting() {
        SystemSetting setting = new SystemSetting(SystemSettingService.LANDING_MESSAGE_ENABLED, "true");
        when(repository.findById(SystemSettingService.LANDING_MESSAGE_ENABLED)).thenReturn(Optional.of(setting));

        systemSettingService.setLandingMessageEnabled(false);

        assertEquals("false", setting.getValue());
        verify(repository).save(setting);
    }
}
