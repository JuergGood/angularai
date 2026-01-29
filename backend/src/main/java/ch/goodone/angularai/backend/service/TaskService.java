package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.TaskStatus;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final String STATUS = "status";
    private static final String DUE_DATE = "dueDate";
    private static final String PRIORITY = "priority";
    private static final String POSITION = "position";

    private final TaskRepository taskRepository;
    private final ActionLogService actionLogService;

    public TaskService(TaskRepository taskRepository, ActionLogService actionLogService) {
        this.taskRepository = taskRepository;
        this.actionLogService = actionLogService;
    }

    public List<TaskDTO> getTasks(User user, String status, String smartFilter, String sort) {
        Specification<Task> spec = (root, query, cb) -> cb.equal(root.get("user"), user);

        if (status != null) {
            final String statusVal = status;
            spec = spec.and((root, query, cb) -> cb.equal(root.get(STATUS), TaskStatus.valueOf(statusVal)));
        }

        if (smartFilter != null) {
            LocalDate today = LocalDate.now();
            switch (smartFilter.toUpperCase()) {
                case "TODAY":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.equal(root.get(DUE_DATE), today),
                            cb.notEqual(root.get(STATUS), TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), TaskStatus.ARCHIVED)
                    ));
                    break;
                case "UPCOMING":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.greaterThan(root.get(DUE_DATE), today),
                            cb.notEqual(root.get(STATUS), TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), TaskStatus.ARCHIVED)
                    ));
                    break;
                case "OVERDUE":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.lessThan(root.get(DUE_DATE), today),
                            cb.notEqual(root.get(STATUS), TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), TaskStatus.ARCHIVED)
                    ));
                    break;
                case "HIGH":
                    spec = spec.and((root, query, cb) -> cb.and(
                            cb.or(
                                cb.equal(root.get(PRIORITY), Priority.HIGH),
                                cb.equal(root.get(PRIORITY), Priority.CRITICAL)
                            ),
                            cb.notEqual(root.get(STATUS), TaskStatus.DONE),
                            cb.notEqual(root.get(STATUS), TaskStatus.ARCHIVED)
                    ));
                    break;
                default:
                    break;
            }
        }

        Sort sortObj = Sort.by(Sort.Direction.ASC, POSITION);
        if (sort != null) {
            switch (sort) {
                case "DUE_ASC":
                    sortObj = Sort.by(Sort.Direction.ASC, DUE_DATE).and(Sort.by(Sort.Direction.ASC, POSITION));
                    break;
                case "PRIO_DESC":
                    sortObj = Sort.by(Sort.Direction.DESC, PRIORITY).and(Sort.by(Sort.Direction.ASC, POSITION));
                    break;
                case "UPDATED_DESC":
                    sortObj = Sort.by(Sort.Direction.DESC, "updatedAt");
                    break;
                default:
                    break;
            }
        }

        return taskRepository.findAll(spec, sortObj).stream()
                .map(TaskDTO::fromEntity)
                .toList();
    }

    @Transactional
    public TaskDTO createTask(User user, TaskDTO taskDTO) {
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
            task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
        }
        if (taskDTO.getTags() != null) {
            task.setTags(new java.util.ArrayList<>(taskDTO.getTags()));
        }

        Task savedTask = taskRepository.save(task);
        actionLogService.log(user.getLogin(), "TASK_ADDED", "Task created: " + savedTask.getTitle());
        return TaskDTO.fromEntity(savedTask);
    }

    @Transactional
    public Optional<TaskDTO> updateTask(User user, Long id, TaskDTO taskDTO) {
        return taskRepository.findById(id)
                .filter(t -> t.getUser().equals(user))
                .map(task -> {
                    task.setTitle(taskDTO.getTitle());
                    task.setDescription(taskDTO.getDescription());
                    task.setDueDate(taskDTO.getDueDate());
                    task.setPriority(taskDTO.getPriority());
                    if (taskDTO.getStatus() != null) {
                        task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
                    }
                    if (taskDTO.getTags() != null) {
                        task.setTags(new java.util.ArrayList<>(taskDTO.getTags()));
                    }
                    Task savedTask = taskRepository.save(task);
                    actionLogService.log(user.getLogin(), "TASK_UPDATED", "Task updated: " + savedTask.getTitle());
                    return TaskDTO.fromEntity(savedTask);
                });
    }

    @Transactional
    public Optional<TaskDTO> patchTask(User user, Long id, TaskDTO taskDTO) {
        return taskRepository.findById(id)
                .filter(t -> t.getUser().equals(user))
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
                        task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
                    }
                    if (taskDTO.getTags() != null) {
                        task.setTags(new java.util.ArrayList<>(taskDTO.getTags()));
                    }
                    
                    Task savedTask = taskRepository.save(task);
                    actionLogService.log(user.getLogin(), "TASK_PATCHED", "Task patched: " + savedTask.getTitle());
                    return TaskDTO.fromEntity(savedTask);
                });
    }

    @Transactional
    public List<TaskDTO> bulkPatchTasks(User user, List<Long> ids, TaskDTO patch) {
        List<Task> tasks = taskRepository.findAllById(ids).stream()
                .filter(t -> t.getUser().equals(user))
                .toList();
        
        for (Task task : tasks) {
            if (patch.getStatus() != null) {
                task.setStatus(TaskStatus.valueOf(patch.getStatus()));
            }
            if (patch.getPriority() != null) {
                task.setPriority(patch.getPriority());
            }
        }
        
        List<Task> saved = taskRepository.saveAll(tasks);
        actionLogService.log(user.getLogin(), "TASK_BULK_PATCH", "Updated " + saved.size() + " tasks");
        return saved.stream().map(TaskDTO::fromEntity).toList();
    }

    @Transactional
    public void reorderTasks(User user, List<Long> taskIds) {
        List<Task> userTasks = taskRepository.findByUserOrderByPositionAsc(user);
        for (int i = 0; i < taskIds.size(); i++) {
            Long id = taskIds.get(i);
            int finalI = i;
            userTasks.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst()
                    .ifPresent(t -> t.setPosition(finalI));
        }
        taskRepository.saveAll(userTasks);
        actionLogService.log(user.getLogin(), "TASKS_REORDERED", "Reordered " + taskIds.size() + " tasks");
    }

    @Transactional
    public boolean deleteTask(User user, Long id) {
        return taskRepository.findById(id)
                .filter(t -> t.getUser().equals(user))
                .map(task -> {
                    taskRepository.delete(task);
                    actionLogService.log(user.getLogin(), "TASK_DELETED", "Task deleted: " + task.getTitle());
                    return true;
                }).orElse(false);
    }

    @Transactional
    public void bulkDeleteTasks(User user, List<Long> ids) {
        List<Task> tasks = taskRepository.findAllById(ids).stream()
                .filter(t -> t.getUser().equals(user))
                .toList();
        taskRepository.deleteAll(tasks);
        actionLogService.log(user.getLogin(), "TASK_BULK_DELETE", "Deleted " + tasks.size() + " tasks");
    }
}
