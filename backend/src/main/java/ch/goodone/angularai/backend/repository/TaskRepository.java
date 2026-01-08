package ch.goodone.angularai.backend.repository;

import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByPositionAsc(User user);
}
