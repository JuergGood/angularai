package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.dto.*;
import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.TaskStatus;
import ch.goodone.angularai.backend.repository.ActionLogRepository;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActionLogRepository actionLogRepository;

    public DashboardService(TaskRepository taskRepository, UserRepository userRepository, ActionLogRepository actionLogRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.actionLogRepository = actionLogRepository;
    }

    public DashboardDTO getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Summary Stats
        long openTasks = taskRepository.countByStatus(TaskStatus.OPEN) + taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long activeUsers = userRepository.count();
        long todayLogs = actionLogRepository.countByTimestampAfter(startOfToday);

        // Real deltas: Items created today
        long openTasksDelta = taskRepository.countByStatusAndCreatedAtAfter(TaskStatus.OPEN, startOfToday) 
                            + taskRepository.countByStatusAndCreatedAtAfter(TaskStatus.IN_PROGRESS, startOfToday);
        long completedTasksDelta = taskRepository.countByStatusAndCreatedAtAfter(TaskStatus.DONE, startOfToday);
        long activeUsersDelta = userRepository.countByCreatedAtAfter(startOfToday);
        long todayLogsDelta = actionLogRepository.countByTimestampAfter(now.minusHours(1));

        DashboardDTO.SummaryStats summary = new DashboardDTO.SummaryStats(
                openTasks, openTasksDelta,
                activeUsers, activeUsersDelta,
                completedTasks, completedTasksDelta,
                todayLogs, todayLogsDelta
        );

        // Task Distribution
        long openCount = taskRepository.countByStatus(TaskStatus.OPEN);
        long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedCount = taskRepository.countByStatus(TaskStatus.DONE);
        long archivedCount = taskRepository.countByStatus(TaskStatus.ARCHIVED);
        long totalCount = taskRepository.count();
        DashboardDTO.TaskStatusDistribution distribution = new DashboardDTO.TaskStatusDistribution(
                openCount, inProgressCount, completedCount, archivedCount, totalCount
        );

        // Recent Data
        List<TaskDTO> priorityTasks = taskRepository.findByPriorityInOrderByIdDesc(List.of(Priority.CRITICAL, Priority.HIGH), PageRequest.of(0, 5))
                .stream().map(TaskDTO::fromEntity).toList();

        List<ActionLogDTO> recentActivity = actionLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 5))
                .stream().map(ActionLogDTO::fromEntity).toList();

        List<UserDTO> recentUsers = userRepository.findAllByOrderByIdDesc(PageRequest.of(0, 5))
                .stream().map(UserDTO::fromEntity).toList();

        return new DashboardDTO(summary, priorityTasks, recentActivity, recentUsers, distribution);
    }
}
