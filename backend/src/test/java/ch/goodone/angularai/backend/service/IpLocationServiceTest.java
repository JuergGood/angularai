package ch.goodone.angularai.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpLocationServiceTest {

    @Mock
    private SystemSettingService systemSettingService;

    @Mock
    private RestTemplate restTemplate;

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

    @Test
    void lookup_ReturnsLocation_WhenSuccessful() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);
        String ip = "8.8.8.8";
        Map<String, Object> response = new HashMap<>();
        response.put("country_name", "United States");
        response.put("city", "Mountain View");
        response.put("latitude", 37.4223);
        response.put("longitude", -122.0847);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        IpLocationService.GeoLocation result = ipLocationService.lookup(ip);

        assertNotNull(result);
        assertEquals("United States", result.getCountry());
        assertEquals("Mountain View", result.getCity());
        assertEquals(37.4223, result.getLatitude());
        assertEquals(-122.0847, result.getLongitude());
    }

    @Test
    void lookup_ReturnsEmpty_WhenApiReturnsError() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);
        Map<String, Object> response = new HashMap<>();
        response.put("error", "some error");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        IpLocationService.GeoLocation result = ipLocationService.lookup("8.8.8.8");

        assertNotNull(result);
        assertNull(result.getCountry());
    }

    @Test
    void lookup_ReturnsEmpty_WhenApiReturnsNull() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        IpLocationService.GeoLocation result = ipLocationService.lookup("8.8.8.8");

        assertNotNull(result);
        assertNull(result.getCountry());
    }

    @Test
    void lookup_ReturnsEmpty_WhenApiThrowsException() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("API down"));

        IpLocationService.GeoLocation result = ipLocationService.lookup("8.8.8.8");

        assertNotNull(result);
        assertNull(result.getCountry());
    }

    @Test
    void lookup_HandlesInvalidNumberTypes() {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);
        Map<String, Object> response = new HashMap<>();
        response.put("latitude", "not-a-number");
        response.put("longitude", null);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        IpLocationService.GeoLocation result = ipLocationService.lookup("8.8.8.8");

        assertNotNull(result);
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
    }
}
