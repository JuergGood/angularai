package ch.goodone.angularai.android.data.remote.dto

data class DashboardDTO(
    val summary: SummaryStatsDTO,
    val priorityTasks: List<TaskDTO>,
    val recentActivity: List<ActionLogDTO>,
    val recentUsers: List<UserDTO>,
    val taskDistribution: TaskStatusDistributionDTO
)

data class SummaryStatsDTO(
    val openTasks: Long,
    val openTasksDelta: Long,
    val activeUsers: Long,
    val activeUsersDelta: Long,
    val completedTasks: Long,
    val completedTasksDelta: Long,
    val todayLogs: Long,
    val todayLogsDelta: Long
)

data class TaskStatusDistributionDTO(
    val open: Long,
    val inProgress: Long,
    val completed: Long,
    val total: Long
)
