package ch.goodone.angularai.backend.service;

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

    public IpLocationService() {
        this.restTemplate = new RestTemplate();
    }

    public GeoLocation lookup(String ip) {
        if (ip == null || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            return new GeoLocation();
        }
        try {
            String url = String.format("%s%s?access_key=%s", apiUrl, ip, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                GeoLocation loc = new GeoLocation();
                loc.setCountry((String) response.get("country_name"));
                loc.setCity((String) response.get("city"));
                loc.setLatitude((Double) response.get("latitude"));
                loc.setLongitude((Double) response.get("longitude"));
                return loc;
            }
        } catch (Exception e) {
            logger.error("Error looking up IP location for {}: {}", ip, e.getMessage());
        }
        return new GeoLocation();
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
