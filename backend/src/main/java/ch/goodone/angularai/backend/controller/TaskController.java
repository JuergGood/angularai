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
        return taskRepository.findByUser(user).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Task task = new Task(
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getDueDate(),
                taskDTO.getPriority(),
                user
        );
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
                    Task updatedTask = taskRepository.save(task);
                    actionLogService.log(user.getLogin(), "TASK_UPDATED", "Task updated: " + updatedTask.getTitle());
                    return ResponseEntity.ok(TaskDTO.fromEntity(updatedTask));
                })
                .orElse(ResponseEntity.notFound().build());
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
