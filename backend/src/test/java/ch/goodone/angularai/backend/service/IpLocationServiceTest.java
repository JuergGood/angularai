package ch.goodone.angularai.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpLocationServiceTest {

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private IpLocationService ipLocationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ipLocationService, "apiKey", "test-key");
        ReflectionTestUtils.setField(ipLocationService, "apiUrl", "http://api.ipstack.com/");
    }

    @Test
    void lookup_ReturnsEmpty_WhenDisabled() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(false);

        IpLocationService.GeoLocation result = ipLocationService.lookup("8.8.8.8");

        assertNotNull(result);
        assertNull(result.getCountry());
        verify(systemSettingService).isGeolocationEnabled();
    }

    @Test
    void lookup_ReturnsEmpty_WhenLocalhostEvenIfEnabled() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);

        IpLocationService.GeoLocation result1 = ipLocationService.lookup("127.0.0.1");
        IpLocationService.GeoLocation result2 = ipLocationService.lookup("192.168.1.10");

        assertNotNull(result1);
        assertNull(result1.getCountry());
        assertNotNull(result2);
        assertNull(result2.getCountry());
    }
}
