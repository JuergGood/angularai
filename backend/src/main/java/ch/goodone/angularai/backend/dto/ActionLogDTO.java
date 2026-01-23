package ch.goodone.angularai.backend.dto;

import ch.goodone.angularai.backend.model.ActionLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ActionLogDTO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String login;
    private String action;
    private String details;
    private String ipAddress;
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;
    private String userAgent;

    public ActionLogDTO() {}

    public ActionLogDTO(Long id, LocalDateTime timestamp, String login, String action, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.login = login;
        this.action = action;
        this.details = details;
    }


    public static ActionLogDTO fromEntity(ActionLog log) {
        ActionLogDTO dto = new ActionLogDTO();
        dto.setId(log.getId());
        dto.setTimestamp(log.getTimestamp());
        dto.setLogin(log.getLogin());
        dto.setAction(log.getAction());
        dto.setDetails(log.getDetails());
        dto.setIpAddress(log.getIpAddress());
        dto.setCountry(log.getCountry());
        dto.setCity(log.getCity());
        dto.setLatitude(log.getLatitude());
        dto.setLongitude(log.getLongitude());
        dto.setUserAgent(log.getUserAgent());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
