package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.TaskParserService;
import ch.goodone.angularai.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for managing user tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;
    private final TaskParserService taskParserService;

    public TaskController(TaskService taskService, UserRepository userRepository, TaskParserService taskParserService) {
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.taskParserService = taskParserService;
    }

    @PostMapping("/analyze")
    public TaskDTO analyzeTask(@RequestBody String input) {
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

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
