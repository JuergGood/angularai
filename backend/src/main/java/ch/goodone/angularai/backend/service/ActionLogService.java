package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.dto.ActionLogDTO;
import ch.goodone.angularai.backend.model.ActionLog;
import ch.goodone.angularai.backend.repository.ActionLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActionLogService {
    
    private static final String ACTION_FIELD = "action";

    private final ActionLogRepository actionLogRepository;
    private final IpLocationService ipLocationService;

    public ActionLogService(ActionLogRepository actionLogRepository, IpLocationService ipLocationService) {
        this.actionLogRepository = actionLogRepository;
        this.ipLocationService = ipLocationService;
    }

    @Transactional
    public void log(String login, String action, String details) {
        ActionLog actionLog = new ActionLog(login, action, details);
        actionLogRepository.save(actionLog);
    }

    @Async
    @Transactional
    public void logLogin(String login, String ip, String userAgent) {
        IpLocationService.GeoLocation location = ipLocationService.lookup(ip);
        
        String uaDetails = userAgent != null ? userAgent : "Unknown";

        ActionLog log = new ActionLog(login, "USER_LOGIN", "User logged in successfully");
        log.setIpAddress(ip);
        log.setCountry(location.getCountry());
        log.setCity(location.getCity());
        log.setLatitude(location.getLatitude());
        log.setLongitude(location.getLongitude());
        log.setUserAgent(uaDetails);
        
        actionLogRepository.save(log);
    }

    public Page<ActionLogDTO> getLogs(Pageable pageable, String type, LocalDateTime start, LocalDateTime end) {
        Specification<ActionLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null && !type.equalsIgnoreCase("all")) {
                if (type.equalsIgnoreCase("login")) {
                    predicates.add(cb.or(
                            cb.equal(root.get(ACTION_FIELD), "USER_LOGIN"),
                            cb.equal(root.get(ACTION_FIELD), "USER_LOGOUT"),
                            cb.equal(root.get(ACTION_FIELD), "USER_REGISTERED")
                    ));
                } else if (type.equalsIgnoreCase("task")) {
                    predicates.add(cb.like(root.get(ACTION_FIELD), "TASK_%"));
                } else if (type.equalsIgnoreCase("user admin")) {
                    predicates.add(cb.or(
                            cb.equal(root.get(ACTION_FIELD), "USER_CREATED"),
                            cb.equal(root.get(ACTION_FIELD), "USER_MODIFIED"),
                            cb.equal(root.get(ACTION_FIELD), "USER_DELETED")
                    ));
                }
            }

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return actionLogRepository.findAll(spec, pageable).map(ActionLogDTO::fromEntity);
    }

    @Transactional
    public void clearLogs() {
        actionLogRepository.deleteAll();
    }

    @Transactional
    public ActionLogDTO createLog(ActionLogDTO logDTO) {
        LocalDateTime timestamp = logDTO.getTimestamp() != null ? logDTO.getTimestamp() : LocalDateTime.now();
        ActionLog actionLog = new ActionLog(logDTO.getLogin(), logDTO.getAction(), logDTO.getDetails(), timestamp);
        return ActionLogDTO.fromEntity(actionLogRepository.save(actionLog));
    }
}
