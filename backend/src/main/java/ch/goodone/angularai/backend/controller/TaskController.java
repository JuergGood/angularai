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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for managing user tasks")
public class TaskController {
    private static final String STATUS = "status";
    private static final String DUE_DATE = "dueDate";
    private static final String PRIORITY = "priority";
    private static final String POSITION = "position";

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActionLogService actionLogService;
    private final ch.goodone.angularai.backend.service.TaskParserService taskParserService;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository, ActionLogService actionLogService, ch.goodone.angularai.backend.service.TaskParserService taskParserService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.actionLogService = actionLogService;
        this.taskParserService = taskParserService;
    }

    @PostMapping("/analyze")
    public TaskDTO analyzeTask(@RequestBody String input) {
        var parsed = taskParserService.parse(input);
        TaskDTO dto = new TaskDTO();
        dto.setTitle(parsed.title());
        dto.setDescription(parsed.description());
        dto.setDueDate(parsed.dueDate());
        dto.setPriority(parsed.priority());
        dto.setStatus(parsed.status().name());
        dto.setTags(parsed.tags());
        return dto;
    }

    @GetMapping
    @Operation(summary = "Get current user's tasks with filtering and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<TaskDTO> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String smartFilter,
            @RequestParam(required = false) String sort,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("user"), user);
        
        if (status != null) {
            final String statusVal = status;
            spec = spec.and((root, query, cb) -> cb.equal(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.valueOf(statusVal)));
        }
        
        if (smartFilter != null) {
            LocalDate today = LocalDate.now();
            switch (smartFilter.toUpperCase()) {
                case "TODAY":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.equal(root.get(DUE_DATE), today),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.ARCHIVED)
                    ));
                    break;
                case "UPCOMING":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.greaterThan(root.get(DUE_DATE), today),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.ARCHIVED)
                    ));
                    break;
                case "OVERDUE":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.lessThan(root.get(DUE_DATE), today),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.ARCHIVED)
                    ));
                    break;
                case "HIGH":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.or(
                                cb.equal(root.get(PRIORITY), ch.goodone.angularai.backend.model.Priority.HIGH),
                                cb.equal(root.get(PRIORITY), ch.goodone.angularai.backend.model.Priority.CRITICAL)
                            ),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), ch.goodone.angularai.backend.model.TaskStatus.ARCHIVED)
                    ));
                    break;
                default:
                    // No additional filtering for unknown smart filters
                    break;
            }
        }

        Sort sortObj = Sort.by(Sort.Direction.ASC, POSITION);
        if (sort != null) {
            switch (sort) {
                case "DUE_ASC":
                    sortObj = Sort.by(Sort.Direction.ASC, "dueDate").and(Sort.by(Sort.Direction.ASC, "position"));
                    break;
                case "PRIO_DESC":
                    sortObj = Sort.by(Sort.Direction.DESC, "priority").and(Sort.by(Sort.Direction.ASC, "position"));
                    break;
                case "UPDATED_DESC":
                    sortObj = Sort.by(Sort.Direction.DESC, "updatedAt");
                    break;
            }
        }
        
        return taskRepository.findAll(spec, sortObj).stream()
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
        if (taskDTO.getTags() != null) {
            task.setTags(new java.util.ArrayList<>(taskDTO.getTags()));
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
                    if (taskDTO.getTags() != null) {
                        task.setTags(new java.util.ArrayList<>(taskDTO.getTags()));
                    }
                    Task updatedTask = taskRepository.save(task);
                    actionLogService.log(user.getLogin(), "TASK_UPDATED", "Task updated: " + updatedTask.getTitle());
                    return ResponseEntity.ok(TaskDTO.fromEntity(updatedTask));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> patchTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return taskRepository.findById(id)
                .filter(task -> task.getUser().getId().equals(user.getId()))
                .map(task -> {
                    if (taskDTO.getTitle() != null) {
                        task.setTitle(taskDTO.getTitle());
                    }
                    if (taskDTO.getDescription() != null) {
                        task.setDescription(taskDTO.getDescription());
                    }
                    if (taskDTO.getDueDate() != null) {
                        task.setDueDate(taskDTO.getDueDate());
                    }
                    if (taskDTO.getPriority() != null) {
                        task.setPriority(taskDTO.getPriority());
                    }
                    if (taskDTO.getStatus() != null) {
                        task.setStatus(ch.goodone.angularai.backend.model.TaskStatus.valueOf(taskDTO.getStatus()));
                    }
                    if (taskDTO.getPosition() != null) {
                        task.setPosition(taskDTO.getPosition());
                    }
                    if (taskDTO.getTags() != null) {
                        task.setTags(new java.util.ArrayList<>(taskDTO.getTags()));
                    }
                    Task updatedTask = taskRepository.save(task);
                    actionLogService.log(user.getLogin(), "TASK_PATCHED", "Task patched: " + updatedTask.getTitle());
                    return ResponseEntity.ok(TaskDTO.fromEntity(updatedTask));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/bulk")
    public ResponseEntity<List<TaskDTO>> bulkPatchTasks(@RequestBody BulkPatchRequest req, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Task> tasks = taskRepository.findAllById(req.ids());
        
        List<Task> updatedTasks = tasks.stream()
                .filter(task -> task.getUser().getId().equals(user.getId()))
                .map(task -> {
                    TaskDTO patch = req.patch();
                    if (patch.getTitle() != null) task.setTitle(patch.getTitle());
                    if (patch.getDescription() != null) task.setDescription(patch.getDescription());
                    if (patch.getDueDate() != null) task.setDueDate(patch.getDueDate());
                    if (patch.getPriority() != null) task.setPriority(patch.getPriority());
                    if (patch.getStatus() != null) task.setStatus(ch.goodone.angularai.backend.model.TaskStatus.valueOf(patch.getStatus()));
                    if (patch.getTags() != null) task.setTags(new java.util.ArrayList<>(patch.getTags()));
                    return task;
                })
                .toList();
        
        List<Task> savedTasks = taskRepository.saveAll(updatedTasks);
        actionLogService.log(user.getLogin(), "TASK_BULK_PATCHED", "Bulk updated " + savedTasks.size() + " tasks");
        
        return ResponseEntity.ok(savedTasks.stream().map(TaskDTO::fromEntity).toList());
    }

    public record BulkPatchRequest(List<Long> ids, TaskDTO patch) {}

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

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDeleteTasks(@RequestBody List<Long> ids, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Task> tasks = taskRepository.findAllById(ids);
        List<Task> toDelete = tasks.stream()
                .filter(task -> task.getUser().getId().equals(user.getId()))
                .toList();
        
        taskRepository.deleteAll(toDelete);
        actionLogService.log(user.getLogin(), "TASK_BULK_REMOVED", "Bulk removed " + toDelete.size() + " tasks");
        
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
