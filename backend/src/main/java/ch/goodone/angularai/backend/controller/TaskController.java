package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.TaskParserService;
import ch.goodone.angularai.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for managing user tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;
    private final TaskParserService taskParserService;
    private final ActionLogService actionLogService;

    public TaskController(TaskService taskService, UserRepository userRepository, TaskParserService taskParserService, ActionLogService actionLogService) {
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.taskParserService = taskParserService;
        this.actionLogService = actionLogService;
    }

    @PostMapping("/analyze")
    public TaskDTO analyzeTask(@RequestBody Map<String, String> payload) {
        String input = payload.get("input");
        TaskParserService.ParsedTask parsed = taskParserService.parse(input);
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
        return taskService.getTasks(getCurrentUser(authentication), status, smartFilter, sort);
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO taskDTO, Authentication authentication) {
        return taskService.createTask(getCurrentUser(authentication), taskDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO, Authentication authentication) {
        return taskService.updateTask(getCurrentUser(authentication), id, taskDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> patchTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO, Authentication authentication) {
        return taskService.patchTask(getCurrentUser(authentication), id, taskDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/bulk")
    public ResponseEntity<List<TaskDTO>> bulkPatchTasks(@RequestBody BulkPatchRequest req, Authentication authentication) {
        List<TaskDTO> updated = taskService.bulkPatchTasks(getCurrentUser(authentication), req.ids(), req.patch());
        return ResponseEntity.ok(updated);
    }

    public record BulkPatchRequest(List<Long> ids, TaskDTO patch) {}

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderTasks(@RequestBody List<Long> taskIds, Authentication authentication) {
        taskService.reorderTasks(getCurrentUser(authentication), taskIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        if (taskService.deleteTask(getCurrentUser(authentication), id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDeleteTasks(@RequestBody List<Long> ids, Authentication authentication) {
        taskService.bulkDeleteTasks(getCurrentUser(authentication), ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getTaskMetrics(Authentication authentication) {
        List<TaskDTO> tasks = taskService.getTasks(getCurrentUser(authentication), null, null, null);
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("total", tasks.size());
        
        long completed = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        metrics.put("completed", completed);
        
        long overdue = tasks.stream().filter(t -> {
            if (t.getDueDate() == null) {
                return false;
            }
            return t.getDueDate().isBefore(LocalDate.now()) && !"DONE".equals(t.getStatus());
        }).count();
        metrics.put("overdue", overdue);

        return ResponseEntity.ok(metrics);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> {
                    actionLogService.log("unknown", "USER_ERROR", "User not found: " + authentication.getName());
                    return new RuntimeException("User not found");
                });
    }
}
