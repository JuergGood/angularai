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

    public ActionLogDTO() {}

    public ActionLogDTO(Long id, LocalDateTime timestamp, String login, String action, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.login = login;
        this.action = action;
        this.details = details;
    }

    public static ActionLogDTO fromEntity(ActionLog log) {
        return new ActionLogDTO(
                log.getId(),
                log.getTimestamp(),
                log.getLogin(),
                log.getAction(),
                log.getDetails()
        );
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
}
