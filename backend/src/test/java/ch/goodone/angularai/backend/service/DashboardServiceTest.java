package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.dto.DashboardDTO;
import ch.goodone.angularai.backend.model.*;
import ch.goodone.angularai.backend.repository.ActionLogRepository;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActionLogRepository actionLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardData_shouldReturnPopulatedDashboardDTO() {
        // Arrange
        when(taskRepository.countByStatus(TaskStatus.OPEN)).thenReturn(5L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.DONE)).thenReturn(10L);
        when(userRepository.count()).thenReturn(20L);
        when(actionLogRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(15L);
        when(taskRepository.count()).thenReturn(18L);

        User user = new User("John", "Doe", "jdoe", "pass", "john@example.com", LocalDate.of(1990, 1, 1), "Addr", Role.ROLE_USER);
        user.setId(1L);
        
        Task task = new Task("Task 1", "Desc", LocalDate.now(), Priority.HIGH, user);
        task.setId(1L);
        
        ActionLog actionLog = new ActionLog("jdoe", "LOGIN", "Logged in");
        actionLog.setId(1L);

        when(taskRepository.findByPriorityOrderByIdDesc(eq(Priority.HIGH), any(PageRequest.class)))
                .thenReturn(List.of(task));
        when(actionLogRepository.findAllByOrderByTimestampDesc(any(PageRequest.class)))
                .thenReturn(List.of(actionLog));
        when(userRepository.findAllByOrderByIdDesc(any(PageRequest.class)))
                .thenReturn(List.of(user));

        // Act
        DashboardDTO result = dashboardService.getDashboardData();

        // Assert
        assertThat(result).isNotNull();
        
        // Summary stats
        assertThat(result.getSummary().getOpenTasks()).isEqualTo(8L); // 5 + 3
        assertThat(result.getSummary().getCompletedTasks()).isEqualTo(10L);
        assertThat(result.getSummary().getActiveUsers()).isEqualTo(20L);
        assertThat(result.getSummary().getTodayLogs()).isEqualTo(15L);

        // Task distribution
        assertThat(result.getTaskDistribution().getOpen()).isEqualTo(5L);
        assertThat(result.getTaskDistribution().getInProgress()).isEqualTo(3L);
        assertThat(result.getTaskDistribution().getCompleted()).isEqualTo(10L);
        assertThat(result.getTaskDistribution().getTotal()).isEqualTo(18L);

        // Lists
        assertThat(result.getPriorityTasks()).hasSize(1);
        assertThat(result.getPriorityTasks().get(0).getTitle()).isEqualTo("Task 1");
        
        assertThat(result.getRecentActivity()).hasSize(1);
        assertThat(result.getRecentActivity().get(0).getLogin()).isEqualTo("jdoe");
        
        assertThat(result.getRecentUsers()).hasSize(1);
        assertThat(result.getRecentUsers().get(0).getLogin()).isEqualTo("jdoe");
    }

    @Test
    void getDashboardData_shouldReturnEmptyDashboardDTO_whenNoData() {
        // Arrange
        when(taskRepository.countByStatus(any())).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(actionLogRepository.countByTimestampAfter(any())).thenReturn(0L);
        when(taskRepository.count()).thenReturn(0L);

        when(taskRepository.findByPriorityOrderByIdDesc(any(), any())).thenReturn(Collections.emptyList());
        when(actionLogRepository.findAllByOrderByTimestampDesc(any())).thenReturn(Collections.emptyList());
        when(userRepository.findAllByOrderByIdDesc(any())).thenReturn(Collections.emptyList());

        // Act
        DashboardDTO result = dashboardService.getDashboardData();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSummary().getOpenTasks()).isZero();
        assertThat(result.getTaskDistribution().getTotal()).isZero();
        assertThat(result.getPriorityTasks()).isEmpty();
        assertThat(result.getRecentActivity()).isEmpty();
        assertThat(result.getRecentUsers()).isEmpty();
    }
}
