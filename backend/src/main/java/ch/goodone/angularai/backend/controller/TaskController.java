package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for managing user tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActionLogService actionLogService;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository, ActionLogService actionLogService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.actionLogService = actionLogService;
    }

    @GetMapping
    @Operation(summary = "Get current user's tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<TaskDTO> getMyTasks(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return taskRepository.findByUserOrderByPositionAsc(user).stream()
                .map(TaskDTO::fromEntity)
                .toList();
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO, Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        // Determine position for new task
        List<Task> existingTasks = taskRepository.findByUserOrderByPositionAsc(user);
        int maxPosition = existingTasks.stream()
                .mapToInt(t -> t.getPosition() != null ? t.getPosition() : 0)
                .max()
                .orElse(-1);

        Task task = new Task(
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getDueDate(),
                taskDTO.getPriority(),
                user
        );
        task.setPosition(maxPosition + 1);
        if (taskDTO.getStatus() != null) {
            task.setStatus(ch.goodone.angularai.backend.model.TaskStatus.valueOf(taskDTO.getStatus()));
        }

        Task savedTask = taskRepository.save(task);
        actionLogService.log(user.getLogin(), "TASK_ADDED", "Task created: " + savedTask.getTitle());
        return TaskDTO.fromEntity(savedTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return taskRepository.findById(id)
                .filter(task -> task.getUser().getId().equals(user.getId()))
                .map(task -> {
                    task.setTitle(taskDTO.getTitle());
                    task.setDescription(taskDTO.getDescription());
                    task.setDueDate(taskDTO.getDueDate());
                    task.setPriority(taskDTO.getPriority());
                    if (taskDTO.getStatus() != null) {
                        task.setStatus(ch.goodone.angularai.backend.model.TaskStatus.valueOf(taskDTO.getStatus()));
                    }
                    if (taskDTO.getPosition() != null) {
                        task.setPosition(taskDTO.getPosition());
                    }
                    Task updatedTask = taskRepository.save(task);
                    actionLogService.log(user.getLogin(), "TASK_UPDATED", "Task updated: " + updatedTask.getTitle());
                    return ResponseEntity.ok(TaskDTO.fromEntity(updatedTask));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderTasks(@RequestBody List<Long> taskIds, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Task> userTasks = taskRepository.findByUserOrderByPositionAsc(user);
        
        for (int i = 0; i < taskIds.size(); i++) {
            Long id = taskIds.get(i);
            int finalI = i;
            userTasks.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst()
                    .ifPresent(t -> {
                        t.setPosition(finalI);
                        taskRepository.save(t);
                    });
        }
        
        actionLogService.log(user.getLogin(), "TASK_REORDERED", "Tasks reordered");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return taskRepository.findById(id)
                .filter(task -> task.getUser().getId().equals(user.getId()))
                .map(task -> {
                    String title = task.getTitle();
                    taskRepository.delete(task);
                    actionLogService.log(user.getLogin(), "TASK_REMOVED", "Task removed: " + title);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
