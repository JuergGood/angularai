package ch.goodone.angularai.backend.dto;

import java.util.List;

public class DashboardDTO {
    private SummaryStats summary;
    private List<TaskDTO> priorityTasks;
    private List<ActionLogDTO> recentActivity;
    private List<UserDTO> recentUsers;
    private TaskStatusDistribution taskDistribution;

    public DashboardDTO() {}

    public DashboardDTO(SummaryStats summary, List<TaskDTO> priorityTasks, List<ActionLogDTO> recentActivity, List<UserDTO> recentUsers, TaskStatusDistribution taskDistribution) {
        this.summary = summary;
        this.priorityTasks = priorityTasks;
        this.recentActivity = recentActivity;
        this.recentUsers = recentUsers;
        this.taskDistribution = taskDistribution;
    }

    public SummaryStats getSummary() { return summary; }
    public void setSummary(SummaryStats summary) { this.summary = summary; }

    public List<TaskDTO> getPriorityTasks() { return priorityTasks; }
    public void setPriorityTasks(List<TaskDTO> priorityTasks) { this.priorityTasks = priorityTasks; }

    public List<ActionLogDTO> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<ActionLogDTO> recentActivity) { this.recentActivity = recentActivity; }

    public List<UserDTO> getRecentUsers() { return recentUsers; }
    public void setRecentUsers(List<UserDTO> recentUsers) { this.recentUsers = recentUsers; }

    public TaskStatusDistribution getTaskDistribution() { return taskDistribution; }
    public void setTaskDistribution(TaskStatusDistribution taskDistribution) { this.taskDistribution = taskDistribution; }

    public static class SummaryStats {
        private long openTasks;
        private long openTasksDelta;
        private long activeUsers;
        private long activeUsersDelta;
        private long completedTasks;
        private long completedTasksDelta;
        private long todayLogs;
        private long todayLogsDelta;

        public SummaryStats() {}

        public SummaryStats(long openTasks, long openTasksDelta, long activeUsers, long activeUsersDelta, long completedTasks, long completedTasksDelta, long todayLogs, long todayLogsDelta) {
            this.openTasks = openTasks;
            this.openTasksDelta = openTasksDelta;
            this.activeUsers = activeUsers;
            this.activeUsersDelta = activeUsersDelta;
            this.completedTasks = completedTasks;
            this.completedTasksDelta = completedTasksDelta;
            this.todayLogs = todayLogs;
            this.todayLogsDelta = todayLogsDelta;
        }

        public long getOpenTasks() { return openTasks; }
        public void setOpenTasks(long openTasks) { this.openTasks = openTasks; }
        public long getOpenTasksDelta() { return openTasksDelta; }
        public void setOpenTasksDelta(long openTasksDelta) { this.openTasksDelta = openTasksDelta; }
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        public long getActiveUsersDelta() { return activeUsersDelta; }
        public void setActiveUsersDelta(long activeUsersDelta) { this.activeUsersDelta = activeUsersDelta; }
        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
        public long getCompletedTasksDelta() { return completedTasksDelta; }
        public void setCompletedTasksDelta(long completedTasksDelta) { this.completedTasksDelta = completedTasksDelta; }
        public long getTodayLogs() { return todayLogs; }
        public void setTodayLogs(long todayLogs) { this.todayLogs = todayLogs; }
        public long getTodayLogsDelta() { return todayLogsDelta; }
        public void setTodayLogsDelta(long todayLogsDelta) { this.todayLogsDelta = todayLogsDelta; }
    }

    public static class TaskStatusDistribution {
        private long open;
        private long inProgress;
        private long completed;
        private long total;

        public TaskStatusDistribution() {}

        public TaskStatusDistribution(long open, long inProgress, long completed, long total) {
            this.open = open;
            this.inProgress = inProgress;
            this.completed = completed;
            this.total = total;
        }

        public long getOpen() { return open; }
        public void setOpen(long open) { this.open = open; }
        public long getInProgress() { return inProgress; }
        public void setInProgress(long inProgress) { this.inProgress = inProgress; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }
}
