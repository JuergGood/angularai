package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.model.ActionLog;
import ch.goodone.angularai.backend.repository.ActionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;

    public ActionLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    @Transactional
    public void log(String login, String action, String details) {
        ActionLog actionLog = new ActionLog(login, action, details);
        actionLogRepository.save(actionLog);
    }
}
