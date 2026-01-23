package ch.goodone.angularai.backend.repository;

import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.model.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByUserOrderByPositionAsc(User user);
    long countByStatus(TaskStatus status);
    List<Task> findByPriorityInOrderByIdDesc(java.util.Collection<Priority> priorities, Pageable pageable);
    long countByCreatedAtAfter(java.time.LocalDateTime timestamp);
    long countByStatusAndCreatedAtAfter(TaskStatus status, java.time.LocalDateTime timestamp);
}
