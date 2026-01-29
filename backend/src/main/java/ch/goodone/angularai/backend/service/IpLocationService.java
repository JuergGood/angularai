package ch.goodone.angularai.backend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class IpLocationService {

    private static final Logger logger = LoggerFactory.getLogger(IpLocationService.class);

    @Value("${ipstack.api.key}")
    private String apiKey;

    @Value("${ipstack.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final SystemSettingService systemSettingService;

    public IpLocationService(SystemSettingService systemSettingService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.systemSettingService = systemSettingService;
    }

    public GeoLocation lookup(String ip) {
        if (!systemSettingService.isGeolocationEnabled()) {
            return new GeoLocation();
        }
        if (ip == null || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            logger.info("Skipping geolocation lookup for local/private IP");
            return new GeoLocation();
        }
        try {
            String url = String.format("%s%s?access_key=%s", apiUrl, ip, apiKey);
            logger.info("Requesting geolocation for IP from provider");
            Map<String, Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();
            
            if (response != null) {
                if (response.containsKey("error")) {
                    logger.error("IpStack API returned error");
                    return new GeoLocation();
                }

                GeoLocation loc = new GeoLocation();
                loc.setCountry((String) response.get("country_name"));
                loc.setCity((String) response.get("city"));
                loc.setLatitude(toDouble(response.get("latitude")));
                loc.setLongitude(toDouble(response.get("longitude")));
                
                logger.info("Geolocation lookup successful");
                return loc;
            } else {
                logger.warn("IpStack API returned null response");
            }
        } catch (Exception e) {
            logger.error("Error looking up IP location");
        }
        return new GeoLocation();
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }

    public static class GeoLocation {
        private String country;
        private String city;
        private Double latitude;
        private Double longitude;

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
}
