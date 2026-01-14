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
import java.util.stream.Collectors;

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
        // Summary Stats
        long openTasks = taskRepository.countByStatus(TaskStatus.OPEN) + taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(TaskStatus.CLOSED);
        long activeUsers = userRepository.count();
        
        LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayLogs = actionLogRepository.countByTimestampAfter(startOfToday);

        // For simplicity, deltas are just placeholder values or simplified logic
        // In a real app, you'd compare with yesterday's totals
        DashboardDTO.SummaryStats summary = new DashboardDTO.SummaryStats(
                openTasks, 2, // delta placeholder
                activeUsers, 1, // delta placeholder
                completedTasks, 5, // delta placeholder
                todayLogs, 10 // delta placeholder
        );

        // Task Distribution
        long openCount = taskRepository.countByStatus(TaskStatus.OPEN);
        long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedCount = taskRepository.countByStatus(TaskStatus.CLOSED);
        long totalCount = taskRepository.count();
        DashboardDTO.TaskStatusDistribution distribution = new DashboardDTO.TaskStatusDistribution(
                openCount, inProgressCount, completedCount, totalCount
        );

        // Recent Data
        List<TaskDTO> priorityTasks = taskRepository.findByPriorityOrderByIdDesc(Priority.HIGH, PageRequest.of(0, 5))
                .stream().map(TaskDTO::fromEntity).collect(Collectors.toList());

        List<ActionLogDTO> recentActivity = actionLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 5))
                .stream().map(ActionLogDTO::fromEntity).collect(Collectors.toList());

        List<UserDTO> recentUsers = userRepository.findAllByOrderByIdDesc(PageRequest.of(0, 5))
                .stream().map(UserDTO::fromEntity).collect(Collectors.toList());

        return new DashboardDTO(summary, priorityTasks, recentActivity, recentUsers, distribution);
    }
}
